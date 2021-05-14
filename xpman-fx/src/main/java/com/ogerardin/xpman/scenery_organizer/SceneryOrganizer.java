package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

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
                new SceneryClass("Airport", "^(?!Global).*[Aa]irport.*|(.*[^A-Z]|^)[A-Z]{4}[^A-Z].*|X-Plane Landmarks.*"),
                new SceneryClass("Global Airports", "Global Airports"),
                new SceneryClass("Overlay scenery", ".*overlay.*|.*world2xplane.*|.*trees.*|.*farms.*|.*osm2xp.*"),
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

    public int rank(SceneryPackage sceneryPackage) {
        SceneryClass sceneryClass = sceneryClass(sceneryPackage);
        if (sceneryClass == OTHER_SCENERY_CLASS) {
            return 98;
        } else if (sceneryClass == LIBRARY_SCENERY_CLASS) {
            return 99;
        }
        return getOrderedSceneryClasses().indexOf(sceneryClass);
    }

    public List<SceneryPackage> apply(List<SceneryPackage> sceneryPackages) {
        final ArrayList<SceneryPackage> packages = new ArrayList<>(sceneryPackages);
        packages.sort(Comparator.comparingInt(this::rank));
        return packages;
    }

    public void setOrderedSceneryClasses(List<SceneryClass> sceneryClasses) {
        this.orderedSceneryClasses.clear();
        this.orderedSceneryClasses.addAll(sceneryClasses);
    }
}
