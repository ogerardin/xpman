package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;
import org.petitparser.utils.Tracer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class AcfFileParserTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean TRACE = false;

    @Test
    void testCanParseAcf() throws IOException {

        Path acfPath = XPlane.getDefaultXPRootFolder().resolve("Aircraft/Laminar Research/Boeing B737-800/b738.acf");
//        Path acfPath = getXPRootFolder().resolve("Aircraft/YAK-55M/YAK-55M.acf");
//        Path acfPath = getXPRootFolder().resolve("Aircraft/RafaleC_solo_display/RafaleC.acf");

        byte[] bytes = Files.readAllBytes(acfPath);
        String fileContents = new String(bytes, UTF_8);

        Parser parser = new AcfFileParser().getParser();

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