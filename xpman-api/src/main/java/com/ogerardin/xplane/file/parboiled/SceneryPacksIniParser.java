package com.ogerardin.xplane.file.parboiled;

import com.ogerardin.xplane.file.data.*;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class SceneryPacksIniParser extends XPlaneFileParserBase {

    static final String REQUIRED_TYPE = "SCENERY";

    /**
     * Upon successful match, pushes an instance of {@link SceneryPackIniData}
     */
    @Override
    public Rule XPlaneFile() {
        return Sequence(
                Header(REQUIRED_TYPE),
                ZeroOrMore(Newline()),
                SceneryPacks(),
                swap() && push(new SceneryPackIniData((Header) pop(), (SceneryPackIniData.SceneryPackList) pop()))
//                Junk(),
//                EOI
        );
    }

    /**
     * Upon successful match, pushes an instrance of {@link SceneryPackIniData.SceneryPackList}
     */
    @SuppressWarnings({"Convert2Lambda", "rawtypes"})
    Rule SceneryPacks() {
        return Sequence(
                push(new SceneryPackIniData.SceneryPackList()),
                ZeroOrMore(
                        Sequence(
                                SceneryPack(),
                                new Action() {
                                    @Override
                                    public boolean run(Context context) {
                                        Path sceneryPack = (Path) pop();
                                        SceneryPackIniData.SceneryPackList sceneryPacks = (SceneryPackIniData.SceneryPackList) peek();
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

}
