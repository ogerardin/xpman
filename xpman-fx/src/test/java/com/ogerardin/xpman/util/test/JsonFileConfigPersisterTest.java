package com.ogerardin.xpman.util.test;

import com.ogerardin.xpman.util.JsonFileConfigPersister;
import com.ogerardin.xpman.util.jfx.JfxAppPrefs;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
    void testSaveAndLoad() {
        final TestPrefs prefs = prefsManager.getConfig();
        prefs.setBla("hahaha");
        prefs.setBli(Arrays.asList("hihihihi", "huhuhuhu"));

        prefsManager.save();
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class TestPrefs extends JfxAppPrefs {
        private String bla;
        private List<String> bli;
    }
}