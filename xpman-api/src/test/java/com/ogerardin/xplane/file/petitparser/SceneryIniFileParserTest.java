package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import org.junit.jupiter.api.Test;
import org.petitparser.parser.Parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class SceneryIniFileParserTest extends ParserTest<SceneryPackIniData> {

    @Test
    void testCanParseSceneryIni() throws IOException, URISyntaxException {
        String fileContents = getResourceAsString("/scenery_packs.ini");
        Parser parser = new SceneryPacksIniParser().getParser();
        SceneryPackIniData result = runParser(fileContents, parser, false);
        assertThat(result.getSceneryPackList(), hasItem(Paths.get("Custom Scenery/X-Plane Landmarks - Chicago")));
        assertThat(result.getSceneryPackList(), hasItem(Paths.get("Custom Scenery/Aerosoft - EDDF Frankfurt")));
    }

}