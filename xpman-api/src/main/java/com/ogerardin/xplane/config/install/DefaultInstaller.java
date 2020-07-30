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

    public List<InspectionMessage> inspect(Path zipFile) {
        Inspections<InstallableZip> inspections = getInspections();
        var installableZip = new InstallableZip(zipFile);
        return inspections.inspect(installableZip);
    }

    protected Inspections<InstallableZip> getInspections() {
        return Inspections.of(
                    new CheckValidZip(),
                    new CheckHasFilesWithType(suffix),
                    new CheckDoesNotOverwriteFiles(targetFolder),
                    new CheckSingleRootFolder()
            );
    }

    @SneakyThrows
    public void install(Path zipFile) {
        FileUtils.unzip(zipFile, targetFolder);
    }
}
