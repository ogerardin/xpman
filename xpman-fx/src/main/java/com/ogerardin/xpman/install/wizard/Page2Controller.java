package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.GenericInstaller;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import com.ogerardin.xpman.util.jfx.wizard.Validating;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import java.util.List;

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

        GenericInstaller installer = new GenericInstaller(wizard.getXPlane(), source, wizard.getInstallType());
        wizard.setInstaller(installer);

        // perform inspection and display results
        List<InspectionMessage> messages = installer.inspect();
        tableView.setItems(new ObservableListWrapper<>(messages));

        // validate page (and enable 'Next' button) only if there is no message with severity ERROR
        invalidProperty.set(messages.stream().anyMatch(InspectionMessage::isError));
    }

}
