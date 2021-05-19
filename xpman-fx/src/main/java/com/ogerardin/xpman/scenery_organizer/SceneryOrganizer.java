package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;

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

    /**
     * @return the {@link SceneryClass} for the specified scenery, determined as follows:
     * <ol>
     *     <li>if the scenery is a library, then {@link #LIBRARY_SCENERY_CLASS}</li>
     *     <li>otherwise the first item of {@link #orderedSceneryClasses} for which the scenery name matches
     *     {@link SceneryClass#getRegex()} </li>
     *     <li>in case no match was found, then {@link #OTHER_SCENERY_CLASS}</li>
     * </ol>
     */
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

    /**
     * @return the rank of the scenery's class for sorting purposes.
     * Special classes {@link #OTHER_SCENERY_CLASS} (not matched by any rule) and
     * {@link #LIBRARY_SCENERY_CLASS} (detected) are assigned ranks 98 and 99.
     */
    public int sceneryClassRank(SceneryPackage sceneryPackage) {
        SceneryClass sceneryClass = sceneryClass(sceneryPackage);
        if (sceneryClass == OTHER_SCENERY_CLASS) {
            return 98;
        } else if (sceneryClass == LIBRARY_SCENERY_CLASS) {
            return 99;
        }
        return getOrderedSceneryClasses().indexOf(sceneryClass);
    }

    public List<SceneryPackage> apply(List<SceneryPackage> sceneryPackages) {
        // make a copy so we don't sort the original list before the user OKs it
        final ArrayList<SceneryPackage> packages = new ArrayList<>(sceneryPackages);
        packages.sort(Comparator.comparingInt(this::sceneryClassRank));
        return packages;
    }

    public void setOrderedSceneryClasses(List<SceneryClass> sceneryClasses) {
        this.orderedSceneryClasses.clear();
        this.orderedSceneryClasses.addAll(sceneryClasses);
    }

}
