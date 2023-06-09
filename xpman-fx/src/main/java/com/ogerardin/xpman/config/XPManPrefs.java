package com.ogerardin.xpman.config;

import com.ogerardin.xpman.scenery_organizer.RegexSceneryClass;
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

    List<RegexSceneryClass> sceneryClasses;

    public static class StringSet extends HashSet<String> {}
}
