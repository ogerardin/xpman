package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.GenericInstaller;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import com.ogerardin.xpman.util.jfx.wizard.Validating;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Wizard page 2 controller: performs inspections on the archive and displays result.
 * Valid (=Next enabled) only if no inspection result is of type ERROR.
 */
@Slf4j
@RequiredArgsConstructor
public class Page2Controller implements Validating, PageListener {

    @NonNull
    private final InstallWizard wizard;

    private final BooleanProperty invalidProperty = new SimpleBooleanProperty();

    @FXML
    private TableColumn<InspectionMessage, Severity> severityColumn;

    @Override
    public final ReadOnlyBooleanProperty invalidProperty() {
        return invalidProperty;
    }

    @FXML
    private TableView<InspectionMessage> tableView;

    @FXML
    public void initialize() {
        tableView.setPlaceholder(new Label("No message to display"));
    }

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        // get the source file
        String sourcePath = (String) wizard.getSettings().get("sourcePathField");
        Path source = Paths.get(sourcePath);

        // instanciate a GenericInstaller for the file selected in the previous page
        GenericInstaller installer = new GenericInstaller(
                wizard.getXPlane(),
                ArchiveInstallSource.ofZip(source),
                wizard.getInstallType());
        wizard.setInstaller(installer);

        // perform inspection and display results
        InspectionResult result = installer.inspect();
        tableView.setItems(FXCollections.observableList(result.getMessages()));

        // validate page (and enable 'Next' button) only if there is no message with severity ERROR
        invalidProperty.set(result.stream().anyMatch(InspectionMessage::isError));
    }

}
