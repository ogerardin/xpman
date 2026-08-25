package com.ogerardin.xpman.panels.xplane.breakdown;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.FileUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The categories of X-Plane usage.
 */
@RequiredArgsConstructor
@Getter
enum UsageCategory {
    AIRCRAFT("Aircraft", "segment-aircraft",
            xp -> xp.getAircraftManager().getAircraftFolder()),

    GLOBAL_SCENERY("Global scenery", "segment-global-scenery",
            xp -> xp.getPaths().globalScenery()),

    CUSTOM_SCENERY("Custom scenery", "segment-custom-scenery",
            xp -> xp.getSceneryManager().getSceneryFolder()),

    CUSTOM_SCENERY_DISABLED("Disabled scenery", "segment-disabled-scenery",
            xp -> xp.getSceneryManager().getDisabledSceneryFolder()),

    OTHER("Other", "segment-other",
            null,
            (xp, folderSizes) -> {
                try {
                    long total = FileUtils.getFolderSize(xp.getBaseFolder());
                    long used = folderSizes.values().stream().mapToLong(Long::longValue).sum();
                    return new CategoryResult(total - used, List.of());
                } catch (IOException e) {
                    return new CategoryResult(0, List.of());
                }
            });

    private final String text;
    private final String styleClass;
    private final Function<XPlane, Path> pathResolver;
    private final BiFunction<XPlane, Map<UsageCategory, Long>, CategoryResult> sizeComputer;

    UsageCategory(String text, String styleClass, Function<XPlane, Path> pathResolver) {
        this(text, styleClass, pathResolver,
                (xp, __) -> computeFolderPath(xp, pathResolver.apply(xp)));
    }

    static CategoryResult computeFolderPath(XPlane xp, Path folder) {
        if (!Files.exists(folder)) return new CategoryResult(0, List.of());
        try {
            return new CategoryResult(FileUtils.getFolderSize(folder), List.of(xp.getBaseFolder().relativize(folder)));
        } catch (IOException e) {
            return new CategoryResult(0, List.of());
        }
    }

    record CategoryResult(long size, List<Path> folderPaths) {}
}
