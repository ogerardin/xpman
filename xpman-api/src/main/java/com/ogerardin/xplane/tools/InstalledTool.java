package com.ogerardin.xplane.tools;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A locally installed tool. It may be associated to a {@link Manifest} or not (if it was installed manually).
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public non-sealed class InstalledTool extends Tool {

    @NonNull
    private final Path app;

    @Getter(lazy = true)
    private final String version = loadVersion();


    private InstalledTool(@NonNull Path app, String name, Manifest manifest) {
        super(name, manifest);
        this.app = app;
    }

    private InstalledTool(Path app, String name) {
        this(app, name, null);
    }

    /** Constructs an installed tool without known manifest */
    public InstalledTool(@NonNull Path app) {
        this(app, FilenameUtils.removeExtension(app.getFileName().toString()));
    }

    /** Constructs an installed tool with specified manifest */
    public InstalledTool(@NonNull Path app, @NonNull Manifest manifest) {
        this(app, manifest.name(), manifest);
    }

    /**
     * Returns an {@link InstalledTool} instance for the specified {@link InstallableTool}, assuming it is installed in
     * the specified folder.
     */
    public static InstalledTool ofInstallable(InstallableTool installableTool, Path toolsFolder) {
        Path path = toolsFolder.resolve(installableTool.getManifest().file());
        return new InstalledTool(path, installableTool.getManifest());
    }

    @Override
    public boolean isInstallable() {
        return false;
    }

    @Override
    public boolean isInstalled() {
        return true;
    }

    @Override
    public boolean isRunnable() {
        return true;
    }

    // ponytail: placeholder versions shipped by tools with unset metadata (e.g. SkunkcraftsUpdater's 0.0.1);
    // extend as encountered — a genuine 0.0.1 tool would display no version.
    private static final Set<String> PLACEHOLDER_VERSIONS = Set.of("0.0.0", "0.0.1");

    private String loadVersion() {
        Manifest manifest = getManifest();
        if (manifest == null) {
            return null;
        }
        Pattern versionRegex = manifest.versionRegex();
        String version = versionRegex != null
                ? manifest.platform().extractVersion(app, versionRegex)
                : manifest.platform().getVersion(app);
        return isPlaceholder(version) ? null : version;
    }

    private static boolean isPlaceholder(String version) {
        return version == null || version.isBlank() || PLACEHOLDER_VERSIONS.contains(version);
    }


}
