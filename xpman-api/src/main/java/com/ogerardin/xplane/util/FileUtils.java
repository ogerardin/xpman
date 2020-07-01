package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@UtilityClass
public class FileUtils {

    public List<Path> findFiles(Path startFolder, Predicate<Path> predicate) throws IOException {
        if (! Files.exists(startFolder)) {
            return Collections.emptyList();
        }
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(startFolder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (predicate.test(file)) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return files;
    }

    public void unzip(Path zipFile, Path targetFolder) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile(), ZipFile.OPEN_READ)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String entryName = zipEntry.getName();
                Path target = targetFolder.resolve(entryName);
                if (! target.startsWith(targetFolder)) {
                    throw new IOException("Entry outside of target directory: " + target);
                }
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    InputStream inputStream = zip.getInputStream(zipEntry);
                    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public Stream<Path> zipPaths(Path zipFile) throws IOException {
        try (ZipFile zip = new ZipFile(zipFile.toFile(), ZipFile.OPEN_READ)) {
            return Collections.list(zip.entries()).stream()
                    .map(ZipEntry::getName)
                    .map(Paths::get);
        }
    }
}
