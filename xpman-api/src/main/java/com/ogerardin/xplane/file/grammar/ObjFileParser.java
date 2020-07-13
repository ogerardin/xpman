package com.ogerardin.xplane.file.grammar;

import com.ogerardin.xplane.file.data.AcfFileData;
import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.ObjFileData;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

//@BuildParseTree
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
                ZeroOrMore(Newline()),
                ObjItems()
//                swap() && push(new AcfFileData((Header) pop(), (AcfFileData.AcfProperties) pop()))
//                Junk(),
//                EOI
        );
    }

    Rule Junk() {
        return ZeroOrMore(JunkLine());
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
                        Newline(),
                        Sequence(WhiteSpace(), Newline()),
                        Sequence(Comment(), Newline()),
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
        return Sequence("TEXTURE_LIT", WhiteSpace(), FileName());
    }

    Rule Texture() {
        return Sequence("TEXTURE", WhiteSpace(), FileName());
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

    Rule FileName() {
        return PropertyValue();
    }

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

    /**
     * Matches a property value.
     * Upon successful match, pushes the value as a String.
     */
    @SuppressSubnodes
    Rule PropertyValue() {
        return Sequence(
//                OneOrMore(PropertyValueChar()),
                OneOrMore(NoneOf("\r\n")),
                push(match())
        );
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
