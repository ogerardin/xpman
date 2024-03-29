package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.acf.AcfFileData;
import com.ogerardin.xplane.file.petitparser.AcfFileParser;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents a parsed .acf file (aircraft description)
 */
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = true)
public class AcfFile extends XPlaneFile<AcfFileData> {

    public AcfFile(Path file) {
        super(file, new AcfFileParser());
    }

    public String getProperty(String name) {
        return getProperties().get(name);
    }

    public Map<String, String> getProperties() {
        return getData().getProperties();
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }

    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }



}
