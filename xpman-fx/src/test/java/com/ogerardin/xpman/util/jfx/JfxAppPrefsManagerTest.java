package com.ogerardin.xpman.util.jfx;

import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JfxAppPrefsManagerTest {

    private JfxAppPrefsManager<TestPrefs> prefsManager;

    @BeforeEach
    void setUp() {
        prefsManager = new JfxAppPrefsManager<>(TestPrefs.class);
    }

    @AfterEach
    void tearDown() {
        prefsManager.clean();
    }

    @Test
    public void testSaveAndLoad() {
        final TestPrefs prefs = new TestPrefs();
        prefs.setBla("hahaha");
        prefs.setBli(Arrays.asList("hihihihi", "huhuhuhu"));

        prefsManager.save(prefs);

        final TestPrefs prefs1 = prefsManager.load();
    }

    @Data
    public static class TestPrefs extends JfxAppPrefs {
        private String bla;
        private List<String> bli;
    }
}