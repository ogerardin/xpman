package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.panels.menu.Label;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class IntrospectionHelper {

    public static List<Method> computeRelevantMethods(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredMethods())
                // skip if method is an Object method
                .filter(IntrospectionHelper::isNotObjectMethod)
                // skip non public or abstract methods
                .filter(method -> Modifier.isPublic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()))
                // skip setters/getters
                .filter(method -> !method.getName().startsWith("set") && !method.getName().startsWith("get") && !method.getName().startsWith("is"))
                .toList();
    }

    private static boolean isNotObjectMethod(Method method) {
        try {
            Object.class.getMethod(method.getName(), method.getParameterTypes());
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public static String getLabelForMethod(Method method) {
        var label = method.getAnnotation(Label.class);
        String text;
        if (label != null) {
            String expr = label.value();
            text = (String) SpelUtil.eval(expr, null);
        } else {
            // no @Label: try to make up something human-readable from the method name
            String[] words = StringUtils.splitByCharacterTypeCamelCase(method.getName());
            words[0] = StringUtils.capitalize(words[0]);
            text = String.join(" ", words);
        }
        return text;
    }

}
