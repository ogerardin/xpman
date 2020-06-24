package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.AcfFileData;
import com.ogerardin.xplane.file.grammar.AcfFileParser;
import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a parsed .acf file
 */
public class AcfFile extends XPlaneDataFile<AcfFileParser, AcfFileData> {

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
    }

    public AcfFile(Path file) {
        super(file, AcfFileParser.class);
    }

    public String getProperty(String name) {
        return getProperties().get(name);
    }

    public Map<String, String> getProperties() {
        return getData().getProperties();
    }

    public String getStudio() {
        return getProperty("acf/_studio");
    }

    public String getName() {
        return Optional.ofNullable(getProperty("acf/_name"))
                .orElse(getFile().getParent().getFileName().toString());
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

    public Category getCategory() {
        return Arrays.stream(Category.values())
                .filter(category -> Objects.equals(getProperty(category.property), "1"))
                .findAny()
                .orElse(null);
    }


}
