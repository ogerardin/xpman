package com.ogerardin.xplane.file.grammar;

import com.ogerardin.xplane.file.data.*;
import com.ogerardin.xplane.file.data.obj.ObjCommand;
import com.ogerardin.xplane.file.data.obj.ObjFileData;
import com.ogerardin.xplane.file.data.obj.ObjTexture;
import com.ogerardin.xplane.file.data.obj.ObjUnknownAttr;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Action;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

/**
 * Parser for OBJ8 file structure.
 * Reference: https://developer.x-plane.com/article/obj8-file-format-specification/#OBJECT_SYNTAX
 */
@Slf4j
public class ObjFileParser extends XPlaneFileParser {

    static final String REQUIRED_TYPE = "OBJ";

    /**
     * Matches a X-Plane scenery object file.
     * Upon successful match, pushes an instance of {@link ObjFileData}
     */
    @Override
    public Rule XPlaneFile() {
        return Sequence(
                Header(REQUIRED_TYPE),
                // ObjFileData is pushed early on the stack and will be enriched by relevant rules
                push(new ObjFileData((Header) peek())),
                ZeroOrMore(Newline()),
                ObjItems(),
                EOI
        );
    }

    Rule ObjItems() {
        return ZeroOrMore(
                FirstOf(
                        // match empty line
                        Newline(),
                        // match line containing only whitespace
                        Sequence(WhiteSpace(), Newline()),
                        // match line containing only comment
                        Sequence(Comment(), Newline()),
                        // match line containing an attribute/datum/command, with an optional comment
                        Sequence(ObjAttribute(), Optional(WhiteSpace()), Optional(Comment()), Newline()),
                        Sequence(ObjDatum(), Optional(WhiteSpace()), Optional(Comment()), Newline()),
                        Sequence(ObjCommand(), Optional(WhiteSpace()), Optional(Comment()), Newline())
                )
        );
    }

    //
    // Attributes
    //

    Rule ObjAttribute() {
        return FirstOf(
                Texture(),
                PointCounts(),
                OtherAttribute()
        );
    }

    Rule OtherAttribute() {
        return Sequence(
                OtherAttributeType(), JunkLine(),
                (Action<?>) context -> {
                    ObjUnknownAttr unkownAttr = new ObjUnknownAttr((String) pop());
                    ObjFileData fileData = (ObjFileData) peek();
                    fileData.getAttributes().add(unkownAttr);
                    return true;
                }
        );
    }

    Rule OtherAttributeType() {
        return Sequence(
                Sequence("ATTR_", OneOrMore(NoneOf(" \t"))), push(match())
        );
    }

    Rule Texture() {
        return Sequence(
                TextureType(), WhiteSpace(), FileName(),
                (Action<?>) context -> {
                    swap();
                    ObjTexture texture = new ObjTexture((String) pop(), (String) pop());
                    ObjFileData fileData = (ObjFileData) peek();
                    fileData.getAttributes().add(texture);
                    return true;
                }
        );
    }

    Rule TextureType() {
        return Sequence(FirstOf("TEXTURE_NORMAL", "TEXTURE_LIT", "TEXTURE"), push(match()));
    }

    Rule PointCounts() {
        return Sequence("POINT_COUNTS", WhiteSpace(), NTimes(4, Number(), WhiteSpace()));
    }


    //
    // Data
    //

    Rule ObjDatum() {
        return FirstOf(
                Comment(),
                Vt(),
                VLine(),
                Idx(),
                Idx10()
        );
    }

    Rule Vt() {
        return Sequence("VT", WhiteSpace(), NTimes(8, Number(), WhiteSpace()));
    }

    Rule VLine() {
        return Sequence("VLINE", WhiteSpace(), NTimes(6, Number(), WhiteSpace()));
    }

    Rule Idx() {
        return Sequence("IDX", WhiteSpace(), Number());
    }

    Rule Idx10() {
        return Sequence("IDX10", WhiteSpace(), NTimes(10, Number(), WhiteSpace()));
    }

    //
    // Commands
    //

    /**
     * Matches a single command. Uption success, pushes a {@link ObjCommand} item.
     */
    Rule ObjCommand() {
        return FirstOf(
                Comment(),
                Tris(),
                Lines()
        );
    }

    Rule Tris() {
        return Sequence("TRIS", WhiteSpace(), NTimes(2, Number(), WhiteSpace()));
    }

    Rule Lines() {
        return Sequence("LINES", WhiteSpace(), NTimes(2, Number(), WhiteSpace()));
    }

    //
    // Common
    //

    /**
     * Upon successful match, pushes the value as a String.
     */
    @SuppressSubnodes
    Rule FileName() {
        return Sequence(
                OneOrMore(NoneOf("\r\n")),
                push(match())
        );
    }

}
