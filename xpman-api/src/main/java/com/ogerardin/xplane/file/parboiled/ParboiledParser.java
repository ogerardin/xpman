package com.ogerardin.xplane.file.parboiled;

import com.ogerardin.xplane.file.StringParser;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.AbstractParseRunner;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;

import java.lang.reflect.InvocationTargetException;

@Data
@RequiredArgsConstructor
public class ParboiledParser<R extends XPlaneFileData, P extends XPlaneFileParserBase>
        implements StringParser<R> {

    private static final Class<BasicParseRunner> DEFAULT_PARSER_RUNNER_CLASS = BasicParseRunner.class;

    /** Class of parser required to parse this file */
    @NonNull
    private final Class<P> parboiledParserClass;

    @NonNull
    private final Class<? extends AbstractParseRunner> parserRunnerClass;

    public ParboiledParser(@NonNull Class<P> parboiledParserClass) {
        this(parboiledParserClass, DEFAULT_PARSER_RUNNER_CLASS);
    }

    @Override
    public R parse(String contents) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        P parser = Parboiled.createParser(parboiledParserClass);
        final AbstractParseRunner<Object> parserRunner = parserRunnerClass.getConstructor(Rule.class).newInstance(parser.XPlaneFile());
        ParsingResult<Object> result = parserRunner.run(contents);
        //noinspection unchecked
        return (R) result.resultValue;
    }



}
