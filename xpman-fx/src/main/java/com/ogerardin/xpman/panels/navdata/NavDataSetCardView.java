package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xpman.util.jfx.menu.GenericContextMenuFactory;
import com.ogerardin.xpman.util.jfx.menu.IntrospectionHelper;
import com.ogerardin.xpman.util.jfx.menu.MethodButton;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Card for one nav data set: header with layer badge, help button and inspection
 * status, plus an expandable per-file list. Hover actions and the context menu come
 * from the annotation-driven action framework (see AircraftCardView).
 */
public class NavDataSetCardView extends VBox {

    private static final int ICON_SIZE = 14;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** Icon per nav data set, keyed by display name (see NavDataManager.loadNavDataSets()). */
    private static final Map<String, Feather> SET_ICONS = Map.of(
            "Sim-wide ARINC424 override", Feather.GLOBE,
            "Base (shipped with X-Plane)", Feather.DATABASE,
            "Updated base (supplied by third-parties)", Feather.REFRESH_CW,
            "FAA updated approaches", Feather.FLAG,
            "Hand-placed localizers", Feather.MAP_PIN,
            "User data", Feather.USER);

    /** Icon per data file, keyed by leaf name ("CIFP" = directory of CIFPSummary). */
    private static final Map<String, Feather> FILE_ICONS = Map.ofEntries(
            Map.entry("earth_nav.dat", Feather.NAVIGATION),
            Map.entry("earth_fix.dat", Feather.MAP_PIN),
            Map.entry("earth_awy.dat", Feather.LINK),
            Map.entry("earth_hold.dat", Feather.ANCHOR),
            Map.entry("earth_mora.dat", Feather.LAYERS),
            Map.entry("earth_msa.dat", Feather.LAYERS),
            Map.entry("earth_424.dat", Feather.GLOBE),
            Map.entry("FAACIFP18", Feather.AIRPLAY),
            Map.entry("user_nav.dat", Feather.RADIO),
            Map.entry("user_fix.dat", Feather.RADIO),
            Map.entry("CIFP", Feather.FOLDER),
            Map.entry("airspace.txt", Feather.FILE_TEXT),
            Map.entry("atc.dat", Feather.FILE_TEXT));

    private final NavDataController controller;
    private final GenericContextMenuFactory<UiNavDataItem> menuFactory;

    private VBox filesBox;
    private FontIcon filesChevron;

