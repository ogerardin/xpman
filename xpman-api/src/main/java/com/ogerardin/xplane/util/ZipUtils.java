package com.ogerardin.xplane.util;

import com.google.common.io.CharStreams;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
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
                // we ignore directory entries; any non-existing intermediate directories will be created before
                // extracting the file
                if (! zipEntry.isDirectory()) {
                    log.debug("Extracting {} as {}", entryName, target);
                    Files.createDirectories(target.getParent());
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

    public static String getAsText(Path zipFile, Path path) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile(), Charset.forName("CP437"))) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                if (! Paths.get(entryName).equals(path)) {
                    continue;
                }
                InputStream inputStream = zip.getInputStream(zipEntry);
                try (Reader reader = new InputStreamReader(inputStream)) {
                    return CharStreams.toString(reader);
                }
            }
        }
        throw new FileNotFoundException(path.toString());
    }

    public interface ProgressListener {
        void extracting(String filename);
    }


}
