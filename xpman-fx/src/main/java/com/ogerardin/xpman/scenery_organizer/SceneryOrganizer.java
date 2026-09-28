package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.Data;

import java.util.*;
import java.util.stream.Stream;

@Data
public class SceneryOrganizer {

    private final List<SceneryClass> orderedSceneryClasses;

    public SceneryOrganizer() {
        this(defaultSceneryClasses());
    }

    public SceneryOrganizer(List<SceneryClass> classes) {
        this.orderedSceneryClasses = new ArrayList<>(migrate(classes));
    }

    private static List<SceneryClass> defaultSceneryClasses() {
        return new ArrayList<>(List.of(
                BuiltinSceneryClass.AIRPORT,
                new RegexSceneryClass("X-Plane Landmark", "X-Plane Landmarks.*"),
                new RegexSceneryClass("Global Airports", "Global Airports"),
                new RegexSceneryClass("Overlay scenery", "(?i)(.*overlay.*|.*world2xplane.*|.*trees.*|.*farms.*|.*osm2xp.*|.*simheaven.*|.*x-world.*|.*x_world.*)"),
                new RegexSceneryClass("Mesh scenery", "(?i)(z\\+.*|.*ortho4xp.*|.*zortho.*|.*yortho.*|.*\\+[0-9]{2}-[0-9]{3}.*|.*elevation_data.*)"),
                BuiltinSceneryClass.LIBRARY,
                BuiltinSceneryClass.OTHER
        ));
    }

    private static List<SceneryClass> migrate(List<SceneryClass> classes) {
        if (classes == null || classes.isEmpty()) {
            return defaultSceneryClasses();
        }
        boolean hasBuiltins = classes.stream().anyMatch(SceneryClass::isBuiltin);
        if (hasBuiltins) {
            return classes;
        }
        List<SceneryClass> result = new ArrayList<>();
        result.add(BuiltinSceneryClass.AIRPORT);
        classes.stream()
                .filter(c -> !"Airport".equals(c.getName()))
                .forEach(result::add);
        result.add(BuiltinSceneryClass.LIBRARY);
        result.add(BuiltinSceneryClass.OTHER);
        return result;
    }

    /**
     * @return the {@link SceneryClass} for the specified scenery. Name-based (regex) rules are consulted
     * first as they are more specific (brand-specific patterns), then file-based (builtin) signals
     * in list order; fallback is {@link BuiltinSceneryClass#OTHER}. The list position determines
     * sort rank (load order in scenery_packs.ini) regardless of tier.
     */
    public SceneryClass sceneryClass(SceneryPackage sceneryPackage) {
        return Stream.concat(
                        getOrderedSceneryClasses().stream().filter(c -> !c.isBuiltin()),
                        getOrderedSceneryClasses().stream().filter(SceneryClass::isBuiltin))
                .filter(c -> c.matches(sceneryPackage))
                .findFirst()
                .orElse(BuiltinSceneryClass.OTHER);
    }

    public int sceneryClassRank(SceneryPackage sceneryPackage) {
        int index = getOrderedSceneryClasses().indexOf(sceneryClass(sceneryPackage));
        return index >= 0 ? index : getOrderedSceneryClasses().size();
    }

    public List<SceneryPackage> apply(List<SceneryPackage> sceneryPackages) {
        final ArrayList<SceneryPackage> packages = new ArrayList<>(sceneryPackages);
        packages.sort(Comparator.comparingInt(this::sceneryClassRank));
        return packages;
    }

    public void setOrderedSceneryClasses(List<SceneryClass> sceneryClasses) {
        this.orderedSceneryClasses.clear();
        this.orderedSceneryClasses.addAll(sceneryClasses);
    }
}
