package com.ogerardin.xplane.install.inspections.custom;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

/** Inspection that extracts and displays the Navigraph cycle info from an archive. */
public enum NavigraphCycleVersion implements Inspection<Archive> {

    INSTANCE;

    @SneakyThrows
    @Override
    public InspectionResult inspect(@NonNull Archive zip) {
        try {
            String text = zip.getAsText(Paths.get("cycle_info.txt"));
            return InspectionResult.of(InspectionMessage.info(text));
        } catch (FileNotFoundException _) {
            return InspectionResult.empty();
        }
    }
}
