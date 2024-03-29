package com.ogerardin.xplane.aircraft;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneObject;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.AircraftSpecInspection;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.WordUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Getter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Aircraft extends XPlaneObject implements Inspectable {

    @EqualsAndHashCode.Include
    private final AcfFile acfFile;

    private final String name;

    @Getter(lazy = true)
    @ToString.Exclude
    private final List<Livery> liveries = loadLiveries();

    public Aircraft(XPlane xPlane, AcfFile acfFile) {
        this(xPlane, acfFile, null);
    }

    public Aircraft(XPlane xPlane, AcfFile acfFile, String name) {
        super(xPlane);
        this.acfFile = acfFile;
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    private String getProperty(String name) {
        return (acfFile != null) ? acfFile.getProperty(name) : null;
    }

    public String getStudio() {
        return getProperty("acf/_studio");
    }

    public String getName() {
        return (name != null) ? name : getAcfName();
    }

    public String getAcfName() {
        String property = getProperty("acf/_name");
        if (property != null) {
            return property;
        }
        return getAcfFile().getFile().getFileName().toString().replace(".acf", "");
    }

    public String getDescription() {
        return getProperty("acf/_descrip");
    }

    public String getCallsign() {
        return getProperty("acf/_callsign");
    }

    public String getTailNumber() {
        return getProperty("acf/_tailnum");
    }

    public String getIcaoCode() {
        return getProperty("acf/_ICAO");
    }

    public String getAuthor() {
        return getProperty("acf/_author");
    }

    public String getManufacturer() {
        return getProperty("acf/_manufacturer");
    }

    public String getNotes() {
        return getProperty("acf/_notes");
    }

    public String getVersion() {
        return getProperty("acf/_version");
    }

    public String getLatestVersion() {
        return null;
    }

    public boolean isExtraAircraft() {
        return getAcfFile().getFile().getParent().getFileName().toString().equals("Extra Aircraft");
    }

    public Category getCategory() {
        return Arrays.stream(Category.values())
                .filter(category -> Objects.equals(getProperty(category.property), "1"))
                .findAny()
                .orElse(null);
    }

    @SneakyThrows
    private List<Livery> loadLiveries() {
        Path liveriesFolder = getLiveriesFolder();
        if (! Files.isDirectory(liveriesFolder)) {
            return Collections.emptyList();
        }
        return Files.list(liveriesFolder)
                .filter(Files::isDirectory)
                .map(path -> new Livery(this, acfFile.getFile().relativize(path)))
                .toList();
    }

    public Path getLiveriesFolder() {
        return getAcfFile().getFile().resolveSibling("liveries");
    }

    @AllArgsConstructor
    public enum Category {
        ULTRALIGHT("acf/_is_ultralight"),
        EXPERIMENTAL("acf/_is_experimental"),
        GENERAL_AVIATION("acf/_is_general_aviation"),
        AIRLINER("acf/_is_airliner"),
        MILITARY("acf/_is_military"),
        CARGO("acf/_is_cargo"),
        GLIDER("acf/_is_glider"),
        SEAPLANE("acf/_is_seaplane"),
        HELICOPTER("acf/_is_helicopter"),
        VTOL("acf/_is_vtol"),
        SCIENCE_FICTION("acf/_is_sci_fi");

        final String property;

        @Override
        public String toString() {
            return WordUtils.capitalizeFully(name().replace("_", " "));
        }
    }

    @SuppressWarnings("unused")
    public Path getThumb() {
        return getAcfDerivedFile("_icon11_thumb.png");
    }

    @SuppressWarnings("unused")
    public Path getIcon() {
        return getAcfDerivedFile("_icon11.png");
    }

    private Path getAcfDerivedFile(String suffix) {
        Path file = getAcfFile().getFile();
        String acfFilename = file.getFileName().toString();
        String derivedFilename = FilenameUtils.removeExtension(acfFilename) + suffix;
        return file.resolveSibling(derivedFilename);
    }

    @SneakyThrows
    public Map<String, URL> getLinks() {
        return new HashMap<>();
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    public Map<String, Path> getManuals() {
        final HashMap<String, Path> map = new HashMap<>();
        try (Stream<Path> pathStream = Files.list(getAcfFile().getFile().getParent())) {
            pathStream
                    .filter(path -> path.getFileName().toString().toLowerCase().contains("manual"))
                    .forEach(path -> map.put(path.getFileName().toString(), path));
        }
        return map;
    }

    @Override
    public InspectionResult inspect() {
        return AircraftSpecInspection.INSTANCE.inspect(this);
    }
}
