package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.XPlaneFileData;
import com.ogerardin.xplane.file.grammar.XPlaneFileParser;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Parboiled;
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
@Data
@Slf4j
public abstract class XPlaneDataFile<P extends XPlaneFileParser, R extends XPlaneFileData> {

    @Getter
    private final Path file;

    /** Class of parser required to parse this file */
    private final Class<P> parserClass;

    /** Result of the parsing */
    @Getter(lazy = true)
    @ToString.Exclude
    private final R data = parse();

    public String getFileSpecVersion() {
        return getData().getHeader().getSpecVersion();
    }


    @SuppressWarnings("ConstantConditions")
    @SneakyThrows
    private R parse()  {
        log.debug("Parsing file {}", file);
        P parser = Parboiled.createParser(parserClass);
        byte[] bytes = Files.readAllBytes(file);
        String fileContents = new String(bytes, UTF_8);
//        ParsingResult<Object> result = new ReportingParseRunner<>(parser.XPlaneFile()).run(fileContents);
        ParsingResult<Object> result = new BasicParseRunner<>(parser.XPlaneFile()).run(fileContents);
//        log.debug("Done parsing {}", file);
        //noinspection unchecked
        return (R) result.resultValue;


    }

}
