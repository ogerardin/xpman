package com.ogerardin.xpman.panels.aircraft;

import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.panels.aircraft.details.AcfTreeController;
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
import java.net.URL;
import java.nio.file.Path;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(includeFieldNames = false, onlyExplicitlyIncluded = true)
public class UiAircraft {

    @Delegate(excludes = Inspectable.class)
    @ToString.Include
    @EqualsAndHashCode.Include
    protected final Aircraft aircraft;

    @SuppressWarnings("unused")
    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(aircraft.getAcfFile().getFile());
    }

//    @SuppressWarnings("unused")
//    @Label("'Enable Aircraft'")
//    @EnabledIf("! enabled")
//    @OnSuccess("tableView.refresh()")
//    public void enable() {
//        xPlane.getAircraftManager().enableAircraft(aircraft);
//    }

//    @SuppressWarnings("unused")
//    @Label("'Disable Aircraft'")
//    @EnabledIf("enabled")
//    @Confirm("'The entire folder <' + xPlane.baseFolder.relativize(aircraft.acfFile.file.parent) " +
//            "+ '> will be moved to <' + xPlane.baseFolder.relativize(xPlane.aircraftManager.disabledAircraftFolder) " +
//            "+ '>\nThis will impact other aircraft contained in the same folder (if there are).'" +
//            "+ '\n\nPress OK to continue.'")
//    @OnSuccess("tableView.refresh()")
//    public void disable() {
//        xPlane.getAircraftManager().disableAircraft(aircraft);
//    }

    @SuppressWarnings("unused")
    @Label("'Move aircraft to Trash'")
    @Confirm(value = "'The entire folder \"' + xPlane.baseFolder.relativize(aircraft.acfFile.file.parent) + '\" will be moved to the trash. '" +
            "+ 'This will impact all aircraft contained in the same folder.'" +
            "+ '\n\nPress OK to continue.'", alertType = Alert.AlertType.WARNING)
    @OnSuccess("reload()")
    public void moveToTrash() {
        getXPlane().getAircraftManager().moveAircraftToTrash(aircraft);
    }

    @SuppressWarnings("unused")
    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @ForEach(group = "Manuals", iterable = "manuals.entrySet()", itemLabel = "#item.key")
    public void openManual(@Value("#item.value") Path path) {
        Platforms.getCurrent().openFile(path);
    }

    @SuppressWarnings("unused")
    @OnSuccess("displayInspectionResults(#result)")
    public InspectionResult inspect() {
        return aircraft.inspect();
    }

    @SuppressWarnings("unused")
    @Label("'Explore properties'")
    public void details() throws IOException {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/acftree.fxml"));
        Pane pane = loader.load();
        AcfTreeController controller = loader.getController();
        controller.setAircraft(aircraft);
        Stage stage = new Stage();
        stage.setTitle("Aircraft details");
        stage.setScene(new Scene(pane));
//        stage.initOwner(this.tableView.getScene().getWindow());
        stage.show();
    }

}
