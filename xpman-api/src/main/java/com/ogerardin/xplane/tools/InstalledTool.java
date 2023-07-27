package com.ogerardin.xplane.tools;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Optional;

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

    private String loadVersion() {
        Manifest manifest = getManifest();
        if (manifest == null) {
            return null;
        }
        return Optional.ofNullable(manifest.platform().getVersion(app))
                .orElse(manifest.version());
    }


}
