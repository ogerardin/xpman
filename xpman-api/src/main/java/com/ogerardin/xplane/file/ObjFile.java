package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.obj.ObjFileData;
import com.ogerardin.xplane.file.petitparser.ObjFileParser;
import lombok.ToString;

import java.nio.file.Path;

/**
 * Represents a parsed .obj file (scenery object)
 */
@ToString(onlyExplicitlyIncluded = true)
public class ObjFile extends XPlaneFile<ObjFileData> {

    public ObjFile(Path file) {
        super(file, new ObjFileParser());
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }

    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }


}
