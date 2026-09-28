package com.ogerardin.xpman.scenery_organizer;

import com.google.gson.stream.JsonWriter;
import com.ogerardin.xplane.scenery.SceneryPackage;

import java.io.IOException;

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
