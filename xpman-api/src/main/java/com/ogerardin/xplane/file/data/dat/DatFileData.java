package com.ogerardin.xplane.file.data.dat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data model for parsed DAT (navigation data) files.
 *
 * <p>DAT files contain navigation data such as waypoints, airways, and airports.
 * The data format varies by file type (earth_nav.dat, earth_awy.dat, etc.).</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * DatFileData data = datFile.getData();
 * DatHeader header = data.getHeader();
 * String version = header.getDatVersion();
 * }</pre>
 *
 * @author Olivier G.
 * @see com.ogerardin.xplane.file.DatFile
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class DatFileData  {

    /**
     * The DAT file header containing version and metadata.
     */
    private DatHeader header;
}
