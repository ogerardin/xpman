package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.obj.ObjFileData;
import com.ogerardin.xplane.file.grammar.ObjFileParser;
import lombok.ToString;
import org.parboiled.parserunners.RecoveringParseRunner;

import java.nio.file.Path;

/**
 * Represents a parsed .acf file
 */
@ToString(onlyExplicitlyIncluded = true)
public class ObjFile extends XPlaneDataFile<ObjFileParser, ObjFileData> {

    public ObjFile(Path file) {
        super(file, ObjFileParser.class, RecoveringParseRunner.class);
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }



}
