package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Data
@RequiredArgsConstructor
public class SceneryOrganizer {

    /** User-defined scenery classes ordered as expected in scenery_packs.ini */
    private final List<RegexSceneryClass> orderedSceneryClasses;

    public SceneryOrganizer() {
        this(defaultSceneryClasses());
    }

    private static List<RegexSceneryClass> defaultSceneryClasses() {
        return Arrays.asList(
                new RegexSceneryClass("Airport", "^(?!Global).*[Aa]irport.*|(.*[^A-Z]|^)[A-Z]{4}[^A-Z].*"),
                new RegexSceneryClass("X-Plane Landmark", "X-Plane Landmarks.*"),
                new RegexSceneryClass("Global Airports", "Global Airports"),
                new RegexSceneryClass("Overlay scenery", ".*overlay.*|.*world2xplane.*|.*trees.*|.*farms.*|.*osm2xp.*"),
                new RegexSceneryClass("Mesh scenery", "z\\+.*")
        );
    }

    /**
     * @return the {@link SceneryClass} for the specified scenery, determined as follows:
     * <ol>
     *     <li>if the scenery is a library, then {@link LibrarySceneryClass}</li>
     *     <li>otherwise the first item of {@link #orderedSceneryClasses} for which the scenery name matches
     *     {@link SceneryClass#matches} is true </li>
     *     <li>in case no match was found, then {@link OtherSceneryClass}</li>
     * </ol>
     */
    public SceneryClass sceneryClass(SceneryPackage sceneryPackage) {
        if (sceneryPackage.isLibrary()) {
            return LibrarySceneryClass.INSTANCE;
        }
        final Optional<SceneryClass> sceneryClass = getOrderedSceneryClasses().stream()
                .filter(c -> c.matches(sceneryPackage))
                .findFirst()
                .map(SceneryClass.class::cast);
        return sceneryClass.orElse(OtherSceneryClass.INSTANCE);
    }

    /**
     * @return the rank of the scenery's class for sorting purposes.
     */
    public int sceneryClassRank(SceneryPackage sceneryPackage) {
        SceneryClass sceneryClass = sceneryClass(sceneryPackage);

        if (sceneryClass instanceof OtherSceneryClass) {
            return 98;
        } else if (sceneryClass instanceof LibrarySceneryClass) {
            return 99;
        }
        return getOrderedSceneryClasses().indexOf(sceneryClass);
    }

    /**
     * Returns a copy of the specified list of {@link SceneryPackage}s ordered according to the
     * scenery class rank of its members, as defined by {@link #sceneryClassRank} 
     */
    public List<SceneryPackage> apply(List<SceneryPackage> sceneryPackages) {
        // make a copy so we don't sort the original list before the user OKs it
        final ArrayList<SceneryPackage> packages = new ArrayList<>(sceneryPackages);
        packages.sort(Comparator.comparingInt(this::sceneryClassRank));
        return packages;
    }

    public void setOrderedSceneryClasses(List<RegexSceneryClass> sceneryClasses) {
        this.orderedSceneryClasses.clear();
        this.orderedSceneryClasses.addAll(sceneryClasses);
    }

}
