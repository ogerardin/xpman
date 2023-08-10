package com.ogerardin.xplane.install.inspections.custom;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

public enum NavigraphCycleVersion implements Inspection<Archive> {

    INSTANCE;

    @SneakyThrows
    @Override
    public InspectionResult inspect(@NonNull Archive zip) {
        try {
            String text = zip.getAsText(Paths.get("cycle_info.txt"));
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.INFO).message(text)
                    .build());
        } catch (FileNotFoundException ignored) {
            return InspectionResult.empty();
        }
    }
}
