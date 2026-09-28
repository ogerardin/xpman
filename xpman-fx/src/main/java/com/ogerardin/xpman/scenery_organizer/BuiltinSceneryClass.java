package com.ogerardin.xpman.scenery_organizer;

import com.google.gson.stream.JsonWriter;
import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * Built-in scenery classes that use file-content signals rather than folder-name regex.
 * Each constant implements {@link #matches} polymorphically.
 */
@Getter
@RequiredArgsConstructor
public enum BuiltinSceneryClass implements SceneryClass {

    AIRPORT("Airport") {
        @Override
        public boolean matches(SceneryPackage pkg) {
            return pkg.getHasAirport();
        }
    },

    LIBRARY("Library") {
        @Override
        public boolean matches(SceneryPackage pkg) {
            return pkg.isLibrary();
        }
    },

    OTHER("Other") {
        @Override
        public boolean matches(SceneryPackage pkg) {
            return true;
        }
    };

    private final String label;

    @Override
    public String getName() {
        return label;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public void writeTo(JsonWriter out) throws IOException {
        out.value(name());
    }
}
