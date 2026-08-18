package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.dat.DatFileData;
import com.ogerardin.xplane.file.petitparser.DatFileParser;

import java.nio.file.Path;

/**
 * Represents a parsed X-Plane navigation data file (.dat).
 *
 * <p>DAT files contain navigation data such as waypoints, airways, and airports.
 * These files are part of the X-Plane navdata system and are typically found
 * in the {@code Resources/default data} directory.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Path datPath = Path.of("Resources/default data/earth_nav.dat");
 * DatFile datFile = new DatFile(datPath);
 *
 * // Get file version
 * String version = datFile.getFileSpecVersion();
 *
 * // Access parsed data
 * DatFileData data = datFile.getData();
 * DatHeader header = data.getHeader();
 * }</pre>
 *
 * <h2>File Format Reference</h2>
 * <p>See <a href="https://developer.x-plane.com/article/navdata-in-x-plane-11/">
 * Navdata in X-Plane</a> for details on nav data file formats.</p>
 *
 * @author Olivier G.
 * @see DatFileData
 * @see DatFileParser
 */
public class DatFile extends XPlaneFile<DatFileData> {

    /**
     * Create a DatFile from a file path.
     *
     * @param file the path to the .dat file
     */
    public DatFile(Path file) {
        super(file, new DatFileParser());
    }

    /**
     * Returns the DAT file specification version.
     *
     * @return version string
     */
    @Override
    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }
}
