package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.AbstractFileHeader;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@UtilityClass
@Slf4j
public class ZipUtils {

    public void unzip(Path zipFile, Path targetFolder) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            zip.extractAll(targetFolder.toString());
        }
    }

    public void unzip(Path zipFile, Path targetFolder, ProgressListener progressListener) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            ProgressMonitor progressMonitor = zip.getProgressMonitor();
            zip.setRunInThread(true);
            zip.extractAll(targetFolder.toString());

            while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
                String fileName = progressMonitor.getFileName();
                int percentDone = progressMonitor.getPercentDone();
                progressListener.progress(percentDone / 100.0, "Extracting " + fileName);

                try {
                    //noinspection BusyWait
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }

            switch (progressMonitor.getResult()) {
                case ERROR -> throw new IOException("Failed to extract zip " + zipFile, progressMonitor.getException());
                case SUCCESS -> progressListener.progress(1.00, "Done!");
            }
        }
    }

    public Stream<Path> zipPaths(Path zipFile) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            return zip.getFileHeaders().stream()
                    .map(AbstractFileHeader::getFileName)
                    .map(Paths::get);

        }
    }

    public static String getAsText(Path zipFile, Path path) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile())) {
            for (FileHeader fileHeader : zip.getFileHeaders()) {
                String entryName = fileHeader.getFileName();
                if (! Paths.get(entryName).equals(path)) {
                    continue;
                }
                InputStream inputStream = zip.getInputStream(fileHeader);
                try (Reader reader = new InputStreamReader(inputStream)) {
                    return IOUtils.toString(reader);
                }
            }
        }
        throw new FileNotFoundException(path.toString());
    }

}
