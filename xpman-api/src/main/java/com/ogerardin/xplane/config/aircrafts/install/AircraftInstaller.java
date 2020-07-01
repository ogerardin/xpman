package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.util.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@UtilityClass
@Slf4j
public class AircraftInstaller {

    public CheckResult checkZip(Path zipFile) {
        //TODO: check if not overwriting anything!
        List<String> acfFiles = new ArrayList<>();
        try (ZipFile zip = new ZipFile(zipFile.toFile(), ZipFile.OPEN_READ)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
//           enum log.debug("ZipEntry: {}", zipEntry);
                String entryName = zipEntry.getName();
                if (entryName.endsWith(".acf")) {
                    acfFiles.add(entryName);
                    log.debug("Found acf file: {}", entryName);
                }
            }
        } catch (IOException e) {
            return new CheckResult(false, "File is not a ZIP archive");
        }
        if (acfFiles.isEmpty()) {
            return new CheckResult(false, "No ACF file found in archive");
        }
        return new CheckResult(true, "Found the following ACF files in archive: " + acfFiles);
    }

    public void installZip(XPlaneInstance xPlaneInstance, Path zipFile) throws IOException {
        FileUtils.unzip(zipFile, xPlaneInstance.getAircraftManager().getAircraftFolder());
    }
}
