package com.ogerardin.xplane.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.WordUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@Slf4j
public class Aircraft implements InspectionsProvider<Aircraft> {

    private final AcfFile acfFile;

    private final String name;

    public Aircraft(AcfFile acfFile) {
        this(acfFile, null);
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

    public Category getCategory() {
        return Arrays.stream(Category.values())
                .filter(category -> Objects.equals(getProperty(category.property), "1"))
                .findAny()
                .orElse(null);
    }

    @AllArgsConstructor
    public enum Category {
        ULTRALIGHT("acf/_is_ultralight"),
        EXPERIMENTZAL("acf/_is_experimental"),
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
        Path file = getAcfFile().getFile();
        String filename = file.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String thumbFilename = filename.substring(0, dot) + "_icon11_thumb.png";
        Path thumbFile = file.resolveSibling(thumbFilename);
        return thumbFile;
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
                    .forEach(path -> map.put("Manual: " + path.getFileName(), path));
        }
        return map;
    }

    @Override
    public Inspections<Aircraft> getInspections(XPlane xPlane) {
        return new Inspections<>();
    }

}
