package com.ogerardin.xplane.file.grammar;

import com.ogerardin.xplane.file.data.*;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

import java.nio.file.Path;
import java.nio.file.Paths;

//@BuildParseTree
@Slf4j
public class SceneryPacksIniParser extends XPlaneFileParser {

    static final String REQUIRED_TYPE = "SCENERY";

    /**
     * Upon successful match, pushes an instance of {@link com.ogerardin.xplane.file.data.SceneryPackIniData}
     */
    @Override
    public Rule XPlaneFile() {
        return Sequence(
                Header(),
                ZeroOrMore(Newline()),
                SceneryPacks(),
                swap() && push(new SceneryPackIniData((Header) pop(), (SceneryPackList) pop()))
//                Junk(),
//                EOI
        );
    }

    Rule Junk() {
        return ZeroOrMore(JunkLine());
    }

    Rule JunkLine() {
        return Sequence(ZeroOrMore(ANY), Newline());
    }

    /**
     * Upon successful match, pushes an instance of {@link Header}
     */
    Rule Header() {
        return Sequence(
                Origin(), WhiteSpace(), Newline(),
                VersionSpec(), WhiteSpace(), Newline(),
                FileType(), WhiteSpace(), Newline(),
                swap3() && push(new Header((String) pop(), (String) pop(), (String) pop()))
        );
    }

    /**
     * Upon successful match, pushes an instrance of {@link SceneryPackList}
     */
    @SuppressWarnings({"Convert2Lambda", "rawtypes"})
    Rule SceneryPacks() {
        return Sequence(
                push(new SceneryPackList()),
                ZeroOrMore(
                        Sequence(
                                SceneryPack(),
                                new Action() {
                                    @Override
                                    public boolean run(Context context) {
                                        Path sceneryPack = (Path) pop();
                                        SceneryPackList sceneryPacks = (SceneryPackList) peek();
                                        sceneryPacks.add(sceneryPack);
                                        return true;
                                    }
                                }
                        )
                )
        );
    }

    /**
     * Matches a line with a scenery reference.
     * Upon successful match, pushes a {@link java.nio.file.Path} instance
     */
    Rule SceneryPack() {
        return Sequence("SCENERY_PACK", ' ',
                FolderName(),
                push(Paths.get((String) pop())),
                Newline());
    }

    Rule Newline() {
        return Sequence(Optional('\r'), '\n');
    }

    /**
     * Matches a scenery folder.
     * Upon successful match, pushes the value as a String.
     */
    @SuppressSubnodes
    Rule FolderName() {
        return Sequence(
                OneOrMore(NoneOf("\r\n")),
                push(match())
        );
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
                Version(), push(match()),
                " ",
                AnyOf("vV"), "ersion");
    }

    Rule WhiteSpace() {
        return ZeroOrMore(Spacechar());
    }

    Rule Version() {
        return Sequence(Digit(), Digit(), Digit(), Digit());
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
