package com.ogerardin.xpman.test;

import org.junit.jupiter.api.Test;

import static com.ogerardin.xpman.util.SpelUtil.eval;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SpelMultilineStringTest {

    @Test
    void stringLiteralCanContainLineBreaks() {
        String expression = """
                'first line

                third line'
                """;

        assertThat(eval(expression, null), is("first line\n\nthird line"));
    }
}
