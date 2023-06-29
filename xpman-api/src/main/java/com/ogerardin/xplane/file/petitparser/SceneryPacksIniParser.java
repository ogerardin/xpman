package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import lombok.extern.slf4j.Slf4j;
import org.petitparser.parser.Parser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.StringParser.of;

@Slf4j
public class SceneryPacksIniParser extends XPlaneFileParserBase<SceneryPackIniData> {

    static final String REQUIRED_TYPE = "SCENERY";

    /**
     * Upon successful match, pushes an instance of {@link SceneryPackIniData}
     */
    @Override
    public Parser XPlaneFile() {
        return Header(REQUIRED_TYPE)
                .seq(Newline().star())
                .seq(SceneryPacks())
                .map((List<Object> input) -> new SceneryPackIniData((Header) input.get(0), (SceneryPackIniData.SceneryPackList) input.get(2)))
                ;
    }

    /**
     * Upon successful match, pushes an instrance of {@link SceneryPackIniData.SceneryPackList}
     */
    Parser SceneryPacks() {
        return SceneryPack().star()
                .map((List<SceneryPackIniItem> input) -> {
                    final SceneryPackIniData.SceneryPackList items = new SceneryPackIniData.SceneryPackList();
                    items.addAll(input);
                    return items;
                })
                ;
    }

    /**
     * Matches a line with a scenery reference.
     * Upon successful match, pushes a {@link SceneryPackIniItem} instance
     */
    Parser SceneryPack() {
        return of("SCENERY_PACK ")
                .seq(FolderNameOrToken())
                .seq(Newline())
                .map((List<Object> input) -> SceneryPackIniItem.of((String) input.get(1)))
                ;
    }

    /**
     * Matches a scenery folder or token e.g. "*GLOBAL AIRPORTS*"
     * Upon successful match, pushes the value as a String.
     */
    Parser FolderNameOrToken() {
        return noneOf("\r\n").plus().flatten();
    }

}
