package com.ogerardin.xpman.install;

import com.ogerardin.xplane.config.install.Installer;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import lombok.experimental.UtilityClass;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class InstallHelper {

    public void checkAndInstall(Path zipfile, Installer installer, Window owner) {
        List<InspectionMessage> inspectionMessages = installer.inspect(zipfile);

        final Map<Severity, List<InspectionMessage>> messagesBySeverity = inspectionMessages.stream()
                .collect(Collectors.groupingBy(InspectionMessage::getSeverity));

        final List<InspectionMessage> errors = messagesBySeverity.get(Severity.ERROR);

        if ( errors != null) {
            final String message = errors.stream()
                    .map(InspectionMessage::getMessage)
                    .collect(Collectors.joining("\n"));
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.initOwner(owner);
            alert.showAndWait();
            return;
        }

        final List<InspectionMessage> warnings = Optional.ofNullable(messagesBySeverity.get(Severity.WARN)).orElse(Collections.emptyList());
        final List<InspectionMessage> info = Optional.ofNullable(messagesBySeverity.get(Severity.INFO)).orElse(Collections.emptyList());

        final String message = Stream.concat(warnings.stream(), info.stream())
                .map(InspectionMessage::getMessage)
                .collect(Collectors.joining("\n"));

        Alert alert = new Alert((! warnings.isEmpty()) ? Alert.AlertType.WARNING : Alert.AlertType.CONFIRMATION,
                message);
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        alert.initOwner(owner);
        if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        installer.install(zipfile);
    }
}
