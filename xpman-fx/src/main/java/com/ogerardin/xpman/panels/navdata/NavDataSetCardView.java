package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xplane.navdata.NavDataSetStatus;
import com.ogerardin.xpman.diag.FixRunner;
import com.ogerardin.xpman.diag.SeverityIconCellFactory;
import com.ogerardin.xpman.util.SizeFormat;
import com.ogerardin.xpman.util.jfx.menu.GenericContextMenuFactory;
import com.ogerardin.xpman.util.jfx.menu.IntrospectionHelper;
import com.ogerardin.xpman.util.jfx.menu.MethodButton;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Card for one nav data set: header with a four-state status ball, layer badge, help
 * button and short state, plus an expandable per-file list. Hover actions and the context
 * menu come from the annotation-driven action framework (see AircraftCardView).
 */
public class NavDataSetCardView extends VBox {

    private static final String STYLE_BADGE = "navdata-card-badge";

    private static final int ICON_SIZE = 14;
    private static final double BALL_RADIUS = 7;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

    public NavDataSetCardView(UiNavDataItem uiItem, int layerIndex, int layerCount, int overriding,
                               NavDataController controller) {
        this.controller = controller;
        this.menuFactory = controller.getCardMenuFactory();

        getStyleClass().add("navdata-card");

        // X-Plane loads no other navdata text file once an overriding layer is present,
        // so everything below it is dead weight and is shown greyed out.
        boolean ignored = overriding > 0 && layerIndex > overriding;
        int suppressed = layerIndex == overriding ? layerCount - layerIndex : 0;
        if (ignored) {
            getStyleClass().add("navdata-card-ignored");
        }

        InspectionResult inspection = uiItem.getInspectionResult();

        Label nameLabel = new Label(uiItem.getName(),
                buildStatusBall(uiItem, ignored, overriding, inspection));
        nameLabel.getStyleClass().add("navdata-card-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        Label layerBadge = new Label("Layer " + layerIndex + "/" + layerCount);
        layerBadge.getStyleClass().add(STYLE_BADGE);

        Label statusLabel = buildStatusLabel(uiItem);

        Button helpButton = new Button();
        helpButton.setGraphic(icon(Feather.HELP_CIRCLE));
        helpButton.getStyleClass().add("navdata-card-help");
        helpButton.setOnAction(_ -> NavDataInfoDialog.show(uiItem, this));

        HBox header = new HBox(8, nameLabel, layerBadge, statusLabel, helpButton);
        header.getStyleClass().add("navdata-card-header");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        if (ignored || suppressed > 0) {
            String overrideText;
            if (ignored) {
                overrideText = "ignored by layer " + overriding;
            } else {
                overrideText = "overrides " + suppressed + (suppressed == 1 ? " layer" : " layers");
            }
            Label overrideBadge = new Label(overrideText);
            overrideBadge.getStyleClass().add(STYLE_BADGE);
            overrideBadge.getStyleClass().add("navdata-card-badge-override");
            header.getChildren().add(overrideBadge);
        }

        Button filesToggle = new Button(uiItem.getChildren().size() + " files");
        filesChevron = new FontIcon(Feather.CHEVRON_DOWN);
        filesChevron.setIconSize(ICON_SIZE);
        filesToggle.setGraphic(filesChevron);
        filesToggle.getStyleClass().add("navdata-card-files-toggle");
        filesToggle.setOnAction(_ -> toggleFiles(uiItem));

        HBox filesRow = new HBox(4, filesToggle);
        findMethod(uiItem.getClass(), "reveal").ifPresent(method -> {
            MethodButton<UiNavDataItem> revealButton = new MethodButton<>("reveal", method, controller, uiItem);
            revealButton.setGraphic(icon(Feather.FOLDER));
            revealButton.getStyleClass().add("navdata-card-files-toggle");
            filesRow.getChildren().add(revealButton);
        });

        VBox info = new VBox(4, header, filesRow);
        VBox messagesBox = buildMessages(inspection, ignored);
        if (messagesBox != null) {
            info.getChildren().add(messagesBox);
        }
        VBox.setMargin(info, new Insets(6, 8, 6, 8));
        getChildren().add(info);

        ContextMenu cardMenu = menuFactory.menuFor(uiItem);
        setOnContextMenuRequested(event -> showMenu(cardMenu, this, event));

        if (uiItem.getChildren().stream().anyMatch(NavDataItem::getExists)) {
            toggleFiles(uiItem);
        }
    }

    /**
     * The card header shows a short state only — see {@code NavDataSet.describeState()}.
     * Anything more than a dozen characters belongs in the inspection dialog, so coverage
     * and cycle-consistency messages reach the header in the status ball's tooltip instead.
     */
    private static Label buildStatusLabel(UiNavDataItem uiItem) {
        Label statusLabel = new Label(uiItem.getState());
        statusLabel.getStyleClass().add("navdata-status-info");
        return statusLabel;
    }

    /**
     * The four-state status ball that stands in for the layer's icon: grey when X-Plane
     * reads none of the files, green when they are clean, orange on warnings, red on errors.
     * The tooltip shows only the summary line; the detailed WARN/ERROR messages are displayed
     * inline in the card.
     */
    private static Circle buildStatusBall(UiNavDataItem uiItem, boolean ignored, int overriding,
                                          InspectionResult inspection) {
        NavDataSetStatus status = NavDataSetStatus.of(uiItem.getExists(), ignored, inspection);
        Circle ball = new Circle(BALL_RADIUS);
        ball.getStyleClass().add(switch (status) {
            case INACTIVE -> "navdata-ball-inactive";
            case OK -> "navdata-ball-ok";
            case WARNING -> "navdata-ball-warning";
            case ERROR -> "navdata-ball-error";
        });
        Tooltip.install(ball, new Tooltip(tooltipText(ignored, overriding, status)));
        return ball;
    }

    private static String tooltipText(boolean ignored, int overriding, NavDataSetStatus status) {
        return switch (status) {
            case INACTIVE -> ignored ? "Ignored by layer " + overriding : "All files absent";
            case OK -> "OK";
            case WARNING -> "Warnings";
            case ERROR -> "Errors";
        };
    }

    /**
     * Shows WARN/ERROR messages inline on the card, each with its own fix buttons.
     * Returns null if the layer is ignored or there are no problems to display.
     */
    private VBox buildMessages(InspectionResult inspection, boolean ignored) {
        if (ignored) {
            return null;
        }
        List<InspectionMessage> problems = inspection.getMessages().stream()
                .filter(m -> m.getSeverity() != Severity.INFO)
                .toList();
        if (problems.isEmpty()) {
            return null;
        }
        VBox box = new VBox(2);
        box.getStyleClass().add("navdata-card-messages");
        for (InspectionMessage message : problems) {
            VBox messageBox = new VBox(2);
            messageBox.getStyleClass().add("navdata-card-message");

            HBox textRow = new HBox(6);
            FontIcon severityIcon = SeverityIconCellFactory.getSeverityIcon(message.getSeverity());
            Label text = new Label(message.getMessage());
            text.setWrapText(true);
            text.getStyleClass().add("navdata-card-message-text");

            textRow.getChildren().add(severityIcon);
            HBox.setHgrow(text, Priority.ALWAYS);
            textRow.getChildren().add(text);
            messageBox.getChildren().add(textRow);

            if (!message.getFixes().isEmpty()) {
                HBox fixes = new HBox(4);
                fixes.setAlignment(Pos.CENTER_RIGHT);
                fixes.setMaxWidth(Double.MAX_VALUE);
                fixes.getStyleClass().add("navdata-card-message-fixes");
                message.getFixes().forEach(fix -> {
                    Button button = new Button(fix.label());
                    button.setGraphic(icon(Feather.TOOL));
                    button.setMinWidth(Region.USE_PREF_SIZE);
                    button.getStyleClass().add("navdata-card-fix");
                    button.setOnAction(_ -> FixRunner.run(fix, this));
                    fixes.getChildren().add(button);
                });
                messageBox.getChildren().add(fixes);
            }

            box.getChildren().add(messageBox);
        }
        return box;
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
            Label missing = new Label("absent");
            missing.getStyleClass().add("navdata-status-info");
            row.getChildren().add(missing);
        } else {
            Optional.ofNullable(item.getAiracCycle()).ifPresent(cycle -> {
                Label cycleBadge = new Label("AIRAC " + cycle);
                cycleBadge.getStyleClass().add(STYLE_BADGE);
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
            String size = SizeFormat.humanSize(Files.size(path));
            String modified = DATE_FORMAT.format(
                    Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault()));
            return size + " · " + modified;
        } catch (IOException _) {
            return "?";
        }
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
        EventHandler<MouseEvent> outsidePressHandler = _ -> menu.hide();
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressHandler);
        menu.setOnHidden(_ -> scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, outsidePressHandler));
    }
}
