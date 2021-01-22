package com.ogerardin.xplane.file.grammar;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.data.obj.ObjFileData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.parboiled.Parboiled;
import org.parboiled.errors.ParseError;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.parboiled.common.Predicates.and;
import static org.parboiled.common.Predicates.not;
import static org.parboiled.support.Filters.rules;
import static org.parboiled.support.Filters.rulesBelow;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class ObjFileParserTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean TRACE = false;

    @Test
    public void testCanParseObj() throws IOException {

//        Path objPath = XPlane.getDefaultXPRootFolder().resolve("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/EDDF_ASR_North01.obj");
        Path objPath = XPlane.getDefaultXPRootFolder().resolve("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/v01partI12.obj");

        byte[] bytes = Files.readAllBytes(objPath);
        String fileContents = new String(bytes, US_ASCII);

        ObjFileParser parser = Parboiled.createParser(ObjFileParser.class);
        AbstractParseRunner<ObjFileData> runner;
        if (TRACE) {
            runner = new TracingParseRunner<ObjFileData>(parser.XPlaneFile())
                    .withFilter(
                            and(
                                    rules(parser.ObjAttribute(), parser.ObjCommand(), parser.ObjDatum()),
                                    not(rulesBelow(parser.ObjAttribute(), parser.ObjCommand(), parser.ObjDatum()))
                            )
                    )
                    .withLog(log::debug);
        } else {
//            runner = new ReportingParseRunner<ObjFileData>(parser.XPlaneFile());
            runner = new RecoveringParseRunner<ObjFileData>(parser.XPlaneFile());
        }

        ParsingResult<ObjFileData> result = runner.run(fileContents);

        if (!result.matched) {
            for (ParseError parseError : result.parseErrors) {
                log.error("Parse error: {}", parseError.getErrorMessage());
            }

        }

        assertTrue(result.matched);

        ObjFileData resultValue = result.resultValue;
        log.debug("resultValue={}", resultValue);

//        log.debug(ParseTreeUtils.printNodeTree(result));
    }

}