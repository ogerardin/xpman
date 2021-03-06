package com.ogerardin.xpman.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Collections;
import java.util.Map;

/**
 * Helper class to invoke expressions written in the Spring Expression Language (SpEL).
 */
@UtilityClass
@Slf4j
public class SpelUtil {

    /**
     * Returns the result of evaluating the specified expression as SpEL with the specified context root and no
     * variables.
     */
    public Object eval(String expr, Object contextRoot) {
        return eval(expr, contextRoot, Collections.emptyMap());
    }

    /**
     * Returns the result of evaluating the specified expression as SpEL with the specified context root and
     * a set of variables.
     */
    public Object eval(String expr, Object contextRoot, Map<String, Object> variables) {
        log.debug("Evaluating '{}' with context root {} and variables {}", expr, contextRoot, variables);
        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression(expr);
        StandardEvaluationContext context= new StandardEvaluationContext(contextRoot);
        context.setVariables(variables);
        Object result = expression.getValue(context);
        log.debug("  result: {}", result);
        return result;
    }

}
