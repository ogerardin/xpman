package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

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

    public static long getFolderSize(Path path) throws IOException {
        if (! Files.isDirectory(path)) {
            throw new IllegalArgumentException(path + " is not a directory");
        }
        return Files.walk(path)
                .filter(Files::isRegularFile)
                .mapToLong(p -> p.toFile().length())
                .sum();
    }
}
