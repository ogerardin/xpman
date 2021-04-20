package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Data
@RequiredArgsConstructor
public class SceneryOrganizer {

    private static final SceneryClass OTHER_SCENERY_CLASS = new SceneryClass("Other");
    private static final SceneryClass LIBRARY_SCENERY_CLASS = new SceneryClass("Library");

    private final List<SceneryClass> orderedSceneryClasses;

    public SceneryOrganizer() {
        this(defaultSceneryClasses());
    }

    private static List<SceneryClass> defaultSceneryClasses() {
        return Arrays.asList(
                new SceneryClass("Airport", ".*[Aa]irport.*", ".*[^A-Z][A-Z]{4}[^A-Z].*"),
                new SceneryClass("Overlay scenery", ".*overlay.*", ".*[Ll]andmark.*"),
                new SceneryClass("Mesh scenery", "z\\+.*")
        );
    }

    public SceneryClass sceneryClass(SceneryPackage sceneryPackage) {
        if (sceneryPackage.isLibrary()) {
            return LIBRARY_SCENERY_CLASS;
        }
        final String sceneryName = sceneryPackage.getFolder().getFileName().toString();
        final Optional<SceneryClass> sceneryClass = getOrderedSceneryClasses().stream()
                .filter(c -> c.matches(sceneryName))
                .findFirst();
        return sceneryClass.orElse(OTHER_SCENERY_CLASS);
    }
}
