package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.XPlaneFileData;
import com.ogerardin.xplane.file.grammar.XPlaneFileParser;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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
public abstract class XPlaneFile<P extends XPlaneFileParser, R extends XPlaneFileData> {

    private static final Class<BasicParseRunner> DEFAULT_PARSER_RUNNER_CLASS = BasicParseRunner.class;

    @Getter
    private final Path file;

    /** Class of parser required to parse this file */
    @NonNull
    private final Class<P> parserClass;

    @NonNull
    private final Class<? extends AbstractParseRunner> parserRunnerClass;

    /** Result of the parsing */
    @Getter(lazy = true)
    @ToString.Exclude
    private final R data = parse();

    public XPlaneFile(Path file, Class<P> parserClass) {
        this(file, parserClass, DEFAULT_PARSER_RUNNER_CLASS);
    }

    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }


    @SneakyThrows
    private R parse()  {
        Objects.requireNonNull(file);
        log.debug("Parsing file {}", file);
        byte[] bytes = Files.readAllBytes(file);
        String fileContents = new String(bytes, UTF_8);
        return parse(fileContents);
    }

    @SuppressWarnings("unchecked")
    public R parse(String contents) throws InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException, NoSuchMethodException {
        P parser = Parboiled.createParser(parserClass);
        final AbstractParseRunner<Object> parserRunner = parserRunnerClass.getConstructor(Rule.class).newInstance(parser.XPlaneFile());
        ParsingResult<Object> result = parserRunner.run(contents);
        return (R) result.resultValue;
    }

}
