package com.ogerardin.test.util;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
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
            AnnotatedElement annotated = element.get();
            XPlaneVersionRequired required = findVersionRequired(annotated);
            if (required == null) {
                return ConditionEvaluationResult.enabled("No version annotation found");
            }
            XPlaneMajorVersion expectedVersion = required.value();

            if (xPlane.getMajorVersion() == expectedVersion) {
                return ConditionEvaluationResult.enabled("X-Plane " + expectedVersion + " found");
            } else {
                return ConditionEvaluationResult.disabled("X-Plane version is " + xPlane.getMajorVersion() + ", expected " + expectedVersion);
            }
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled("Could not determine X-Plane version: " + e);
        }
    }

    /**
     * Traverses the element's annotations to find one that is meta-annotated with
     * {@link XPlaneVersionRequired} (e.g. {@link EnableOnLocalXPlane11} /
     * {@link EnableOnLocalXPlane12}) and returns that meta-annotation.
     */
    private static XPlaneVersionRequired findVersionRequired(AnnotatedElement annotated) {
        return Arrays.stream(annotated.getAnnotations())
                .map(annotation -> annotation.annotationType().getAnnotation(XPlaneVersionRequired.class))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
