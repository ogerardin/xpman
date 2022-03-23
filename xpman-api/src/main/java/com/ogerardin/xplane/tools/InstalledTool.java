package com.ogerardin.xplane.tools;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Optional;

@Getter
@EqualsAndHashCode(callSuper = true)
public class InstalledTool extends Tool {

    @NonNull
    private final Path app;

    @Getter(lazy = true)
    private final String version = loadVersion();


    private InstalledTool(@NonNull Path app, String name, ToolManifest manifest) {
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
    public InstalledTool(@NonNull Path app, @NonNull ToolManifest manifest) {
        this(app, manifest.getName(), manifest);
    }

    public boolean isInstallable() {
        return false;
    }

    public boolean isInstalled() {
        return true;
    }

    public boolean isRunnable() {
        return (app != null);
    }

    private String loadVersion() {
        if (app == null) {
            return null;
        }
        return Optional.ofNullable(getManifest())
                .map(ToolManifest::getInstalledVersionGetter)
                .map(versionGetter -> versionGetter.apply(app))
                .orElse(null);
    }


}
