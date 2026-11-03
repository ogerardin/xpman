package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateProvider;
import com.ogerardin.xplane.update.*;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Shared update presentation and routing for all add-on providers. */
@UtilityClass
public class UpdateUtil {
    public String actionLabel(List<UpdateOffer> offers) {
        return !offers.isEmpty() && offers.getFirst().action() instanceof UpdateAction.DownloadPage
                ? "Open download page" : "Update…";
    }

    public String actionLabel(Updatable item) { return actionLabel(item.getUpdateStatus().offers()); }

    public String updateTooltip(Updatable item) {
        var status = item.getUpdateStatus();
        if (status.message() != null) return status.message();
        return status.offers().stream().map(o -> o.channel().getLabel() + " update: " + Objects.toString(o.version(), "?"))
                .collect(Collectors.joining("\n"));
    }

    public String availableVersions(Updatable item) {
        return item.getUpdateStatus().offers().stream().map(o -> o.channel().getLabel() + " " + Objects.toString(o.version(), "?"))
                .collect(Collectors.joining(" · "));
    }

    public HBox updateAction(String label, boolean betaAvailable, String tooltip, Runnable action) {
        Button button = new Button(label);
        button.getStyleClass().add("updates-item-button");
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(_ -> action.run());
        HBox graphic = new HBox(6, button);
        graphic.setAlignment(Pos.CENTER_LEFT);
        if (betaAvailable) {
            Label beta = new Label("Beta available");
            beta.getStyleClass().add("update-beta-badge");
            graphic.getChildren().add(beta);
        }
        return graphic;
    }

    public void showUpdates(Updatable item, Runnable reload) {
        if (!item.isUpdateAvailable()) return;
        var provider = item.getUpdateProvider().orElseThrow();
        if (provider instanceof SkunkcraftsUpdateProvider skunkcrafts) {
            new SkunkcraftsUpdateWizard(item.getName(), skunkcrafts, reload).showAndWait();
            return;
        }
        var action = item.getUpdateStatus().offers().getFirst().action();
        Runnable execute = () -> {
            try {
                execute(action, (p, message) -> {}, Platforms.getCurrent()::openUrl,
                        () -> Platform.runLater(reload));
            } catch (Exception e) { Platform.runLater(() -> ErrorDialog.showError(e, null)); }
        };
        if (action instanceof UpdateAction.Direct) AsyncHelper.runAsync(execute);
        else execute.run();
    }

    /** Executes only the advertised capability; browser hand-off never triggers an install refresh. */
    public void execute(UpdateAction action, ProgressListener progress, Consumer<URL> openUrl, Runnable reload) throws IOException {
        switch (action) {
            case UpdateAction.Direct direct -> { direct.apply(progress); reload.run(); }
            case UpdateAction.DownloadPage page -> openUrl.accept(page.url());
        }
    }
}
