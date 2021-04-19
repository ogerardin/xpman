package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.util.JsonFileConfigPersister;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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
    public void testSaveAndLoad() {
        final TestPrefs prefs = prefsManager.getConfig();
        prefs.setBla("hahaha");
        prefs.setBli(Arrays.asList("hihihihi", "huhuhuhu"));

        prefsManager.save();
    }

    @Data
    public static class TestPrefs extends JfxAppPrefs {
        private String bla;
        private List<String> bli;
    }
}