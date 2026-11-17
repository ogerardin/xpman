package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for trimming platform-specific .xpl binaries from a plugin folder.
 * Used by both {@link Plugin} (for global plugins) and {@link com.ogerardin.xplane.aircraft.Aircraft}
 * (for aircraft-local plugins).
 */
@Slf4j
@UtilityClass
public class XplTrimmer {

    /**
     * Returns all .xpl files under the given folder.
     */
    public List<Path> allXplFiles(Path folder) {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xpl"))
                    .toList();
        } catch (IOException e) {
            log.warn("Failed to scan for .xpl files in {}", folder, e);
            return List.of();
        }
    }

    /**
     * Returns the .xpl files that are NOT applicable for the given X-Plane variant.
     * Non-applicable files are those whose binary format is for a different platform.
     */
    public List<Path> nonApplicableXplFiles(Path folder, XPlaneVariant variant) {
        Platform targetPlatform = variant.getPlatform();
        if (targetPlatform == Platforms.UNKNOWN) return List.of();
        return allXplFiles(folder).stream()
                .filter(xpl -> !targetPlatform.isPlatformBinary(xpl)
                        && isKnownOtherPlatformBinary(xpl, targetPlatform))
                .toList();
    }

    private static boolean isKnownOtherPlatformBinary(Path binary, Platform targetPlatform) {
        return java.util.Arrays.stream(Platforms.values())
                .filter(platform -> platform != Platforms.UNKNOWN && platform != targetPlatform)
                .anyMatch(platform -> platform.isPlatformBinary(binary));
    }

    /**
     * Trims the plugin by removing all .xpl files that are not applicable for the given variant.
     * Files are moved to trash. For Skunkcrafts-managed plugins, the removed files are first
     * added to {@code skunkcrafts_updater_ignore.txt} so the updater does not restore them.
     *
     * @param folder the folder to trim
     * @param variant the target X-Plane variant
     * @param skunkcraftsBaseFolder the folder containing {@code skunkcrafts_updater_ignore.txt},
     *                              or {@code null} if not Skunkcrafts-managed
     * @return the list of files that were removed
     */
    public List<Path> trim(Path folder, XPlaneVariant variant, Path skunkcraftsBaseFolder) throws IOException {
        List<Path> nonApplicable = nonApplicableXplFiles(folder, variant);
        if (nonApplicable.isEmpty()) return List.of();

        // For Skunkcrafts-managed folders, record exclusions before deleting so the updater
        // does not offer to restore the trimmed binaries; abort if the ignore file cannot be saved
        if (skunkcraftsBaseFolder != null) {
            addToIgnoreFile(nonApplicable, folder, skunkcraftsBaseFolder);
        }

        List<Path> removed = new ArrayList<>();
        for (Path file : nonApplicable) {
            moveToTrash(file);
            removed.add(file);
            log.debug("Removed non-applicable .xpl file: {}", file);

            // Clean up empty parent directories up to the folder boundary
            cleanupEmptyDirectories(file.getParent(), folder);
        }

        return removed;
    }

    private void moveToTrash(Path path) throws IOException {
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(path.toFile());
    }

    /**
     * Appends the given relative paths to the Skunkcrafts ignore file ({@code skunkcrafts_updater_ignore.txt}),
     * preserving existing entries and comments. Does nothing if all paths are already present.
     */
    private void addToIgnoreFile(List<Path> files, Path baseFolder, Path skunkcraftsBaseFolder) throws IOException {
        Path ignoreFile = skunkcraftsBaseFolder.resolve("skunkcrafts_updater_ignore.txt");

        // Normalize to forward-slash relative paths as the updater expects
        Set<String> newEntries = files.stream()
                .map(file -> baseFolder.relativize(file).toString().replace("\\", "/"))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        String existing = Files.exists(ignoreFile) ? Files.readString(ignoreFile) : "";
        for (String line : existing.split("\\R")) {
            newEntries.remove(line.trim()); // already present
        }
        if (newEntries.isEmpty()) return;

        // Append after existing content, preserving comments and entries
        String content = existing.isEmpty() ? String.join("\n", newEntries)
                : existing.endsWith("\n") || existing.endsWith("\r")
                ? existing + String.join("\n", newEntries)
                : existing + "\n" + String.join("\n", newEntries);
        Files.writeString(ignoreFile, content);
    }

    /**
     * Deletes empty directories starting from the given path up to (but not including) the boundary folder.
     */
    private void cleanupEmptyDirectories(Path directory, Path boundary) throws IOException {
        // Walk up the directory tree, deleting empty directories until we reach the boundary
        while (directory != null &&
                directory.startsWith(boundary) &&
                !directory.equals(boundary)) {

            // Check if directory is empty
            try (Stream<Path> contents = Files.list(directory)) {
                if (contents.findAny().isPresent()) {
                    // Directory not empty, stop cleanup
                    break;
                }
            }

            // Directory is empty, delete it
            Files.delete(directory);
            log.debug("Deleted empty directory: {}", directory);

            // Move up to parent
            directory = directory.getParent();
        }
    }
}
