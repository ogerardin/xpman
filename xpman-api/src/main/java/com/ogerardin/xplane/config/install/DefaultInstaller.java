package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.util.FileUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Data
public class DefaultInstaller implements Installer {

    final Path targetFolder;
    final String suffix;

    public List<InspectionMessage> apply(Path zipFile) {
        Inspections<InstallableZip> inspections = getInspections();
        var installableZip = new InstallableZip(zipFile);
        return inspections.apply(installableZip);
    }

    protected Inspections<InstallableZip> getInspections() {
        return Inspections.of(
                new CheckIsValidZip(),
                new CheckHasSingleRootFolder(),
                new CheckHasFilesWithType2(suffix),
                new CheckDoesNotOverwriteFiles(targetFolder)
        );
    }

    @SneakyThrows
    public void install(Path zipFile) {
        FileUtils.unzip(zipFile, targetFolder);
    }
}
