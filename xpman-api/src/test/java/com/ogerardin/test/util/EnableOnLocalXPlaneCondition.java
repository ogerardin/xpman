package com.ogerardin.test.util;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EnableOnLocalXPlaneCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            return ConditionEvaluationResult.enabled("X-Plane root folder: " + XPlaneTestUtil.getDefaultXPRootFolder());
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled("Could not find X-Plane installation: " + e);
        }
    }
}
