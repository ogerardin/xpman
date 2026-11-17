package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.util.zip.Archive;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Installable type for scenery libraries.
 * Recognizes single-root archives containing "library.txt" at the root and installs to Custom Scenery.
 */
@SuppressWarnings("unused")
public class SceneryLibraryInstallableType extends SceneryInstallableType {

    @Override
    public String description() {
        return "scenery library";
    }

    @Override
    public boolean recognizes(Archive archive) {
        // Only recognize single-root archives — multi-root archives (e.g. openSAM with plugin + library)
        // are handled by their more specialized plugin types.
        Set<Path> roots = archive.getPaths().stream()
                .filter(path -> path.getNameCount() > 1)
                .map(path -> path.getName(0))
                .collect(Collectors.toSet());
        if (roots.size() != 1) {
            return false;
        }
        return archive.getPaths().stream()
            .anyMatch(path -> path.getFileName().toString().equals("library.txt")
                    && path.getNameCount() == 2);
    }
}
