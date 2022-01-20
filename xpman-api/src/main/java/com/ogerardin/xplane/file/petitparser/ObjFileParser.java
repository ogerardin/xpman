package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.obj.*;
import lombok.extern.slf4j.Slf4j;
import org.petitparser.parser.Parser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.StringParser.of;

/**
 * Very partial implementation of a parser for OBJ8 file structure.
 * Reference: https://developer.x-plane.com/article/obj8-file-format-specification/#OBJECT_SYNTAX
 */
@Slf4j
public class ObjFileParser extends XPlaneFileParserBase<ObjFileData> {

    static final String REQUIRED_TYPE = "OBJ";

    /**
     * Matches a X-Plane scenery object file.
     * Upon successful match, pushes an instance of {@link ObjFileData}
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

    private static void addItem(ObjFileData objFileData, Object o) {
        if (o instanceof ObjAttribute) {
            objFileData.getAttributes().add((ObjAttribute) o);
        }
        else if (o instanceof ObjDatum) {
            objFileData.getData().add((ObjDatum) o);
        }
        else if (o instanceof ObjCommand) {
            objFileData.getCommands().add((ObjCommand) o);
        }
        //ignore any other type
    }

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

    Parser ObjAttribute() {
        return Texture()
                .or(PointCounts())
                .or(OtherAttribute());
    }

    Parser OtherAttribute() {
        return OtherAttributeType()
                .seq(Newline().neg().star())
                .map((List<String> input) -> new ObjUnknownAttr(input.get(0)))
                ;
    }

    Parser OtherAttributeType() {
        return of("ATTR")
                .seq(noneOf(" \t").plus())
                .flatten();
    }

    Parser Texture() {
        return TextureType()
                .seq(WhiteSpace())
                .seq(FileName())
                .map((List<String> input) -> new ObjTexture(input.get(0), input.get(2)))
                ;
    }

    Parser TextureType() {
        return of("TEXTURE_NORMAL")
                .or(of("TEXTURE_LIT"))
                .or(of("TEXTURE"));
    }

    Parser PointCounts() {
        return of("POINT_COUNTS")
                .seq(WhiteSpace().seq(Number()).times(4));
    }


    //
    // Data
    //

    Parser ObjDatum() {
        return Comment()
                .or(Vt())
                .or(VLine())
                .or(Idx())
                .or(Idx10())
                ;
    }

    Parser Vt() {
        return of("VT")
                .seq(WhiteSpace().seq(Number()).times(8));
    }

    Parser VLine() {
        return of("VLINE")
                .seq(WhiteSpace().seq(Number()).times(6));
    }

    Parser Idx() {
        return of("IDX")
                .seq(WhiteSpace()).seq(Number());
    }

    Parser Idx10() {
        return of("IDX10")
                .seq(WhiteSpace().seq(Number()).times(10));
    }

    //
    // Commands
    //

    /**
     * Matches a single command. Uption success, pushes a {@link ObjCommand} item.
     */
    Parser ObjCommand() {
        return Comment()
                .or(Tris())
                .or(Lines());
    }

    Parser Tris() {
        return of("TRIS")
                .seq(WhiteSpace().seq(Number()).times(2));
    }

    Parser Lines() {
        return of("LINES")
                .seq(WhiteSpace().seq(Number()).times(2));
    }

    //
    // Common
    //

    /**
     * Upon successful match, pushes the value as a String.
     */
    Parser FileName() {
        return noneOf("\r\n").plus().flatten();
    }

}
