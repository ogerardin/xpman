package com.ogerardin.xplane.file.parboiled;

import com.ogerardin.xplane.file.StringParser;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.*;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

@SuppressWarnings({"rawtypes", "unchecked"})
@Data
@RequiredArgsConstructor
public class ParboiledParser<R extends XPlaneFileData, P extends XPlaneFileParserBase>
        implements StringParser<R> {

    /** Class of parser required to parse this file */
    @NonNull
    private final Class<P> parboiledParserClass;

    /**
     * Use a recovering parser ? If true, an instance of {@link org.parboiled.parserunners.RecoveringParseRunner}
     * will be used, otherwise an instance of {@link BasicParseRunner}.
     */
    private final boolean useRecoveringParser;

    @Getter(lazy = true) //defer parser creation until getParser() is called
    private final P parser = createParser();

    @Getter(lazy = true) //defer ParseRunner creation until getParseRunner() is called
    private final ParseRunner parseRunner = createParseRunner();


    @SneakyThrows
    private ParseRunner createParseRunner() {
        P parser = getParser();
        return useRecoveringParser ?
                new RecoveringParseRunner(parser.XPlaneFile()) : new BasicParseRunner(parser.XPlaneFile());
    }

    public ParboiledParser(@NonNull Class<P> parboiledParserClass) {
        this(parboiledParserClass, false);
    }

    private P createParser() {
        return Parboiled.createParser(Objects.requireNonNull(parboiledParserClass));
    }

    @Override
    public R parse(String contents) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ParseRunner<Object> parseRunner = getParseRunner();
        ParsingResult<Object> result = parseRunner.run(contents);
        return (R) result.resultValue;
    }



}
