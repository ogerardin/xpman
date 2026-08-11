package com.ogerardin.test.util;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;
import java.nio.file.Path;
import java.util.Optional;

public class EnableOnLocalXPlaneVersionCondition implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<AnnotatedElement> element = context.getElement();
        if (element.isEmpty()) {
            return ConditionEvaluationResult.enabled("No element");
        }

        Path rootFolder;
        try {
            rootFolder = XPlaneTestUtil.getDefaultXPRootFolder();
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled("Could not find X-Plane installation: " + e);
        }

        try {
            XPlane xPlane = new XPlane(rootFolder);
            XPlaneMajorVersion expectedVersion;
            AnnotatedElement annotated = element.get();
            if (annotated.isAnnotationPresent(EnableOnLocalXPlane11.class)) {
                expectedVersion = XPlaneMajorVersion.XP11;
            } else if (annotated.isAnnotationPresent(EnableOnLocalXPlane12.class)) {
                expectedVersion = XPlaneMajorVersion.XP12;
            } else {
                return ConditionEvaluationResult.enabled("No version annotation found");
            }

            if (xPlane.getMajorVersion() == expectedVersion) {
                return ConditionEvaluationResult.enabled("X-Plane " + expectedVersion + " found");
            } else {
                return ConditionEvaluationResult.disabled("X-Plane version is " + xPlane.getMajorVersion() + ", expected " + expectedVersion);
            }
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled("Could not determine X-Plane version: " + e);
        }
    }
}
