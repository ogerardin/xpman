package com.ogerardin.xpman.config;

import com.google.gson.annotations.JsonAdapter;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryClassesAdapter;
import com.ogerardin.xpman.settings.SettingsCategory;
import com.ogerardin.xpman.util.jfx.JfxAppPrefs;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** User preferences for XPman, persisted as JSON to {@code ~/.xpman}. */
@EqualsAndHashCode(callSuper = true)
@Data
public class XPManPrefs extends JfxAppPrefs {

    String lastXPlanePath;
    StringSet recentPaths = new StringSet();

    String theme = "dark";

    WindowPosition settingsPosition;

    WindowSize wizardSize;

    WindowSize skunkcraftsWizardSize;

    Map<String, WindowPosition> propertiesPositions = new HashMap<>();

    SettingsCategory settingsCategory;

    @JsonAdapter(SceneryClassesAdapter.class)
    List<SceneryClass> sceneryClasses;

    /** Gson-friendly HashSet<String> for JSON deserialization of string sets. */
    public static class StringSet extends HashSet<String> {}
}
