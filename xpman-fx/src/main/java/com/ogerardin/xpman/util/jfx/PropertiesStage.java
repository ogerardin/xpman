package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.config.XPManPrefs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class PropertiesStage {

    private static final DefaultPropertyEditorFactory DEFAULT_EDITOR_FACTORY = new DefaultPropertyEditorFactory();

    @SneakyThrows
    public static void show(String key, String title, Object target) {
        show(key, title, target, Map.of(), null);
    }

    @SneakyThrows
    public static void show(String key, String title, Object target, Map<String, String> editableProperties,
                            Consumer<Map<String, String>> onSave) {
        if (target == null) {
            new Alert(Alert.AlertType.INFORMATION, "No object to inspect").showAndWait();
            return;
        }

        Map<String, String> pendingChanges = new HashMap<>();
        PropertySheet propertySheet = new PropertySheet(
                buildItems(target, editableProperties, pendingChanges));
        propertySheet.setPropertyEditorFactory(PropertiesStage::editorFor);

        propertySheet.setSearchBoxVisible(true);
        propertySheet.setModeSwitcherVisible(false);

        Stage stage = new Stage();
        stage.setTitle("Properties: " + (title == null ? target.toString() : title));
        VBox content = new VBox(8, propertySheet);
        VBox.setVgrow(propertySheet, Priority.ALWAYS);
        if (onSave != null && !editableProperties.isEmpty()) {
            Button saveButton = new Button("Save");
            saveButton.setDefaultButton(true);
            Button closeButton = new Button("Close");
            HBox buttons = new HBox(8, saveButton, closeButton);
            buttons.setAlignment(Pos.CENTER_RIGHT);
            content.getChildren().add(buttons);

            saveButton.setOnAction(_ -> save(stage, pendingChanges, onSave));
            closeButton.setOnAction(_ -> stage.fireEvent(
                    new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
            stage.setOnCloseRequest(event -> {
                if (pendingChanges.isEmpty()) {
                    return;
                }
                event.consume();
                ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.YES);
                ButtonType discard = new ButtonType("Discard", ButtonBar.ButtonData.NO);
                ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                        "You have unsaved changes.", save, discard, cancel);
                confirmation.initOwner(stage);
                ButtonType choice = confirmation.showAndWait().orElse(cancel);
                if (choice == save) {
                    save(stage, pendingChanges, onSave);
                } else if (choice == discard) {
                    pendingChanges.clear();
                    stage.close();
                }
            });
        }

        Scene scene = new Scene(content);
        scene.getStylesheets().add(PropertiesStage.class.getResource("/css/xpman.css").toExternalForm());
        stage.setScene(scene);
        if (JfxApp.getInstance() instanceof XPmanFX app) {
            XPManPrefs prefs = app.getConfig();
            JfxApp.applyWindowPosition(stage, prefs.getPropertiesPositions().get(key));
            stage.setOnHidden(_ -> {
                prefs.getPropertiesPositions().put(key, new JfxAppPrefs.WindowPosition(
                        stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
                app.saveConfig();
            });
        }
        stage.setMinWidth(480);
        stage.setMinHeight(320);
        if (onSave != null && !editableProperties.isEmpty()) {
            stage.showAndWait();
        } else {
            stage.show();
        }
    }

    private static PropertyEditor<?> editorFor(PropertySheet.Item item) {
        if (item instanceof ReadOnlyPropertyItem && !usesDefaultEditor(item)) {
            return new ReadOnlyPropertyEditor(item);
        }
        return DEFAULT_EDITOR_FACTORY.call(item);
    }

    private static boolean usesDefaultEditor(PropertySheet.Item item) {
        return item instanceof EditablePropertyItem;
    }

    private static void save(Stage stage, Map<String, String> pendingChanges,
                             Consumer<Map<String, String>> onSave) {
        try {
            onSave.accept(Map.copyOf(pendingChanges));
            pendingChanges.clear();
            stage.close();
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not save ACF properties: " + e.getMessage());
            alert.initOwner(stage);
            alert.showAndWait();
        }
    }

    private static ObservableList<PropertySheet.Item> buildItems(Object target,
                                                                  Map<String, String> editableProperties,
                                                                  Map<String, String> pendingChanges)
            throws java.beans.IntrospectionException {
        ObservableList<PropertySheet.Item> items = FXCollections.observableArrayList();
        Map<String, Object> cachedValues = new HashMap<>();
        items.add(new ReadOnlyPropertyItem("class", "class", String.class, target.getClass().getName()));

        BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass(), Object.class);
        Arrays.stream(beanInfo.getPropertyDescriptors())
                .filter(pd -> pd.getReadMethod() != null && !"xPlane".equals(pd.getName()))
                .sorted(Comparator.comparing(PropertyDescriptor::getName))
                .forEach(pd -> {
                    String propName = pd.getName();
                    String label = humanize(propName);
                    Object value = getCachedValue(target, pd, cachedValues);
                    String saveKey = editableProperties.get(propName);
                    items.add(saveKey == null
                            ? new ReadOnlyPropertyItem(label, propName, pd.getPropertyType(), value)
                            : new EditablePropertyItem(label, propName, pd.getPropertyType(), value,
                                    saveKey, pendingChanges));
                });
        return items;
    }

    private static String humanize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        String[] words = StringUtils.splitByCharacterTypeCamelCase(name);
        for (int i = 0; i < words.length; i++) {
            if (i == 0) {
                words[i] = StringUtils.capitalize(words[i]);
            } else {
                words[i] = StringUtils.lowerCase(words[i]);
            }
        }
        return String.join(" ", words);
    }

    @SneakyThrows
    private static Object getCachedValue(Object target, PropertyDescriptor pd, Map<String, Object> cache) {
        String key = pd.getName();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        try {
            Object value = pd.getReadMethod().invoke(target);
            cache.put(key, value);
            return value;
        } catch (Exception e) {
            String errorValue = "Error: " + e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
            cache.put(key, errorValue);
            return errorValue;
        }
    }

    private static class ReadOnlyPropertyItem implements PropertySheet.Item {
        private final String name;
        private final String category;
        private final Class<?> type;
        protected Object value;

        ReadOnlyPropertyItem(String name, String category, Class<?> type, Object value) {
            this.name = name;
            this.category = category;
            this.type = type;
            this.value = value;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public void setValue(Object o) {
            // Read-only
        }

        @Override
        public Optional<javafx.beans.value.ObservableValue<? extends Object>> getObservableValue() {
            return Optional.empty();
        }

        @Override
        public boolean isEditable() {
            // ControlsFX disables non-editable rows, which also prevents selecting/copying their values.
            return true;
        }
    }

    private static final class ReadOnlyPropertyEditor implements PropertyEditor<Object> {
        private final TextField textField = new TextField();

        ReadOnlyPropertyEditor(PropertySheet.Item item) {
            textField.setEditable(false);
            setValue(item.getValue());
        }

        @Override
        public TextField getEditor() {
            return textField;
        }

        @Override
        public Object getValue() {
            return textField.getText();
        }

        @Override
        public void setValue(Object value) {
            textField.setText(Objects.toString(value, ""));
        }
    }

    private static final class EditablePropertyItem extends ReadOnlyPropertyItem {
        private final Object originalValue;
        private final String saveKey;
        private final Map<String, String> pendingChanges;

        EditablePropertyItem(String name, String category, Class<?> type, Object value,
                             String saveKey, Map<String, String> pendingChanges) {
            super(name, category, type, value);
            this.originalValue = value;
            this.saveKey = saveKey;
            this.pendingChanges = pendingChanges;
        }

        @Override
        public void setValue(Object value) {
            this.value = Objects.toString(value, "");
            if (Objects.equals(this.value, Objects.toString(originalValue, ""))) {
                pendingChanges.remove(saveKey);
            } else {
                pendingChanges.put(saveKey, this.value.toString());
            }
        }

        @Override
        public boolean isEditable() {
            return true;
        }
    }
}
