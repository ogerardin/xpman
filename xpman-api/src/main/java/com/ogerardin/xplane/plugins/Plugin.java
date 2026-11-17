package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneObject;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.ArchitectureMismatchInspection;
import com.ogerardin.xplane.inspection.impl.UpdateAvailableInspection;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateProvider;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.update.*;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An X-Plane plugin, identified by its {@code .xpl} file.
 *
 * <p>Provides access to plugin metadata (name, description, version), enable/disable state
 * (derived from whether the plugin folder resides under the active or disabled plugins
 * directory), quarantine status, manuals, and links. Implements {@link Inspectable} for
 * update-available checks, {@link Uninstallable} to move the plugin folder to trash, and
 * {@link Updatable} for provider-based updates.</p>
 *
 * <p>For Skunkcrafts-managed plugins, the updater config provides the version; otherwise
 * it is extracted from the {@code .xpl} binary by the current platform. The concrete subclass
 * may be refined at runtime via {@link com.ogerardin.xplane.util.IntrospectionHelper}
 * (e.g. FlyWithLua).</p>
 */
@Slf4j
@Getter
public class Plugin extends XPlaneObject implements Inspectable, Uninstallable, Updatable {

    private Path xplFile;

    private final String name;

    private final String desc;

    @SuppressWarnings("unused")
    public boolean getSystem()
    {
        return false;
    }

    @Getter(lazy = true)
    private final String version = computeVersion();

    public Plugin(XPlane xPlane, Path xplFile, String name, String desc) {
        super(xPlane);
        this.xplFile = xplFile;
        this.name = name;
        this.desc = desc;
    }

    @SuppressWarnings("unused")
    public Plugin(XPlane xPlane, Path xplFile) {
        this(xPlane, xplFile, computeName(xplFile), null);
    }

    private static String computeName(Path xplFile) {
        Path folder = getBaseFolder(xplFile);
        return folder.getFileName().toString();
    }

    protected static Path getBaseFolder(Path xplFile) {
        Path folder = xplFile.getParent();
        String folderName = folder.getFileName().toString();
        if (folderName.endsWith("64") || folderName.endsWith("32")) {
            folder = folder.getParent();
        }
        return folder;
    }

    public Path getBaseFolder() {
        return getBaseFolder(xplFile);
    }

    @Getter(lazy = true)
    private final Optional<UpdateProvider> updateProvider = loadUpdateProvider();

    /** Creates the cached provider; subclasses may supply a fallback when no protocol is assigned. */
    protected Optional<UpdateProvider> loadUpdateProvider() {
        return supportsSkunkcrafts()
                ? SkunkcraftsUpdateProvider.discover(this::getBaseFolder, this::getVersion, this::isEnabled)
                : Optional.empty();
    }

    protected boolean supportsSkunkcrafts() { return true; }

