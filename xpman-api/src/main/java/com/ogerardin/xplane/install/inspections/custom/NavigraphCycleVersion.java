package com.ogerardin.xplane.install.inspections.custom;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallableArchive;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Data
public class NavigraphCycleVersion implements Inspection<InstallableArchive> {

    @SneakyThrows
    @Override
    public List<InspectionMessage> inspect(InstallableArchive zip) {
        try {
            String text = zip.getAsText(Paths.get("cycle_info.txt"));
            return Collections.singletonList(InspectionMessage.builder()
                    .severity(Severity.INFO).message(text)
                    .build());
        } catch (FileNotFoundException ignored) {
            return Collections.emptyList();
        }
    }
}
