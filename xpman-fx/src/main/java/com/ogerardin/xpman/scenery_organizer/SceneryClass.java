package com.ogerardin.xpman.scenery_organizer;

import com.google.gson.stream.JsonWriter;
import com.ogerardin.xplane.scenery.SceneryPackage;

import java.io.IOException;

/**
 * Categorization rule for scenery packages: a named class that matches scenery by name or file signals.
 * <p>
 * Implementations include {@link BuiltinSceneryClass} (file-based detection, e.g. airports and libraries)
 * and {@link RegexSceneryClass} (name-based regex patterns, e.g. overlay or mesh scenery). The {@code matches}
 * method determines whether a given {@link com.ogerardin.xplane.scenery.SceneryPackage} belongs to this class.
 * Built-in classes are matched after regex classes (more specific first), and control whether a class is
 * editable or can be deleted by the user.
 */
public interface SceneryClass {

    String getName();

    boolean matches(SceneryPackage sceneryPackage);

    default String getRegex() {
        return null;
    }

    default boolean isEditable() {
        return true;
    }

    default boolean isBuiltin() {
        return false;
    }

    void writeTo(JsonWriter out) throws IOException;
}
