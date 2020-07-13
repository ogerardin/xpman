package com.ogerardin.xplane.file.grammar;

import com.ogerardin.xplane.file.data.*;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Action;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

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
                Header(),
                // ObjFileData is pushed early on the stack and will be enriched by relevant rules
                push(new ObjFileData((Header) peek())),
                ZeroOrMore(Newline()),
                ObjItems(),
                EOI
        );
    }

    Rule JunkLine() {
        return Sequence(ZeroOrMore(NoneOf("\r\n")), Newline());
    }

    /**
     * Matches a X-Plane file header.
     * Upon successful match, pushes an instance of {@link Header}
     */
    Rule Header() {
        return Sequence(
                Origin(), JunkLine(),
                VersionSpec(), JunkLine(),
                FileType(), JunkLine(),
                swap3() && push(new Header((String) pop(), (String) pop(), (String) pop()))
        );
    }

    Rule ObjItems() {
        return Sequence(
                ObjAttributes(),
                ObjData(),
                ObjCommands()
        );
    }

    //
    // Attributes
    //

    Rule ObjAttributes() {
        return ZeroOrMore(
                FirstOf(
                        // match empty line
                        Newline(),
                        // match line containing only whitespace
                        Sequence(WhiteSpace(), Newline()),
                        // match line containing only comment
                        Sequence(Comment(), Newline()),
                        // match line containing an attribute, with an optional comment
                        Sequence(ObjAttribute(), Optional(WhiteSpace()), Optional(Comment()), Newline())
                )
        );
    }

    Rule ObjAttribute() {
        return FirstOf(
                Texture(),
                TextureLit(),
                PointCounts()
        );
    }

    Rule TextureLit() {
        return Sequence("TEXTURE_LIT", WhiteSpace(), FileName(),
                (Action<?>) context -> {
                    ObjTexture texture = new ObjTexture((String) pop());
                    ObjFileData fileData = (ObjFileData) peek();
                    fileData.getAttributes().add(texture);
                    return true;
                }
        );
    }

    Rule TextureNormal() {
        return Sequence("TEXTURE_NORMAL", WhiteSpace(), FileName(),
                (Action<?>) context -> {
                    ObjTexture texture = new ObjTexture((String) pop());
                    ObjFileData fileData = (ObjFileData) peek();
                    fileData.getAttributes().add(texture);
                    return true;
                }
        );
    }

    Rule Texture() {
        return Sequence(
                "TEXTURE", WhiteSpace(), FileName(),
                (Action<?>) context -> {
                    ObjTexture texture = new ObjTexture((String) pop());
                    ObjFileData fileData = (ObjFileData) peek();
                    fileData.getAttributes().add(texture);
                    return true;
                }
        );
    }

    Rule PointCounts() {
        return Sequence("POINT_COUNTS", WhiteSpace(), NTimes(4, Number(), WhiteSpace()));
    }


    //
    // Data
    //

    Rule ObjData() {
        return ZeroOrMore(
                FirstOf(
                        Newline(),
                        Sequence(WhiteSpace(), Newline()),
                        Sequence(Comment(), Newline()),
                        Sequence(ObjDatum(), Optional(WhiteSpace()), Optional(Comment()), Newline())
                )
        );
    }

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

    Rule ObjCommands() {
        return ZeroOrMore(
                FirstOf(
                        Newline(),
                        Sequence(WhiteSpace(), Newline()),
                        Sequence(Comment(), Newline()),
                        Sequence(ObjCommand(), Optional(WhiteSpace()), Optional(Comment()), Newline())
                )
        );
    }

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

    Rule Comment() {
        return Sequence('#', ZeroOrMore(NoneOf("\r\n")));
    }

    Rule CommentLine() {
        return Sequence(Comment(), Newline());
    }

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

    @SuppressSubnodes
    Rule Number() {
        return Sequence(
                Optional('-'),
                OneOrMore(Digit()),
                Optional(
                        '.',
                        OneOrMore(Digit())
                )
        );
    }

    Rule Newline() {
        return Sequence(Optional('\r'), '\n');
    }

    Rule PropertyValueChar() {
        return CharRange(' ', '~');
    }

    /**
     * Matches a property name.
     * Upon successful match, pushes the name as a String.
     */
    @SuppressSubnodes
    Rule PropertyName() {
        return Sequence(
                OneOrMore(PropertyNameChar()),
                push(match())
        );
    }

    Rule PropertyNameChar() {
        return FirstOf(Alphanumeric(), '_', ',', '/');
    }

    Rule Alphanumeric() {
        return FirstOf(Letter(), Digit());
    }

    Rule Spacechar() {
        return AnyOf(" \t");
    }

    Rule FileType() {
        return Sequence(
//                Sequence(Letter(), Letter(), Letter()),
                REQUIRED_TYPE,
                push(match())
        );
    }

    Rule VersionSpec() {
        return Sequence(
                Version(), push(match())
        );
    }

    Rule WhiteSpace() {
        return OneOrMore(Spacechar());
    }

    Rule Version() {
        return OneOrMore(Digit());
    }

    Rule Letter() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    Rule Digit() {
        return CharRange('0', '9');
    }

    Rule Origin() {
        return Sequence(
                AnyOf("IA"),
                push(match())
        );
    }

}
