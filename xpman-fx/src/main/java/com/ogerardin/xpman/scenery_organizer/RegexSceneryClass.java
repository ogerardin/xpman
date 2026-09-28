package com.ogerardin.xpman.scenery_organizer;

import com.google.gson.stream.JsonWriter;
import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;

@Data
@AllArgsConstructor
public class RegexSceneryClass implements SceneryClass {

    private String name;

    private String regex;

    public RegexSceneryClass(String name) {
        this(name, null);
    }

    @Override
    public boolean matches(SceneryPackage sceneryPackage) {
        return regex != null && sceneryPackage.getName().matches(regex);
    }

    @Override
    public void writeTo(JsonWriter out) throws IOException {
        out.beginObject();
        out.name("name").value(name);
        out.name("regex").value(regex);
        out.endObject();
    }
}
