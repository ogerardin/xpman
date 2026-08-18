package com.ogerardin.xplane.file.data;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Base class for all X-Plane file data models.
 *
 * <p>This class provides the common structure for parsed file data,
 * including delegation to the {@link Header} for version and type information.</p>
 *
 * <h2>Usage</h2>
 * <p>Subclasses extend this class to add format-specific data structures:</p>
 * <ul>
 *   <li>{@link com.ogerardin.xplane.file.data.acf.AcfFileData} - ACF property maps</li>
 *   <li>{@link com.ogerardin.xplane.file.data.obj.ObjFileData} - OBJ attributes, data, commands</li>
 *   <li>{@link com.ogerardin.xplane.file.data.dat.DatFileData} - NAV data headers</li>
 * </ul>
 *
 * @author Olivier G.
 * @see Header
 */
@Data
@ToString
public class XPlaneFileData {
    /**
     * The file header containing origin, version, and file type.
     * Delegated via Lombok's {@code @Delegate} annotation.
     */
    @ToString.Include
    @Delegate
    final Header header;
}
