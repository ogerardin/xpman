package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.config.install.Installer;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.util.jfx.wizard.FlowListener;
import com.ogerardin.xpman.util.jfx.wizard.Validating;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.Wizard;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class Page2Controller implements Validating, FlowListener {

    private BooleanProperty invalidProperty = new SimpleBooleanProperty();

    @Override
    public final ReadOnlyBooleanProperty invalidProperty() {
        return invalidProperty;
    }

    @FXML
    private TableView<InspectionMessage> tableView;

    @NonNull
    private final Installer installer;

    public Page2Controller(InstallWizard installWizard) {
        this.installer = installWizard.getInstaller();
    }


    @Override
    public void onEnteringPage(Wizard wizard) {
        String sourcePath = (String) wizard.getSettings().get("sourcePathField");
        Path source = Paths.get(sourcePath);
        List<InspectionMessage> messages = installer.apply(source);
        tableView.setItems(new ObservableListWrapper<>(messages));
        invalidProperty.set(messages.stream().anyMatch(InspectionMessage::isError));
    }

}
