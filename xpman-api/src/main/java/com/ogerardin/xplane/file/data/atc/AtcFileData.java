package com.ogerardin.xplane.file.data.atc;

import com.ogerardin.xplane.file.data.Header;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Data model for parsed atc.dat files.
 *
 * <p>atc.dat files provide airspace boundaries for ATC controllers. Only the
 * standard header is parsed; the controller body is not.</p>
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class AtcFileData {

    /**
     * The atc.dat file header (origin, spec version, file type).
     */
    private Header header;
}