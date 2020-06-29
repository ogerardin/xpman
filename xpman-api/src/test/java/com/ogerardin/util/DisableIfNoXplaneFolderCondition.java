package com.ogerardin.util;

import com.ogerardin.xplane.config.XPlaneInstance;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Path;

public class DisableIfNoXplaneFolderCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            Path xpRootFolder = XPlaneInstance.getDefaultXPRootFolder();
            return ConditionEvaluationResult.enabled("X-Plane root folder: " + xpRootFolder);
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled("Could not get X-Plane root folder: " + e.toString());
        }
    }
}