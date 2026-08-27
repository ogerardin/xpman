package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import lombok.extern.slf4j.Slf4j;
import org.petitparser.parser.Parser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.StringParser.of;

/**
 * Parser for X-Plane scenery_packs.ini files.
 *
 * <p>This parser handles the scenery_packs.ini file which contains a prioritized
 * list of scenery packages. Packages listed earlier have higher priority.</p>
 *
 * <h2>Grammar Overview</h2>
 * <pre>
 * SceneryPacksIniFile = Header("SCENERY") SceneryPack*
 * SceneryPack = "SCENERY_PACK " folderNameOrToken Newline
 * </pre>
 *
 * <h2>Example Content</h2>
 * <pre>
 * I
 * 1100 version
 * SCENERY_PACK *GLOBAL AIRPORTS*
 * SCENERY_PACK Custom Scenery/My Airport/
 * </pre>
 *
 * @author Olivier G.
 * @see SceneryPackIniData
 */
@Slf4j
public class SceneryPacksIniParser extends XPlaneFileParserBase<SceneryPackIniData> {

    /**
     * Required file type for scenery packs files.
     */
    static final String REQUIRED_TYPE = "SCENERY";

    /**
     * Parse a complete scenery_packs.ini file.
     * Upon successful match, pushes an instance of {@link SceneryPackIniData}
     *
     * @return parser for scenery packs files
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
     * Parse the list of scenery packs.
     * Upon successful match, pushes an instance of {@link SceneryPackIniData.SceneryPackList}
     *
     * @return parser for scenery pack list
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
     * Parse a single scenery pack entry.
     * Upon successful match, pushes a {@link SceneryPackIniItem} instance
     *
     * @return parser for scenery pack entries
     */
    Parser SceneryPack() {
        return of("SCENERY_PACK ")
                .seq(FolderNameOrToken())
                .seq(Newline())
                .map((List<Object> input) -> SceneryPackIniItem.of((String) input.get(1)))
                ;
    }

    /**
     * Parse a scenery folder name or token (e.g., "*GLOBAL AIRPORTS*").
     * Upon successful match, pushes the value as a String.
     *
     * @return parser for folder names or tokens
     */
    Parser FolderNameOrToken() {
        return noneOf("\r\n").plus().flatten();
    }

}
