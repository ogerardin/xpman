package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.XPlaneFileData;
import com.ogerardin.xplane.file.grammar.AcfFileParser;
import com.ogerardin.xplane.file.grammar.XPlaneFileParser;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a generic X-Plane file.
 * @param <P> parser class
 * @param <R> type of the result produced by the parser
 */
@SuppressWarnings("rawtypes")
@Data
@Slf4j
@RequiredArgsConstructor
public abstract class XPlaneDataFile<P extends XPlaneFileParser, R extends XPlaneFileData> {

    private static final Class<BasicParseRunner> DEFAULT_PARSER_RUNNER_CLASS = BasicParseRunner.class;

    @Getter
    private final Path file;

    /** Class of parser required to parse this file */
    private final Class<P> parserClass;

    private final Class<? extends AbstractParseRunner> parserRunnerClass;

    /** Result of the parsing */
    @Getter(lazy = true)
    @ToString.Exclude
    private final R data = parse();

    public XPlaneDataFile(Path file, Class<P> parserClass) {
        this(file, parserClass, DEFAULT_PARSER_RUNNER_CLASS);
    }

    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }


    @SuppressWarnings({"ConstantConditions", "unchecked"})
    @SneakyThrows
    private R parse()  {
        log.debug("Parsing file {}", file);
        P parser = Parboiled.createParser(parserClass);
        byte[] bytes = Files.readAllBytes(file);
        String fileContents = new String(bytes, UTF_8);
//        ParsingResult<Object> result = new ReportingParseRunner<>(parser.XPlaneFile()).run(fileContents);
//        ParsingResult<Object> result = new BasicParseRunner<>(parser.XPlaneFile()).run(fileContents);
        final AbstractParseRunner<Object> parserRunner = parserRunnerClass.getConstructor(Rule.class).newInstance(parser.XPlaneFile());
        ParsingResult<Object> result = parserRunner.run(fileContents);
//        log.debug("Done parsing {}", file);
        //noinspection unchecked
        return (R) result.resultValue;


    }

}
