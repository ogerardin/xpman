package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannel;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
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
    private final SkunkcraftsUpdatable updatable;

    private final ObjectProperty<SkunkcraftsChannel> channel =
            new SimpleObjectProperty<>(SkunkcraftsChannel.STABLE);

    public SkunkcraftsUpdateWizard(SkunkcraftsUpdatable updatable) {
        this(updatable, SkunkcraftsChannel.STABLE);
    }

    public SkunkcraftsUpdateWizard(SkunkcraftsUpdatable updatable, SkunkcraftsChannel requestedChannel) {
        super("Skunkcrafts Update");
        this.updatable = updatable;
        this.channel.set(requestedChannel);
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

    public SkunkcraftsChannel getChannel() {
        return channel.get();
    }

    public void setChannel(SkunkcraftsChannel channel) {
        this.channel.set(channel);
    }

    public ObjectProperty<SkunkcraftsChannel> channelProperty() {
        return channel;
    }
}
