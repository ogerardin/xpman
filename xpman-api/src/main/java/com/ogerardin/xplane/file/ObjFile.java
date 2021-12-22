package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.obj.ObjFileData;
import com.ogerardin.xplane.file.parboiled.ObjFileParser;
import com.ogerardin.xplane.file.parboiled.ParboiledParser;
import lombok.ToString;

import java.nio.file.Path;

/**
 * Represents a parsed .obj file
 */
@ToString(onlyExplicitlyIncluded = true)
public class ObjFile extends XPlaneFile<ObjFileData> {

    public ObjFile(Path file) {
        super(file, new ParboiledParser<>(ObjFileParser.class, true));
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }



}
