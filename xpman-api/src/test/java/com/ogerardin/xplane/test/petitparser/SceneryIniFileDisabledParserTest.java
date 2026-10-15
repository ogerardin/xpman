package com.ogerardin.xplane.test.petitparser;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.file.petitparser.SceneryPacksIniParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.petitparser.parser.Parser;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryIniFileDisabledParserTest extends ParserTest<SceneryPackIniData> {

    @Test
    void testCanParseSceneryIniWithDisabledEntries() throws IOException, URISyntaxException {
        String fileContents = getResourceAsString("/scenery_packs_disabled.ini");
        Parser parser = new SceneryPacksIniParser().getParser();
        SceneryPackIniData result = runParser(fileContents, parser, false);

        // regression: entries after a SCENERY_PACK_DISABLED line used to be silently dropped
        assertThat(result.getItems(), contains(
                SceneryPackIniItem.of("Custom Scenery/A"),
                SceneryPackIniItem.of("Custom Scenery/B", true),
                SceneryPackIniItem.of("Custom Scenery/C"),
                SceneryPackIniItem.of("Custom Scenery/D", true),
                SceneryPackIniItem.of("*GLOBAL_AIRPORTS*")
        ));
    }
}
