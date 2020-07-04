package com.ogerardin.xplane.config.aircrafts;

import com.ogerardin.xplane.config.LinkType;
import com.ogerardin.xplane.diag.DiagItem;
import com.ogerardin.xplane.diag.DiagProvider;
import com.ogerardin.xplane.file.AcfFile;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;

@Data
@Slf4j
public class Aircraft implements DiagProvider {

    @Setter(AccessLevel.PACKAGE)
    private AcfFile acfFile;

    private final String name;

    public boolean enabled = false;

    public Aircraft(AcfFile acfFile, String name) {
        this.acfFile = acfFile;
        this.name = name;
    }

    public Aircraft(AcfFile acfFile) {
        this(acfFile, null);
    }

    private String getProperty(String name) {
        return acfFile.getProperty(name);
    }

    public String getStudio() {
        return getProperty("acf/_studio");
    }

    public String getName() {
        return Optional.ofNullable(name).orElse(getAcfName());
    }

    private String getAcfName() {
        return Optional.ofNullable(getProperty("acf/_name"))
                .orElse(getAcfFile().getFile().getParent().getFileName().toString());
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


    @Override
    public List<DiagItem> getDiagItems() {
        return null;
    }

    public String getLatestVersion() {
        return null;
    };

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

    //** Methods excluded from delegation */
    private interface Named {
        String getName();
    }

    public String getVersion() {
        return null;
    }

    public Path getThumb() {
        Path file = getAcfFile().getFile();
        String filename = file.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String thumbFilename = filename.substring(0, dot) + "_icon11_thumb.png";
        Path thumbFile = file.resolveSibling(thumbFilename);
        return thumbFile;
    }

    public Map<LinkType, URL> getLinks() {
        return Collections.emptyMap();
    }


}
