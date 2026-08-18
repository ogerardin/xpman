package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.obj.*;
import lombok.extern.slf4j.Slf4j;
import org.petitparser.parser.Parser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.StringParser.of;

/**
 * Parser for X-Plane OBJ8 (scenery object) files.
 *
 * <p>This parser handles the OBJ8 file format which contains 3D geometry data
 * for scenery objects including attributes, vertex data, and rendering commands.</p>
 *
 * <h2>Grammar Overview</h2>
 * <pre>
 * OBJFile = Header("OBJ") Items
 * Items = (EmptyLine | WhitespaceLine | CommentLine | Attribute | Datum | Command)*
 * Attribute = Texture | PointCounts | OtherAttribute
 * Datum = Vt | VLine | Idx | Idx10
 * Command = Tris | Lines
 * </pre>
 *
 * <h2>File Format Reference</h2>
 * <p>See <a href="https://developer.x-plane.com/article/obj8-file-format-specification/">
 * OBJ8 file format specification</a> for details.</p>
 *
 * @author Olivier G.
 * @see ObjFileData
 */
@Slf4j
public class ObjFileParser extends XPlaneFileParserBase<ObjFileData> {

    /**
     * Required file type for OBJ files.
     */
    static final String REQUIRED_TYPE = "OBJ";

    /**
     * Matches a X-Plane scenery object file.
     * Upon successful match, pushes an instance of {@link ObjFileData}
     *
     * @return parser for OBJ files
     */
    @SuppressWarnings("unchecked")
    @Override
    public Parser XPlaneFile() {
        return Header(REQUIRED_TYPE)
                .seq(Newline().star())
                .seq(ObjItems())
                .map((List<Object> input) -> {
                    ObjFileData objFileData = new ObjFileData((Header) input.get(0));
                    List<Object> items = (List<Object>) input.get(2);
                    items.forEach(o -> addItem(objFileData, o));
                    return objFileData;
                });
    }

    /**
     * Add a parsed item to the ObjFileData structure.
     *
     * @param objFileData the data structure to populate
     * @param o the parsed item (ObjAttribute, ObjDatum, or ObjCommand)
     */
    private static void addItem(ObjFileData objFileData, Object o) {
        if (o instanceof ObjAttribute oa) {
            objFileData.getAttributes().add(oa);
        }
        else if (o instanceof ObjDatum od) {
            objFileData.getData().add(od);
        }
        else if (o instanceof ObjCommand oc) {
            objFileData.getCommands().add(oc);
        }
        //ignore any other type
    }

    /**
     * Parse OBJ items (attributes, data, commands).
     *
     * @return parser for OBJ items
     */
    Parser ObjItems() {
        return
                    // match empty line
                    Newline()
                    // match line containing only whitespace
                    .or(WhiteSpace().seq(Newline()))
                    // match line containing only comment
                    .or(Comment().seq(Newline()))
                    // match line containing an attribute/datum/command, with an optional comment
                    .or(ObjAttribute().seq(WhiteSpace().optional()).seq(Comment().optional()).seq(Newline()).pick(0))
                    .or(ObjDatum().seq(WhiteSpace().optional()).seq(Comment().optional()).seq(Newline()).pick(0))
                    .or(ObjCommand().seq(WhiteSpace().optional()).seq(Comment().optional()).seq(Newline()).pick(0))
                .star();
    }

    //
    // Attributes
    //

    /**
     * Parse an OBJ attribute.
     *
     * @return parser for attributes
     */
    Parser ObjAttribute() {
        return Texture()
                .or(PointCounts())
                .or(OtherAttribute());
    }

    /**
     * Parse an unknown attribute type.
     *
     * @return parser for unknown attributes
     */
    Parser OtherAttribute() {
        return OtherAttributeType()
                .seq(Newline().neg().star())
                .map((List<String> input) -> new ObjUnknownAttr(input.get(0)))
                ;
    }

    /**
     * Parse an attribute type keyword.
     *
     * @return parser for attribute types
     */
    Parser OtherAttributeType() {
        return of("ATTR")
                .seq(noneOf(" \t").plus())
                .flatten();
    }

    /**
     * Parse a texture attribute.
     *
     * @return parser for texture attributes
     */
    Parser Texture() {
        return TextureType()
                .seq(WhiteSpace())
                .seq(FileName())
                .map((List<String> input) -> new ObjTexture(input.get(0), input.get(2)))
                ;
    }

    /**
     * Parse a texture type keyword.
     *
     * @return parser for texture types
     */
    Parser TextureType() {
        return of("TEXTURE_NORMAL")
                .or(of("TEXTURE_LIT"))
                .or(of("TEXTURE"));
    }

    /**
     * Parse a point counts attribute.
     *
     * @return parser for point counts
     */
    Parser PointCounts() {
        return of("POINT_COUNTS")
                .seq(WhiteSpace().seq(Number()).times(4));
    }


    //
    // Data
    //

    /**
     * Parse an OBJ datum item.
     *
     * @return parser for data items
     */
    Parser ObjDatum() {
        return Comment()
                .or(Vt())
                .or(VLine())
                .or(Idx())
                .or(Idx10())
                ;
    }

    /**
     * Parse a vertex (VT) data item.
     *
     * @return parser for vertex data
     */
    Parser Vt() {
        return of("VT")
                .seq(WhiteSpace().seq(Number()).times(8));
    }

    /**
     * Parse a vertex list (VLINE) data item.
     *
     * @return parser for vertex lists
     */
    Parser VLine() {
        return of("VLINE")
                .seq(WhiteSpace().seq(Number()).times(6));
    }

    /**
     * Parse an index (IDX) data item.
     *
     * @return parser for index data
     */
    Parser Idx() {
        return of("IDX")
                .seq(WhiteSpace()).seq(Number());
    }

    /**
     * Parse a 10-element index (IDX10) data item.
     *
     * @return parser for IDX10 data
     */
    Parser Idx10() {
        return of("IDX10")
                .seq(WhiteSpace().seq(Number()).times(10));
    }

    //
    // Commands
    //

    /**
     * Matches a single command. Upon success, pushes a {@link ObjCommand} item.
     *
     * @return parser for commands
     */
    Parser ObjCommand() {
        return Comment()
                .or(Tris())
                .or(Lines());
    }

    /**
     * Parse a TRIS (triangle) command.
     *
     * @return parser for TRIS commands
     */
    Parser Tris() {
        return of("TRIS")
                .seq(WhiteSpace().seq(Number()).times(2));
    }

    /**
     * Parse a LINES command.
     *
     * @return parser for LINES commands
     */
    Parser Lines() {
        return of("LINES")
                .seq(WhiteSpace().seq(Number()).times(2));
    }

    //
    // Common
    //

    /**
     * Parse a filename (any characters until newline).
     * Upon successful match, pushes the value as a String.
     *
     * @return parser for filenames
     */
    Parser FileName() {
        return noneOf("\r\n").plus().flatten();
    }

}
