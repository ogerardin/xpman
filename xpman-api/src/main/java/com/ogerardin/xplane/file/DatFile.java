package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.dat.DatFileData;
import com.ogerardin.xplane.file.petitparser.DatFileParser;

import java.nio.file.Path;

/** Represents a nav data file, that is one of the files described
 * in <a href="https://developer.x-plane.com/article/navdata-in-x-plane-11">this page</a>.
 */
public class DatFile extends XPlaneFile<DatFileData> {

    public DatFile(Path file) {
        super(file, new DatFileParser());
    }

    @Override
    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }
}
