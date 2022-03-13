package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.aircrafts.Livery;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.panels.menu.Confirm;
import com.ogerardin.xpman.util.jfx.panels.menu.Label;
import com.ogerardin.xpman.util.jfx.panels.menu.OnSuccess;
import javafx.scene.control.Alert;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.file.Path;

@ToString(includeFieldNames = false, onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false)
public class UiLivery extends UiAircraft {

    @ToString.Include
    public final Livery livery;

    public UiLivery(XPlane xPlane, Aircraft aircraft, Livery livery) {
        super(aircraft, xPlane);
        this.livery = livery;
    }

    @Override
    public String getName() {
        return livery.getName();
    }

    public Path getThumb() {
        return livery.getThumb();
    }

    public Path getIcon() {
        return livery.getIcon();
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getLatestVersion() {
        return null;
    }

    @SuppressWarnings("unused")
    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(aircraft.getAcfFile().getFile().resolve(livery.getFolder()).normalize());
    }

    @SuppressWarnings("unused")
    @Label("'Move livery to Trash'")
    @Confirm(value = "'The entire folder \"' + xPlane.baseFolder.relativize(aircraft.liveriesFolder.resolve(livery.path)) + '\" will be moved to the trash. '" +
            "+ 'This will impact all aircrafts contained in the same aircraft folder.'" +
            "+ '\n\nPress OK to continue.'", alertType = Alert.AlertType.WARNING)
    @OnSuccess("reload()")
    public void moveToTrash() {
        xPlane.getAircraftManager().moveLiveryToTrash(livery);
    }


}
