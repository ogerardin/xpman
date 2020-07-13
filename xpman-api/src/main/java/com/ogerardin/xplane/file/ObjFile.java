package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.AcfFileData;
import com.ogerardin.xplane.file.data.ObjFileData;
import com.ogerardin.xplane.file.grammar.AcfFileParser;
import com.ogerardin.xplane.file.grammar.ObjFileParser;
import lombok.ToString;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents a parsed .acf file
 */
@ToString(onlyExplicitlyIncluded = true)
public class ObjFile extends XPlaneDataFile<ObjFileParser, ObjFileData> {

    public ObjFile(Path file) {
        super(file, ObjFileParser.class);
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }



}
