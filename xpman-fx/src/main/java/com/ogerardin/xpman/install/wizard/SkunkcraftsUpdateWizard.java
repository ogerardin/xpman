package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.update.UpdateChannel;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateProvider;
import com.ogerardin.xpman.util.jfx.WindowPositionHelper;
import com.ogerardin.xpman.util.jfx.wizard.Wizard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import lombok.Getter;

/**
 * Wizard for performing Skunkcrafts updates.
 * Shows a summary page followed by a progress page.
 * Carries the selected channel; page 1 lets the user switch when both channels have updates.
 */
public class SkunkcraftsUpdateWizard extends Wizard {

    @Getter
    private final SkunkcraftsUpdateProvider updatable;

    @Getter private final String addonName;
    @Getter private final Runnable onUpdated;

    private final ObjectProperty<UpdateChannel> channel =
            new SimpleObjectProperty<>(UpdateChannel.RELEASE);

    public SkunkcraftsUpdateWizard(String addonName, SkunkcraftsUpdateProvider updatable, Runnable onUpdated) {
        super("Skunkcrafts Update");
        this.updatable = updatable;
        this.addonName = addonName;
        this.onUpdated = onUpdated;
        setFlow(
            "/fxml/install_wizard/skunkcrafts_page1.fxml",
            "/fxml/install_wizard/skunkcrafts_page2.fxml"
        );
        WindowPositionHelper.setupSkunkcraftsWizard(this);
        getDialog().setOnShown(_ -> {
            Stage stage = (Stage) getDialog().getDialogPane().getScene().getWindow();
            stage.setMinWidth(400);
            stage.setMinHeight(300);
            // Saved sizes can predate the channel section; never restore below the usable minimum.
            stage.setWidth(Math.max(stage.getWidth(), stage.getMinWidth()));
            stage.setHeight(Math.max(stage.getHeight(), stage.getMinHeight()));
        });
    }

    public UpdateChannel getChannel() {
        return channel.get();
    }

    public void setChannel(UpdateChannel channel) {
        this.channel.set(channel);
    }

    public ObjectProperty<UpdateChannel> channelProperty() {
        return channel;
    }
}
