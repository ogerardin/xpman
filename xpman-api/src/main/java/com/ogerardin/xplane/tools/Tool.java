package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.util.Optional;

@Data
@AllArgsConstructor
public class Tool implements InspectionsProvider<Tool> {

    private final Path executable;
    private final String name;
    private final ToolManifest manifest;

    @Getter(lazy = true)
    private final String version = loadVersion();

    /** Constructs an installed tool without known manifest */
    public Tool(@NonNull Path path) {
        this(path, FilenameUtils.removeExtension(path.getFileName().toString()), null);
    }

    /** Constructs an installed tool with specified manifest */
    public Tool(@NonNull Path path, @NonNull ToolManifest manifest) {
        this(path, manifest.getName(), manifest);
    }

    /** Constructs a non-installed (available) tool */
    public Tool(ToolManifest manifest) {
        this(null, manifest.getName(), manifest);
    }


    @Override
    public Inspections<Tool> getInspections(XPlane xPlane) {
        return null;
    }

    public boolean isInstallable() {
        return (executable == null) && (manifest != null);
    }

    public boolean isInstalled() {
        return (executable != null) && (manifest != null);
    }

    public boolean isRunnable() {
        return (executable != null);
    }

    public String loadVersion() {
        if (executable == null) {
            return null;
        }
        return Optional.ofNullable(manifest)
                .map(ToolManifest::getVersionGetter)
                .map(versionGetter -> versionGetter.apply(executable))
                .orElse(null);
    }

}
