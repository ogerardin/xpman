package com.ogerardin.xplane.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class ToolIconTest {

    private static Gson gson;

    @BeforeAll
    static void setUp() throws MalformedURLException {
        gson = new GsonBuilder()
                .registerTypeAdapter(ToolIcon.class, (JsonDeserializer<ToolIcon>) (json, __, ___) -> {
                    String value = json.getAsString();
                    if (value.startsWith("http://") || value.startsWith("https://")) {
                        return new ToolIcon.Url(new URL(value));
                    } else if (value.startsWith("/")) {
                        return new ToolIcon.Resource(value);
                    } else {
                        return new ToolIcon.IconFont(value);
                    }
                })
                .create();
    }

    @Test
    void deserializeUrlIcon() {
        String json = "\"https://example.com/icon.png\"";
        ToolIcon icon = gson.fromJson(json, ToolIcon.class);
        assertInstanceOf(ToolIcon.Url.class, icon);
        assertEquals("https://example.com/icon.png", ((ToolIcon.Url) icon).url().toString());
    }

    @Test
    void deserializeResourceIcon() {
        String json = "\"/img/tools/icon.png\"";
        ToolIcon icon = gson.fromJson(json, ToolIcon.class);
        assertInstanceOf(ToolIcon.Resource.class, icon);
        assertEquals("/img/tools/icon.png", ((ToolIcon.Resource) icon).path());
    }

    @Test
    void deserializeIconFont() {
        String json = "\"fth-tool\"";
        ToolIcon icon = gson.fromJson(json, ToolIcon.class);
        assertInstanceOf(ToolIcon.IconFont.class, icon);
        assertEquals("fth-tool", ((ToolIcon.IconFont) icon).literal());
    }
}
