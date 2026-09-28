package com.ogerardin.xpman.scenery_organizer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gson adapter for a mixed list of {@link SceneryClass} instances.
 * Built-in classes serialize as their enum name (e.g., "AIRPORT");
 * regex classes serialize as objects with "name" and "regex" fields.
 */
public class SceneryClassesAdapter extends TypeAdapter<List<SceneryClass>> {

    @Override
    public void write(JsonWriter out, List<SceneryClass> value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.beginArray();
        for (SceneryClass c : value) {
            c.writeTo(out);
        }
        out.endArray();
    }

    @Override
    public List<SceneryClass> read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        List<SceneryClass> result = new ArrayList<>();
        in.beginArray();
        while (in.hasNext()) {
            if (in.peek() == JsonToken.STRING) {
                result.add(BuiltinSceneryClass.valueOf(in.nextString()));
            } else {
                result.add(readRegexClass(in));
            }
        }
        in.endArray();
        return result;
    }

    private RegexSceneryClass readRegexClass(JsonReader in) throws IOException {
        String name = null;
        String regex = null;
        in.beginObject();
        while (in.hasNext()) {
            String fieldName = in.nextName();
            switch (fieldName) {
                case "name" -> name = in.nextString();
                case "regex" -> regex = in.nextString();
                default -> in.skipValue();
            }
        }
        in.endObject();
        return new RegexSceneryClass(name, regex);
    }
}
