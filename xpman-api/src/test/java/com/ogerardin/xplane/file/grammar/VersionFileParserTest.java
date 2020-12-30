package com.ogerardin.xplane.file.grammar;

import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.parboiled.Parboiled;
import org.parboiled.common.FileUtils;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.IOException;
import java.net.URL;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.parboiled.common.Predicates.and;

@Slf4j
@ExtendWith(TimingExtension.class)
class VersionFileParserTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean TRACE = false;

    @Test
    public void testCanParseVersions() throws IOException {

        final URL serverList = getClass().getResource("/server_list_11.txt");

        final byte[] bytes = FileUtils.readAllBytes(serverList.openStream());
        String fileContents = new String(bytes, US_ASCII);

        ServersFileParser parser = Parboiled.createParser(ServersFileParser.class);
        AbstractParseRunner<ServersFileData> runner;
        if (TRACE) {
            runner = new TracingParseRunner<ServersFileData>(parser.XPlaneFile())
                    .withLog(log::debug);
        } else {
//            runner = new ReportingParseRunner<ObjFileData>(parser.XPlaneFile());
            runner = new RecoveringParseRunner<ServersFileData>(parser.XPlaneFile());
        }

        ParsingResult<ServersFileData> result = runner.run(fileContents);

        if (!result.matched) {
            for (ParseError parseError : result.parseErrors) {
                log.error("Parse error: {}", parseError.getErrorMessage());
            }

        }

        assertTrue(result.matched);

        ServersFileData resultValue = result.resultValue;
        log.debug("resultValue={}", resultValue);

//        log.debug(ParseTreeUtils.printNodeTree(result));
    }

}