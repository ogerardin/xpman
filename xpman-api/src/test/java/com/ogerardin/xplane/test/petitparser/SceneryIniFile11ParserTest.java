package com.ogerardin.xplane.test.petitparser;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniData;
import com.ogerardin.xplane.file.petitparser.SceneryPacksIniParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.petitparser.parser.Parser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryIniFile11ParserTest extends ParserTest<SceneryPackIniData> {

    @Test
    void testCanParseSceneryIni() throws IOException, URISyntaxException {
        String fileContents = getResourceAsString("/scenery_packs_11.ini");
        Parser parser = new SceneryPacksIniParser().getParser();
        SceneryPackIniData result = runParser(fileContents, parser, false);
        assertThat(result.getItems(), hasItem(new PathSceneryPackIniItem(Path.of("Custom Scenery/X-Plane Landmarks - Chicago"))));
        assertThat(result.getItems(), hasItem(new PathSceneryPackIniItem(Path.of("Custom Scenery/Aerosoft - LPFR Faro"))));
    }

}