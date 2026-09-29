package com.ogerardin.xpman.config;

import com.google.gson.annotations.JsonAdapter;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryClassesAdapter;
import com.ogerardin.xpman.settings.SettingsCategory;
import com.ogerardin.xpman.util.jfx.JfxAppPrefs;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class XPManPrefs extends JfxAppPrefs {

    String lastXPlanePath;
    StringSet recentPaths = new StringSet();

    String theme = "dark";

    WindowPosition settingsPosition;

    SettingsCategory settingsCategory;

    @JsonAdapter(SceneryClassesAdapter.class)
    List<SceneryClass> sceneryClasses;

    public static class StringSet extends HashSet<String> {}
}
