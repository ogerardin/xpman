package com.ogerardin.xpman.config;

import com.ogerardin.xpman.util.jfx.JfxAppConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;

@EqualsAndHashCode(callSuper = true)
@Data
public class XPManConfig extends JfxAppConfig {

    String lastXPlanePath;

    StringSet recentPaths = new StringSet();

    public static class StringSet extends HashSet<String> {}
}
