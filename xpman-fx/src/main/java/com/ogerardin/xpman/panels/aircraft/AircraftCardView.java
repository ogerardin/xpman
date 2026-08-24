package com.ogerardin.xpman.panels.aircraft;

import com.ogerardin.xpman.util.jfx.menu.IntrospectionHelper;
import com.ogerardin.xpman.util.jfx.menu.GenericContextMenuFactory;
import com.ogerardin.xpman.util.jfx.menu.MethodActionConfigurer;
import com.ogerardin.xpman.util.jfx.menu.MethodButton;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A card displaying a single {@link UiAircraft}: thumbnail, name, studio/author, badges, and — when the
 * aircraft has liveries — an expandable row of livery mini-cards. Hovering the card reveals quick action
 * buttons (explore properties, inspect, reveal, move to trash) built through the annotation-driven action
 * framework so that confirmations and post-success behavior are preserved; the full action set remains
 * available through the context menu. Double-clicking the card opens the aircraft details view.
 */
@Slf4j
public class AircraftCardView extends VBox {

    private static final int ICON_SIZE = 14;

    private final AircraftsController controller;
    private final GenericContextMenuFactory<UiAircraft> menuFactory;

    private FlowPane liveryRow;

    private FontIcon liveriesChevron;

    public AircraftCardView(UiAircraft uiAircraft, AircraftsController controller) {
        this.controller = controller;
        this.menuFactory = controller.getCardMenuFactory();

        getStyleClass().add("aircraft-card");

        Node thumbnail = loadThumbnail(uiAircraft.getThumb(), 200, 120);
        thumbnail.getStyleClass().add("aircraft-card-thumb");

        Label nameLabel = new Label(uiAircraft.getName());
        nameLabel.getStyleClass().add("aircraft-card-name");

        Label authorLabel = new Label(authorText(uiAircraft));
        authorLabel.getStyleClass().add("aircraft-card-author");

        HBox badges = new HBox(6);
        badges.getStyleClass().add("aircraft-card-badges");
        Optional.ofNullable(uiAircraft.getAcfFile().getFileSpecVersion())
                .filter(spec -> !spec.isBlank())
                .map(spec -> badge("XP " + spec))
                .ifPresent(badges.getChildren()::add);
        int liveryCount = uiAircraft.getAircraft().getLiveries().size();
        if (liveryCount > 0) {
            Button liveryButton = new Button(liveryCount + " liveries");
            liveryButton.getStyleClass().add("aircraft-card-liveries");
            liveriesChevron = new FontIcon(Feather.CHEVRON_DOWN);
            liveriesChevron.setIconSize(ICON_SIZE);
            liveryButton.setGraphic(liveriesChevron);
            liveryButton.setOnAction(__ -> toggleLiveries(uiAircraft));
            badges.getChildren().add(liveryButton);
        }

        HBox actions = buildHoverActions(uiAircraft);

        VBox info = new VBox(2, nameLabel, authorLabel, badges);
        VBox.setMargin(info, new Insets(6, 8, 6, 8));

        getChildren().addAll(thumbnail, info, actions);

        ContextMenu cardMenu = menuFactory.menuFor(uiAircraft);
        setOnContextMenuRequested(event -> showMenu(cardMenu, this, event));
        setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                runAction(uiAircraft, "details");
            }
        });
    }

    private static Label badge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("aircraft-card-badge");
        return label;
    }

    private String authorText(UiAircraft uiAircraft) {
        String studio = uiAircraft.getStudio();
        String author = uiAircraft.getAuthor();
        if (studio != null && !studio.isBlank() && author != null && !author.isBlank()) {
            return studio + " / " + author;
        }
        if (studio != null && !studio.isBlank()) {
            return studio;
        }
        return author != null ? author : "";
    }

    private HBox buildHoverActions(UiAircraft uiAircraft) {
        HBox actions = new HBox(4);
        actions.getStyleClass().add("aircraft-card-actions");
        addAction(actions, uiAircraft, "details", Feather.SEARCH);
        addAction(actions, uiAircraft, "inspect", Feather.ACTIVITY);
        addAction(actions, uiAircraft, "reveal", Feather.FOLDER);
        addAction(actions, uiAircraft, "moveToTrash", Feather.TRASH_2);
        return actions;
    }

    private void addAction(HBox actions, UiAircraft uiAircraft, String methodName, Feather icon) {
        findMethod(uiAircraft.getClass(), methodName).ifPresent(method -> {
            MethodButton<UiAircraft> button = new MethodButton<>(methodName, method, controller, uiAircraft);
            button.setText(null);
            FontIcon fontIcon = new FontIcon(icon);
            fontIcon.setIconSize(ICON_SIZE);
            button.setGraphic(fontIcon);
            button.getStyleClass().add("aircraft-card-action");
            actions.getChildren().add(button);
        });
    }

    private void runAction(UiAircraft uiAircraft, String methodName) {
        findMethod(uiAircraft.getClass(), methodName).ifPresent(method ->
                new MethodActionConfigurer<>(
                        () -> getScene() != null ? getScene().getWindow() : null,
                        method, controller, uiAircraft, new Object[0])
                        .getMethodAction()
                        .run());
    }

    private static Optional<Method> findMethod(Class<?> type, String name) {
        return IntrospectionHelper.computeRelevantMethods(type).stream()
                .filter(method -> method.getName().equals(name) && method.getParameterCount() == 0)
                .findFirst();
    }

    private void toggleLiveries(UiAircraft uiAircraft) {
        if (liveryRow != null) {
            boolean expanded = !liveryRow.isVisible();
            liveryRow.setVisible(expanded);
            liveryRow.setManaged(expanded);
            setLiveriesExpanded(expanded);
            return;
        }
        // FlowPane wraps livery cards within the card width (2 per row) instead of
        // overflowing the fixed-width card
        liveryRow = new FlowPane(6, 6);
        liveryRow.getStyleClass().add("aircraft-card-livery-row");
        // the outer card grid sizes rows by prefHeight(-1): without a preferred wrap length the
        // livery FlowPane would report its single-row height and the expanded card would paint
        // over neighbouring rows
        liveryRow.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(
                () -> getWidth() - snappedLeftInset() - snappedRightInset(),
                widthProperty()));
        uiAircraft.getAircraft().getLiveries().forEach(livery ->
                liveryRow.getChildren().add(buildLiveryCard(new UiLivery(uiAircraft.getAircraft(), livery))));
        getChildren().add(liveryRow);
        setLiveriesExpanded(true);
    }

    private void setLiveriesExpanded(boolean expanded) {
        if (liveriesChevron != null) {
            liveriesChevron.setRotate(expanded ? 90 : 0);
        }
    }

    private VBox buildLiveryCard(UiLivery uiLivery) {
        VBox card = new VBox(4);
        card.getStyleClass().add("livery-card");
        // 88px wide so that two livery cards fit per row within the 202px card content width
        Node thumb = loadThumbnail(uiLivery.getThumb(), 88, 55);
        Label name = new Label(uiLivery.getLivery().getName());
        name.getStyleClass().add("livery-card-name");
        card.getChildren().addAll(thumb, name);
        ContextMenu liveryMenu = menuFactory.menuFor(uiLivery);
        card.setOnContextMenuRequested(event -> showMenu(liveryMenu, card, event));
        return card;
    }

    /**
     * Shows the given context menu over the owner node and guarantees that any mouse press
     * outside it (in the main window) dismisses it: in addition to the popup's own auto-hide,
     * a one-shot scene-level press filter hides the menu explicitly.
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

    private static Node loadThumbnail(Path path, double fitWidth, double fitHeight) {
        if (path != null && Files.exists(path)) {
            // background loading keeps the FX thread responsive while thumbnails decode
            Image image = new Image(path.toUri().toString(), true);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(fitWidth);
            imageView.setFitHeight(fitHeight);
            return imageView;
        }
        FontIcon placeholder = new FontIcon(Feather.IMAGE);
        placeholder.setIconSize(32);
        StackPane placeholderPane = new StackPane(placeholder);
        placeholderPane.getStyleClass().add("aircraft-card-thumb-placeholder");
        placeholderPane.setPrefSize(fitWidth, Math.min(fitHeight, 80));
        return placeholderPane;
    }
}
