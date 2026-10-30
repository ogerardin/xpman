package com.ogerardin.xpman.panels.aircraft;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.panels.aircraft.details.AcfTreeController;
import com.ogerardin.xpman.util.jfx.PropertiesStage;
import com.ogerardin.xpman.util.jfx.menu.annotation.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/** UI wrapper around {@link Aircraft} that exposes context-menu actions (reveal, uninstall, inspect, properties, etc.) via annotations. */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = false, onlyExplicitlyIncluded = true)
public class UiAircraft {

    @Delegate(excludes = {Inspectable.class, Uninstallable.class})
    @ToString.Include
    @EqualsAndHashCode.Include
    protected final Aircraft aircraft;

    @SuppressWarnings("unused")
    @Order(10)
    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(aircraft.getAcfFile().getFile());
    }

    @SuppressWarnings("unused")
    @Order(15)
    @Label("'Uninstall this variant'")
    @EnabledIf("isMultiVariant")
    @Confirm(value = """
            'Only variant "' + aircraft.name + '" (' + aircraft.acfFile.file.fileName + ') will be uninstalled. ' +
            'Files shared with the other variants of this aircraft, including liveries, will be preserved.

            Press OK to continue.'
            """, alertType = Alert.AlertType.WARNING)
    public void uninstallVariant() {
        getXPlane().getAircraftManager().uninstallAircraftVariant(aircraft);
    }

    /** True when other variants visible in the aircraft panel share this aircraft's folder.
     * Named with the {@code is} prefix so the menu introspection skips it (not a menu action). */
    public boolean isMultiVariant() {
        return getXPlane().getAircraftManager()
                .getAircraftByFolder(aircraft.getAcfFile().getFile().getParent())
                .size() > 1;
    }

    @SuppressWarnings("unused")
    @Order(20)
    @Label("'Uninstall aircraft'")
    @Confirm(value = """
            'The entire folder "' + xPlane.baseFolder.relativize(aircraft.acfFile.file.parent) + '" will be uninstalled. ' +
            'This will impact the following aircraft: ' + xPlane.aircraftManager.getAircraftByFolder(aircraft.acfFile.file.parent) +
            '

Press OK to continue.'
            """, alertType = Alert.AlertType.WARNING)
    public void uninstall() {
        getXPlane().getAircraftManager().uninstallAircraft(aircraft);
    }

    @SuppressWarnings("unused")
    @Order(30)
    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @Order(40)
    @ForEach(group = "Manuals", iterable = "manuals.entrySet()", itemLabel = "#item.key")
    public void openManual(@Value("#item.value") Path path) {
        Platforms.getCurrent().openFile(path);
    }

    @SuppressWarnings("unused")
    @Order(50)
    @OnSuccess("displayInspectionResults(#result)")
    public InspectionResult inspect() {
        return aircraft.inspect();
    }

    @SuppressWarnings("unused")
    @Order(60)
    @Label("'Explore ACF property tree'")
    public void details() throws IOException {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/acftree.fxml"));
        Pane pane = loader.load();
        AcfTreeController controller = loader.getController();
        controller.setAircraft(aircraft);
        Stage stage = new Stage();
        stage.setTitle("Aircraft details");
        stage.setScene(new Scene(pane));
        stage.show();
    }

    @SuppressWarnings("unused")
    @Order(70)
    @Label("'Update…'")
    @EnabledIf("anySkunkcraftsUpdateAvailable")
    @OnSuccess("reload()")
    public void skunkcraftsUpdate() {
        new com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard(aircraft).showAndWait();
    }

    @SuppressWarnings("unused")
    @Order(80)
    @Label("'Skunkcrafts: Locked by developer'")
    @EnabledIf("skunkcraftsLocked")
    public void skunkcraftsLocked() {
        new Alert(Alert.AlertType.INFORMATION,
                "This aircraft is currently locked by the developer. " +
                "The developer may be uploading files. Please try again later.")
                .showAndWait();
    }

    @SuppressWarnings("unused")
    @Order(90)
    @OnSuccess("reload()")
    public void properties() {
        Map<String, String> editableProperties = new HashMap<>(Map.of(
                "name", "acf/_name",
                "callsign", "acf/_callsign",
                "tailNumber", "acf/_tailnum",
                "icaoCode", "acf/_ICAO",
                "description", "acf/_descrip",
                "author", "acf/_author",
                "studio", "acf/_studio",
                "notes", "acf/_notes"
        ));
        if (!aircraft.isAcfNameEditable()) {
            editableProperties.remove("name");
        }
        PropertiesStage.show("aircraft", getName(), aircraft, editableProperties,
                changes -> aircraft.getAcfFile().save(changes)
        );
    }
}