    public NavDataSetCardView(UiNavDataItem uiItem, int layerIndex, int layerCount, NavDataController controller) {
        this.controller = controller;
        this.menuFactory = controller.getCardMenuFactory();

        getStyleClass().add("navdata-card");

        Label nameLabel = new Label(uiItem.getName(), icon(SET_ICONS.getOrDefault(uiItem.getName(), Feather.DATABASE)));
        nameLabel.getStyleClass().add("navdata-card-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        Label layerBadge = new Label("Layer " + layerIndex + "/" + layerCount);
        layerBadge.getStyleClass().add("navdata-card-badge");

        Label statusLabel = buildStatusLabel(uiItem);

        Button helpButton = new Button();
        helpButton.setGraphic(icon(Feather.HELP_CIRCLE));
        helpButton.getStyleClass().add("navdata-card-help");
        helpButton.setOnAction(__ -> NavDataInfoDialog.show(uiItem, this));

        HBox header = new HBox(8, nameLabel, layerBadge, statusLabel, helpButton);
        header.getStyleClass().add("navdata-card-header");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Button filesToggle = new Button(uiItem.getChildren().size() + " files");
        filesChevron = new FontIcon(Feather.CHEVRON_DOWN);
        filesChevron.setIconSize(ICON_SIZE);
        filesToggle.setGraphic(filesChevron);
        filesToggle.getStyleClass().add("navdata-card-files-toggle");
        filesToggle.setOnAction(__ -> toggleFiles(uiItem));

        HBox actions = buildHoverActions(uiItem);

        VBox info = new VBox(4, header, filesToggle, actions);
        VBox.setMargin(info, new Insets(6, 8, 6, 8));
        getChildren().add(info);

        ContextMenu cardMenu = menuFactory.menuFor(uiItem);
        setOnContextMenuRequested(event -> showMenu(cardMenu, this, event));
    }

    private static Label buildStatusLabel(UiNavDataItem uiItem) {
        List<InspectionMessage> messages = uiItem.inspect().getMessages();
        // NavDataSet.inspect() always appends the summary message last
        InspectionMessage summary = messages.get(messages.size() - 1);
        Label statusLabel = new Label(summary.getMessage());
        statusLabel.getStyleClass().add("navdata-status-"
                + summary.getSeverity().toString().toLowerCase(Locale.ROOT));
        return statusLabel;
    }

    private HBox buildHoverActions(UiNavDataItem uiItem) {
        HBox actions = new HBox(4);
        actions.getStyleClass().add("navdata-card-actions");
        addAction(actions, uiItem, "inspect", Feather.ACTIVITY);
        addAction(actions, uiItem, "reveal", Feather.FOLDER);
        return actions;
    }

    private void addAction(HBox actions, UiNavDataItem uiItem, String methodName, Feather feather) {
        findMethod(uiItem.getClass(), methodName).ifPresent(method -> {
            MethodButton<UiNavDataItem> button = new MethodButton<>(methodName, method, controller, uiItem);
            button.setText(null);
            button.setGraphic(icon(feather));
            button.getStyleClass().add("navdata-card-action");
            actions.getChildren().add(button);
        });
    }

    private static Optional<Method> findMethod(Class<?> type, String name) {
        return IntrospectionHelper.computeRelevantMethods(type).stream()
                .filter(method -> method.getName().equals(name) && method.getParameterCount() == 0)
                .findFirst();
    }

    private void toggleFiles(UiNavDataItem uiItem) {
        if (filesBox != null) {
            boolean expanded = !filesBox.isVisible();
            filesBox.setVisible(expanded);
            filesBox.setManaged(expanded);
            filesChevron.setRotate(expanded ? 90 : 0);
            return;
        }
        filesBox = new VBox(2);
        filesBox.getStyleClass().add("navdata-card-files");
        uiItem.getChildren().forEach(item -> filesBox.getChildren().add(buildFileRow(item)));
        getChildren().add(filesBox);
        filesChevron.setRotate(90);
    }

    private HBox buildFileRow(NavDataItem item) {
        Label nameLabel = new Label(item.getName(), icon(FILE_ICONS.getOrDefault(leafOf(item), Feather.FILE)));
        nameLabel.getStyleClass().add("navdata-card-file-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        HBox row = new HBox(8, nameLabel);
        row.getStyleClass().add("navdata-card-file-row");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        if (!item.getExists()) {
            nameLabel.getStyleClass().add("navdata-card-file-missing");
            Label missing = new Label("not found");
            missing.getStyleClass().add("navdata-status-error");
            row.getChildren().add(missing);
        } else {
            Optional.ofNullable(item.getAiracCycle()).ifPresent(cycle -> {
                Label cycleBadge = new Label("AIRAC " + cycle);
                cycleBadge.getStyleClass().add("navdata-card-badge");
                row.getChildren().add(cycleBadge);
            });
            Label meta = new Label(metaText(item));
            meta.getStyleClass().add("navdata-card-file-meta");
            row.getChildren().add(meta);
        }
        return row;
    }

    private static String leafOf(NavDataItem item) {
        Path path = item.getPath();
        return path != null && path.getFileName() != null ? path.getFileName().toString() : "";
    }

    private static String metaText(NavDataItem item) {
        try {
            Path path = item.getPath();
            String size = humanSize(Files.size(path));
            String modified = DATE_FORMAT.format(
                    Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault()));
            return size + " · " + modified;
        } catch (IOException e) {
            return "?";
        }
    }

    private static String humanSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        }
        if (bytes < 1024L * 1024 * 1024) {
            return String.format("%.1f MB", bytes / 1024.0 / 1024);
        }
        return String.format("%.1f GB", bytes / 1024.0 / 1024 / 1024);
    }

    private static FontIcon icon(Feather feather) {
        FontIcon fontIcon = new FontIcon(feather);
        fontIcon.setIconSize(ICON_SIZE);
        return fontIcon;
    }

    /**
     * Shows the given context menu over the owner node and guarantees that any mouse
     * press outside it (in the main window) dismisses it. Copied from AircraftCardView.
     */
    private static void showMenu(ContextMenu menu, Node owner, ContextMenuEvent event) {
        menu.setAutoHide(true);
        menu.show(owner, event.getScreenX(), event.getScreenY());
        event.consume();

        Scene scene = owner.getScene();
        if (scene == null) {
            return;
        }
        EventHandler<MouseEvent> outsidePressHandler = __ -> menu.hide();
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressHandler);
        menu.setOnHidden(__ -> scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressHandler));
    }
}
