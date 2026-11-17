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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility for trimming platform-specific binaries from a plugin folder.
 * Handles both .xpl plugin files (detected by content) and native libraries
 * (.dylib, .dll, .so detected by extension).
 * Used by both {@link Plugin} (for global plugins) and {@link com.ogerardin.xplane.aircraft.Aircraft}
 * (for aircraft-local plugins).
 */
@Slf4j
@UtilityClass
public class XplTrimmer {

    /**
     * Map of native library file extensions to their target platforms.
     * These are detected by extension, not by content inspection.
     */
    private static final Map<String, Platforms> NATIVE_LIBRARY_EXTENSIONS;
    static {
        Map<String, Platforms> map = new LinkedHashMap<>();
        map.put(".dylib", Platforms.MAC);
        map.put(".dll", Platforms.WINDOWS);
        map.put(".so", Platforms.LINUX);
        NATIVE_LIBRARY_EXTENSIONS = Map.copyOf(map);
    }

    /**
     * Returns all platform-specific binary files under the given folder.
     * Includes .xpl files, .dylib (macOS), .dll (Windows), and .so (Linux) files.
     */
    public List<Path> allPlatformBinaries(Path folder) {
        try (Stream<Path> paths = Files.walk(folder)) {
            return paths.filter(Files::isRegularFile)
                    .filter(XplTrimmer::isPlatformBinary)
                    .toList();
        } catch (IOException e) {
            log.warn("Failed to scan for platform binaries in {}", folder, e);
            return List.of();
        }
    }

    /**
     * Checks if a file is a platform-specific binary (.xpl, .dylib, .dll, or .so).
     */
    private boolean isPlatformBinary(Path file) {
        String filename = file.getFileName().toString().toLowerCase();
        return filename.endsWith(".xpl") ||
               filename.endsWith(".dylib") ||
               filename.endsWith(".dll") ||
               filename.endsWith(".so");
    }

    /**
     * Returns the platform-specific binary files that are NOT applicable for the given X-Plane variant.
     * Includes .xpl files (detected by content) and native libraries (detected by extension).
     */
    public List<Path> nonApplicablePlatformBinaries(Path folder, XPlaneVariant variant) {
        Platform targetPlatform = variant.getPlatform();
        if (targetPlatform == Platforms.UNKNOWN) return List.of();
        return allPlatformBinaries(folder).stream()
                .filter(file -> !isApplicableToPlatform(file, targetPlatform))
                .toList();
    }

    /**
     * Checks if a binary file is applicable to the target platform.
     * For .xpl files, checks by content inspection.
     * For native libraries, checks by file extension.
     * Returns true if the file is not a recognized platform binary (unknown format).
     */
    private boolean isApplicableToPlatform(Path file, Platform targetPlatform) {
        String filename = file.getFileName().toString().toLowerCase();

        // Native libraries: check by extension - definitive platform identification
        for (Map.Entry<String, Platforms> entry : NATIVE_LIBRARY_EXTENSIONS.entrySet()) {
            if (filename.endsWith(entry.getKey())) {
                return entry.getValue() == targetPlatform;
            }
        }

        // .xpl files: check by content inspection
        if (filename.endsWith(".xpl")) {
            // Check if this is a known platform binary by content
            boolean isKnownPlatformBinary = java.util.Arrays.stream(Platforms.values())
                    .filter(platform -> platform != Platforms.UNKNOWN)
                    .anyMatch(platform -> platform.isPlatformBinary(file));
            
            // If not a recognized platform binary, leave it alone (don't trim unknown formats)
            if (!isKnownPlatformBinary) {
                return true;
            }
            
            // It's a known platform binary - check if it matches the target platform
            return targetPlatform.isPlatformBinary(file);
        }

        return true; // Should not reach here if isPlatformBinary() was called first
    }

    /**
     * Trims the plugin by removing all platform-specific binary files that are not applicable for the given variant.
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
        List<Path> nonApplicable = nonApplicablePlatformBinaries(folder, variant);
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
