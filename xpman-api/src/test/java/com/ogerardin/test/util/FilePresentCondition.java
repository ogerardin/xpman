package com.ogerardin.test.util;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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
        Annotation fileAnnotation = findFilePresenceAnnotation(annotated);
        if (fileAnnotation == null) {
            return ConditionEvaluationResult.enabled("No file annotation found");
        }
        String relativePath = valueOf(fileAnnotation);

        Path file = rootFolder.resolve(relativePath);
        if (Files.exists(file)) {
            return ConditionEvaluationResult.enabled("File present: " + relativePath);
        } else {
            return ConditionEvaluationResult.disabled("File not found: " + relativePath);
        }
    }

    /**
     * Traverses the element's annotations to find one that is meta-annotated with {@link FilePresenceCheck} (e.g.
     * {@link EnableOnAircraftPresent} / {@link EnableOnSceneryPresent}) and returns that annotation.
     */
    private static Annotation findFilePresenceAnnotation(AnnotatedElement annotated) {
        return Arrays.stream(annotated.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(FilePresenceCheck.class))
                .findFirst()
                .orElse(null);
    }

    /**
     * Extracts the {@code value()} from a file-presence annotation via reflection.
     */
    @SneakyThrows
    private static String valueOf(Annotation annotation) {
        try {
            return (String) annotation.annotationType().getMethod("value").invoke(annotation);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
