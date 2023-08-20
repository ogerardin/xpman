package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.util.Records;
import com.ogerardin.xplane.util.platform.Platform;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A {@link Manifest} is the description of a {@link Tool}, including how to download and install it.
 * @param id the tool identifier which is the name of the JSON file from which the manifest was loaded (without the
 * ".json" extension)
 * @param platform target platform for the tool
 * @param xplaneVersion target X-Plane major version required by the tool
 * @param url URL where the tool can be downloaded. If the URL includes a fragment (part after #), it is interpreted
 * as a name to rename the downloaded file to.
 * @param file the path to the tool's executable file (.exe file for Windows, .app bundle for Mac, etc.).
 *  This is used to check whether the tool is already installed, and to launch it.
 * @param installChecker an additional {@link Predicate} that checks whether the tool is already installed. This can
 *  be used to differentiate between different versions of a tool that have the same executable file name.
 * @param version the version of the tool. If null, the version is extracted from the tool's executable file.
 */
@Slf4j
@With
public record Manifest(
        String id,
        String name,
        Path file,
        URL homepage,
        String version,
        String description,
        Platform platform,
        XPlaneMajorVersion xplaneVersion,
        URL url,
        Predicate<Path> installChecker,
        Set<Manifest> items
) {

    /**
     * Unfold all varients of this manifest recursively.
     */
    public List<Manifest> unfold() {
        if (items == null) {
            return Collections.singletonList(this);
        }
        return items.stream()
                .flatMap(item -> item.unfold().stream())
                .map(item -> Records.coalesce(this, item).withItems(null))
                .collect(Collectors.toList());
    }
}