    private String computeVersion() {
        if (supportsSkunkcrafts()) {
            SkunkcraftsConfig config = SkunkcraftsUpdater.findConfig(getBaseFolder());
            if (config != null && config.version() != null) {
                return config.version();
            }
        }
        return Platforms.getCurrent().extractPluginVersion(xplFile);
    }

    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }

    @SuppressWarnings("unused")
    @Getter(lazy = true)
    private final Map<String, Path> manuals = computeManuals();

    @SneakyThrows
    private Map<String, Path> computeManuals() {
        Map<String, Path> foundManuals = new HashMap<>();
        try (Stream<Path> paths = Files.walk(getBaseFolder())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pdf"))
                    .forEach(p -> foundManuals.put(p.getFileName().toString(), p));
        }
        return foundManuals;
    }

    public boolean isEnabled() {
        return !getBaseFolder().startsWith(getXPlane().getPaths().disabledPlugins());
    }

    public void setEnabled(boolean enabled) throws IOException {
        if (enabled == isEnabled()) return;
        Path source = getBaseFolder();
        Path targetBase = enabled ? getXPlane().getPaths().plugins() : getXPlane().getPaths().disabledPlugins();
        Path target = targetBase.resolve(source.getFileName());
        Files.createDirectories(targetBase);
        if (Files.exists(target)) {
            com.sun.jna.platform.FileUtils.getInstance().moveToTrash(target.toFile());
        }
        Files.move(source, target);
        // keep this object truthful for consumers (enabled state, reveal, uninstall)
        // until the reload replaces it with a fresh instance
        this.xplFile = target.resolve(source.relativize(xplFile));
    }

    // referenced by com.ogerardin.xpman.panels.plugins.UiPlugin.removeQuarantine
    @SuppressWarnings("unused")
    public boolean isQuarantined() {
        return Platforms.getCurrent().isQuarantined(getBaseFolder());
    }

    public String getUninstallWarningDetails() {
        return "";
    }

    /** Moves the given path to the trash; overridable as a seam for tests (Sges idiom). */
    protected void moveToTrash(Path path) throws IOException {
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(path.toFile());
    }

    @Override
    public InspectionResult inspect() {
        return isEnabled()
                ? UpdateAvailableInspection.INSTANCE.inspect(this)
                        .append(ArchitectureMismatchInspection.INSTANCE.inspect(this))
                : InspectionResult.empty();
    }

    @Override
    public void uninstall() throws IOException {
        moveToTrash(getBaseFolder());
    }

    /**
     * Returns all .xpl files under the plugin's base folder.
     */
    @SneakyThrows
    public java.util.List<Path> getAllXplFiles() {
        try (Stream<Path> paths = Files.walk(getBaseFolder())) {
            return paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xpl"))
                    .toList();
        }
    }

    /**
     * Returns the .xpl files that are applicable for the given X-Plane variant.
     * Applicable files are those whose binary format matches the variant's platform.
     */
    @SneakyThrows
    public java.util.List<Path> getApplicableXplFiles(XPlaneVariant variant) {
        Platform targetPlatform = variant.getPlatform();
        if (targetPlatform == Platforms.UNKNOWN) return getAllXplFiles();
        return getAllXplFiles().stream()
                .filter(xpl -> targetPlatform.isPlatformBinary(xpl)
                        || !isKnownOtherPlatformBinary(xpl, targetPlatform))
                .toList();
    }

    /**
     * Returns the .xpl files that are NOT applicable for the given X-Plane variant.
     * Non-applicable files are those whose binary format is for a different platform.
     */
    @SneakyThrows
    public java.util.List<Path> getNonApplicableXplFiles(XPlaneVariant variant) {
        Platform targetPlatform = variant.getPlatform();
        if (targetPlatform == Platforms.UNKNOWN) return java.util.List.of();
        return getAllXplFiles().stream()
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
     * Returns true if a Skunkcrafts updater config exists in the plugin's base folder.
     */
    @SneakyThrows
    public boolean isSkunkcraftsManaged() {
        return supportsSkunkcrafts()
                && com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater.findConfig(getBaseFolder()) != null;
    }

    /**
     * Appends the given relative paths to the Skunkcrafts ignore file ({@code skunkcrafts_updater_ignore.txt}),
     * preserving existing entries and comments. Does nothing if all paths are already present.
     */
    void addToSkunkcraftsIgnore(java.util.List<Path> files) throws IOException {
        Path ignoreFile = getBaseFolder().resolve("skunkcrafts_updater_ignore.txt");

        // Normalize to forward-slash relative paths as the updater expects
        java.util.Set<String> newEntries = files.stream()
                .map(file -> getBaseFolder().relativize(file).toString().replace("\\", "/"))
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

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
     * Trims the plugin by removing all .xpl files that are not applicable
     * for the given X-Plane variant. Files are moved to trash.
     * For Skunkcrafts-managed plugins, the removed files are first added to
     * {@code skunkcrafts_updater_ignore.txt} so the updater does not restore them.
     *
     * @param variant the target X-Plane variant
     * @return the list of files that were removed
     */
    @SneakyThrows
    public java.util.List<Path> trimToVariant(XPlaneVariant variant) throws IOException {
        java.util.List<Path> nonApplicable = getNonApplicableXplFiles(variant);
        if (nonApplicable.isEmpty()) return java.util.List.of();

        // For Skunkcrafts-managed plugins, record exclusions before deleting so the updater
        // does not offer to restore the trimmed binaries; abort if the ignore file cannot be saved
        if (isSkunkcraftsManaged()) {
            addToSkunkcraftsIgnore(nonApplicable);
        }

        java.util.List<Path> removed = new java.util.ArrayList<>();
        for (Path file : nonApplicable) {
            moveToTrash(file);
            removed.add(file);
            log.debug("Removed non-applicable .xpl file: {}", file);
            
            // Clean up empty parent directories up to the base folder
            cleanupEmptyDirectories(file.getParent());
        }

        return removed;
    }
    
    /**
     * Deletes empty directories starting from the given path up to (but not including) the base folder.
     */
    private void cleanupEmptyDirectories(Path directory) throws IOException {
        Path baseFolder = getBaseFolder();
        
        // Walk up the directory tree, deleting empty directories until we reach the base folder
        while (directory != null && 
               directory.startsWith(baseFolder) && 
               !directory.equals(baseFolder)) {
            
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
