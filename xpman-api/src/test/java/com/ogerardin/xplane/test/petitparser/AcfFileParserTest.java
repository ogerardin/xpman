package com.ogerardin.xplane.test.petitparser;

import com.ogerardin.test.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.acf.AcfFileData;
import com.ogerardin.xplane.file.petitparser.AcfFileParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.petitparser.parser.Parser;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class AcfFileParserTest extends ParserTest<AcfFileData> {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean TRACE = false;

    @Test
    void testCanParseAcf1() throws IOException {
        String fileContents = getXPlaneFileContents("Aircraft/Laminar Research/Boeing B737-800/b738.acf");
        Parser parser = new AcfFileParser().getParser();
        AcfFileData result = runParser(fileContents, parser, false);
        assertThat(result.getProperties().get("acf/_descrip"), is("Boeing 737-800"));
    }

    @Test
    void testCanParseAcf2() throws IOException {
        String fileContents = getXPlaneFileContents("Aircraft/YAK-55M/YAK-55M.acf");
        Parser parser = new AcfFileParser().getParser();
        AcfFileData result = runParser(fileContents, parser, false);
        assertThat(result.getProperties().get("acf/_descrip"), is("YAK-55M"));
    }

    @Test
    void testCanParseAcf3() throws IOException {
        String fileContents = getXPlaneFileContents("Aircraft/RafaleC_solo_display/RafaleC.acf");
        Parser parser = new AcfFileParser().getParser();
        AcfFileData result = runParser(fileContents, parser, false);
        assertThat(result.getProperties().get("acf/_descrip"), is("Rafale Solo Display for X-Plane 11"));
    }

}