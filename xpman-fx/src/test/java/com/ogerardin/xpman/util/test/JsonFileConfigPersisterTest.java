package com.ogerardin.xpman.util.test;

import com.ogerardin.xpman.util.JsonFileConfigPersister;
import com.ogerardin.xpman.util.jfx.JfxAppPrefs;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class JsonFileConfigPersisterTest {

    private JsonFileConfigPersister<TestPrefs> prefsManager;

    @BeforeEach
    void setUp() {
        prefsManager = new JsonFileConfigPersister<>(TestPrefs.class);
    }

    @AfterEach
    void tearDown() {
        prefsManager.clean();
    }

    @Test
    void testSaveAndLoad() {
        final TestPrefs prefs = prefsManager.getConfig();
        prefs.setBla("hahaha");
        prefs.setBli(Arrays.asList("hihihihi", "huhuhuhu"));

        prefsManager.save();
    }

    @Test
    void themeDefaultsToDarkWhenMissingFromPrefs() throws Exception {
        Class<?> prefsClass = Class.forName("com.ogerardin.xpman.config.XPManPrefs");
        Path file = Files.createTempFile("XPManPrefs", ".json");
        try {
            Files.writeString(file, "{\"lastXPlanePath\":\"/X-Plane 12\"}");
            JsonFileConfigPersister<?> prefsManager = new JsonFileConfigPersister<>(prefsClass, file);
            assertThat(prefsManager.getConfig().toString(), containsString("theme=dark"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TestPrefs extends JfxAppPrefs {
        private String bla;
        private List<String> bli;
    }
}