package com.ogerardin.xplane.test.petitparser;

import com.ogerardin.xplane.file.data.servers.ServersFileData;
import com.ogerardin.xplane.file.petitparser.ServersFileParser;
import org.junit.jupiter.api.Test;
import org.petitparser.parser.Parser;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class VersionFileParserTest extends ParserTest<ServersFileData> {

    @Test
    void testCanParseVersions() throws IOException, URISyntaxException {
        String fileContents = getResourceAsString("/server_list_11.txt");
        Parser parser = new ServersFileParser().getParser();
        ServersFileData result = runParser(fileContents, parser, false);
        assertThat(result.getFinalVersion(), is("11.51r1"));
        assertThat(result.getBetaVersion(), is("11.51r1"));
    }

}