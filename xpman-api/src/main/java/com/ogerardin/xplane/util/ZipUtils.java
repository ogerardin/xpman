package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@UtilityClass
@Slf4j
public class ZipUtils {

    public void unzip(Path zipFile, Path targetFolder) throws IOException {
        unzip(zipFile, targetFolder, null);
    }

    public void unzip(Path zipFile, Path targetFolder, ProgressListener progressListener) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile(), Charset.forName("CP437"))) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                if (progressListener != null) {
                    progressListener.extracting(entryName);
                }
                Path target = targetFolder.resolve(entryName);
                if (! target.startsWith(targetFolder)) {
                    throw new IOException("Entry outside of target directory: " + target);
                }
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    log.debug("Extracting {} as {}", entryName, target);
                    InputStream inputStream = zip.getInputStream(zipEntry);
                    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public Stream<Path> zipPaths(Path zipFile) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile(), Charset.forName("CP437"))) {
            return Collections.list(zip.entries()).stream()
                    .map(ZipEntry::getName)
                    .map(Paths::get);
        }
    }

    public interface ProgressListener {
        void extracting(String filename);
    }
}
