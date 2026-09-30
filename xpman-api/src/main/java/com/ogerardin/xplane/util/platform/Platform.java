package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.util.Urls;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common interface for operations that have a platform-specific implementation.
 */
public interface Platform {

    /** The int value of {@link com.sun.jna.Platform} that matches this platform */
    int getOsType();

    default String getCpuType() { return "unknown"; }

    default int getCpuCount() { return 1; }

    default boolean isCurrent() {
        return getOsType() == com.sun.jna.Platform.getOSType();
    }

    /** The platform-specific text corresponding to the action "reveal in Finder" or "show in Explorer" or equivalent */
    default String revealLabel() {
        return "Show in files";
    }

    /** Reveal in Finder / show in Explorer or equivalent */
    void reveal(@NonNull Path path);

    void openFile(@NonNull Path path);

    /**
     * Opens a file in the system's default text editor.
     * Unlike {@link #openFile(Path)}, this ensures the file is opened for editing, not with its default handler.
     */
    void openInTextEditor(@NonNull Path path);

    void openUrl(@NonNull URL url);

    @SneakyThrows
    default void openUrl(@NonNull String url) {
        openUrl(Urls.url(url));
    }

    /**
     * Start an application from the specified path.
     * The nature of the path may vary depending on the platform (binary executable file, app bundle, etc.)
     */
    void startApp(@NonNull Path app);

    /** Is the specified path an existing runnable for this platform? */
    boolean isRunnable(@NonNull Path path);

    /**
     * Returns the version of the application at the specified path.
     */
    String getVersion(Path app);

    /** Returns the path of the application's main binary (the executable inside an app bundle on macOS). */
    default Path getBinary(Path app) {
        return app;
    }

    // ponytail: whole-binary regex scan; fine for one-shot lazy version extraction, revisit if it gets hot.
    /** Extracts a version from the application's binary by scanning for the first match of the given pattern (group 1). */
    @SneakyThrows
    default String extractVersion(Path app, Pattern pattern) {
        String content = new String(Files.readAllBytes(getBinary(app)), StandardCharsets.ISO_8859_1);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Extracts version information from an X-Plane plugin (.xpl) binary.
     */
    default String extractPluginVersion(Path xplFile) {
        return null;
    }

    /**
     * Checks if the specified path has the macOS quarantine attribute.
     * Returns false on non-Mac platforms.
     */
    default boolean isQuarantined(Path path) {
        return false;
    }

    /**
     * Removes the macOS quarantine attribute from the specified path.
     * No-op on non-Mac platforms.
     */
    default void removeQuarantine(Path path) {}

    /**
     * Fixes executable permissions for application bundles after extraction.
     * No-op on non-Mac platforms.
     */
    default void fixAppBundlePermissions(Path path) {}

    /**
     * The name of the folder holding the convert424toxplane binary inside the converter
     * distribution archive, or null if this platform has no CIFP converter.
     */
    default String cifpConverterFolder() {
        return null;
    }

    /**
     * The name of the convert424toxplane executable for the given X-Plane version,
     * or null if this platform has no CIFP converter.
     */
    default String cifpConverterName(XPlaneMajorVersion majorVersion) {
        return null;
    }

    default List<Path> getCandidateInstallBaseFolders(Path userHome) {
        return List.of(
                userHome.resolve("Applications"),
                userHome.resolve("Desktop"),
                userHome
        );
    }

    default boolean isMatchingPluginPath(Path xplFile) {
        String path = xplFile.toString().toLowerCase();
        String id = pluginPathIdentifier();
        if (id == null) return true;
        return path.contains(id) || !hasAnyPlatformMarker(path);
    }

    default String pluginPathIdentifier() {
        return null;
    }

    private static boolean hasAnyPlatformMarker(String path) {
        return path.contains("mac") || path.contains("win") || path.contains("lin");
    }
}
