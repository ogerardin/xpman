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

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class ObjFileParserTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean TRACE = false;

    @Test
    void testCanParseObj() throws IOException {

        Path objPath = XPlane.getDefaultXPRootFolder().resolve("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/EDDF_ASR_North01.obj");
//        Path objPath = XPlane.getDefaultXPRootFolder().resolve("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/v01partI12.obj");

        byte[] bytes = Files.readAllBytes(objPath);
        String fileContents = new String(bytes, US_ASCII);

        Parser parser = new ObjFileParser().getParser();
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