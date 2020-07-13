package com.ogerardin.xplane.file.grammar;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.config.XPlaneInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
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
    private final boolean TRACE = true;

    @Test
    public void testCanParseObj() throws IOException {

        Path objPath = XPlaneInstance.getDefaultXPRootFolder().resolve("Custom Scenery/Aerosoft - EDDF Frankfurt/Objects/Airport/EDDF_ASR_North01.obj");

        byte[] bytes = Files.readAllBytes(objPath);
        String fileContents = new String(bytes, US_ASCII);

        ObjFileParser parser = Parboiled.createParser(ObjFileParser.class);
        ReportingParseRunner<?> runner;
        if (TRACE) {
            runner = new TracingParseRunner<>(parser.XPlaneFile())
                    .withFilter(
                            and(
                                    rules(parser.ObjAttribute(), parser.ObjCommand(), parser.ObjDatum()),
                                    not(rulesBelow(parser.ObjAttribute(), parser.ObjCommand(), parser.ObjDatum()))
                            )
                    )
//                    .withFilter(rules(parser.Number()))
                    .withLog(log::debug);
        } else {
            runner = new ReportingParseRunner<>(parser.XPlaneFile());
        }

        ParsingResult<?> result = runner.run(fileContents);

        assertTrue(result.matched);

//        log.debug(ParseTreeUtils.printNodeTree(result));
    }

}