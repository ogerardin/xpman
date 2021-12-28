package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.util.TimingExtension;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.parboiled.common.FileUtils;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;
import org.petitparser.utils.Tracer;

import java.io.IOException;
import java.net.URL;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(TimingExtension.class)
class VersionFileParserTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean TRACE = false;

    @Test
    void testCanParseVersions() throws IOException {

        final URL serverList = getClass().getResource("/server_list_11.txt");

        final byte[] bytes = FileUtils.readAllBytes(serverList.openStream());
        String fileContents = new String(bytes, US_ASCII);

//        Parser parser = new ServersFileParser().getParser();
        Parser parser = new ServersFileGrammer().build();

        if (TRACE) {
            parser = Tracer.on(parser, traceEvent -> log.debug(traceEvent.toString()));
        }

        final Result result = parser.parse(fileContents);

        if (result.isFailure()) {
                log.error("Parse error: {}", result.getMessage());
        }

        assertTrue(result.isSuccess());

//        ServersFileData resultValue = result.get();
        log.debug("resultValue={}", (Object) result.get());

//        log.debug(ParseTreeUtils.printNodeTree(result));
    }

}