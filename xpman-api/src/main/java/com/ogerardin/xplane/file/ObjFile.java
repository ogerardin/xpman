package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.obj.ObjFileData;
import com.ogerardin.xplane.file.petitparser.ObjFileParser;
import lombok.ToString;

import java.nio.file.Path;

/**
 * Represents a parsed X-Plane scenery object file (.obj).
 *
 * <p>OBJ files contain 3D geometry data for scenery objects including:</p>
 * <ul>
 *   <li><b>Attributes:</b> Texture references and point counts</li>
 *   <li><b>Data:</b> Vertex data (VT), vertex lists (VLINE), and indices (IDX, IDX10)</li>
 *   <li><b>Commands:</b> Triangle rendering (TRIS) and line drawing (LINES)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * Path objPath = Path.of("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/v01partI12.obj");
 * ObjFile objFile = new ObjFile(objPath);
 *
 * // Get file version
 * String version = objFile.getFileSpecVersion(); // "800"
 *
 * // Access parsed data
 * ObjFileData data = objFile.getData();
 * List<ObjAttribute> attributes = data.getAttributes();
 * List<ObjDatum> dataItems = data.getData();
 * List<ObjCommand> commands = data.getCommands();
 * }</pre>
 *
 * <h2>File Format Reference</h2>
 * <p>See <a href="https://developer.x-plane.com/article/obj8-file-format-specification/">
 * OBJ8 file format specification</a> for details.</p>
 *
 * @author Olivier G.
 * @see ObjFileData
 * @see ObjFileParser
 */
@ToString(onlyExplicitlyIncluded = true)
public class ObjFile extends XPlaneFile<ObjFileData> {

    /**
     * Create an ObjFile from a file path.
     *
     * @param file the path to the .obj file
     */
    public ObjFile(Path file) {
        super(file, new ObjFileParser());
    }

    @ToString.Include
    private Path file() {
        return super.getFile();
    }

    /**
     * Returns the OBJ file specification version.
     *
     * @return version string (e.g., "800" for OBJ8 format)
     */
    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }

}
