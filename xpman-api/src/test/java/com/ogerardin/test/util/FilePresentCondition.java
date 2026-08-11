package com.ogerardin.test.util;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FilePresentCondition implements ExecutionCondition {
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

        AnnotatedElement annotated = element.get();
        String relativePath = null;
        if (annotated.isAnnotationPresent(EnableOnAircraftPresent.class)) {
            relativePath = annotated.getAnnotation(EnableOnAircraftPresent.class).value();
        } else if (annotated.isAnnotationPresent(EnableOnSceneryPresent.class)) {
            relativePath = annotated.getAnnotation(EnableOnSceneryPresent.class).value();
        }
        if (relativePath == null) {
            return ConditionEvaluationResult.enabled("No file annotation found");
        }

        Path file = rootFolder.resolve(relativePath);
        if (Files.exists(file)) {
            return ConditionEvaluationResult.enabled("File present: " + relativePath);
        } else {
            return ConditionEvaluationResult.disabled("File not found: " + relativePath);
        }
    }
}
