package com.ogerardin.xplane.file.data.obj;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

/**
 * Data model for parsed OBJ (scenery object) files.
 *
 * <p>OBJ8 files contain three main sections:</p>
 * <ul>
 *   <li><b>Attributes:</b> Texture references and point counts</li>
 *   <li><b>Data:</b> Vertex data (VT), vertex lists (VLINE), and indices (IDX)</li>
 *   <li><b>Commands:</b> Rendering commands (TRIS, LINES)</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ObjFileData data = objFile.getData();
 * for (ObjAttribute attr : data.getAttributes()) {
 *     if (attr instanceof ObjTexture tex) {
 *         System.out.println("Texture: " + tex.getReference());
 *     }
 * }
 * }</pre>
 *
 * @author Olivier G.
 * @see com.ogerardin.xplane.file.ObjFile
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ObjFileData extends XPlaneFileData {

    /**
     * List of OBJ attributes (textures, point counts, etc.).
     */
    ObjAttributes attributes = new ObjAttributes();

    /**
     * List of OBJ data items (vertices, indices, etc.).
     */
    ObjData data = new ObjData();

    /**
     * List of OBJ rendering commands (TRIS, LINES).
     */
    ObjCommands commands = new ObjCommands();

    /**
     * Create an ObjFileData instance.
     *
     * @param header the file header
     */
    public ObjFileData(Header header) {
        super(header);
    }

    /**
     * List of OBJ attributes.
     */
    public static class ObjAttributes extends ArrayList<ObjAttribute> {}

    /**
     * List of OBJ data items.
     */
    public static class ObjData extends ArrayList<ObjDatum> {}

    /**
     * List of OBJ commands.
     */
    public static class ObjCommands extends ArrayList<ObjCommand> {}

}
