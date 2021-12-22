package com.ogerardin.xplane.file.parboiled;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.XPlane;
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.parboiled.common.Predicates.and;
import static org.parboiled.common.Predicates.not;
import static org.parboiled.support.Filters.rules;
import static org.parboiled.support.Filters.rulesBelow;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class AcfFileParserTest {

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean TRACE = true;

    @Test
    void testCanParseAcf() throws IOException {

        Path acfPath = XPlane.getDefaultXPRootFolder().resolve("Aircraft/Laminar Research/Boeing B737-800/b738.acf");
//        Path acfPath = getXPRootFolder().resolve("Aircraft/YAK-55M/YAK-55M.acf");
//        Path acfPath = getXPRootFolder().resolve("Aircraft/RafaleC_solo_display/RafaleC.acf");

        byte[] bytes = Files.readAllBytes(acfPath);
        String fileContents = new String(bytes, UTF_8);

        AcfFileParser parser = Parboiled.createParser(AcfFileParser.class);
        ReportingParseRunner<?> runner;
        if (TRACE) {
            runner = new TracingParseRunner<>(parser.XPlaneFile())
                    .withFilter(and(rules(parser.PropertyName()), not(rulesBelow(parser.PropertyName()))))
                    .withLog(log::debug);
        } else {
            runner = new ReportingParseRunner<>(parser.XPlaneFile());
        }

        ParsingResult<?> result = runner.run(fileContents);


        assertTrue(result.matched);

//        log.debug(ParseTreeUtils.printNodeTree(result));
    }

}