package com.ogerardin.xpman.config;

import com.google.gson.Gson;
import com.ogerardin.xpman.XPmanFX;
import javafx.geometry.Rectangle2D;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.util.prefs.Preferences;

/**
 * Utility class to load and save preferences and config using the Java Preferences mechanism
 */
@UtilityClass
public class PrefsConfigManager {
    //TODO use a single key and store all as JSON

    private final Gson GSON = new Gson();

    private final String KEY_LAST_XPLANE_PATH = "lastXplanePath";
    private final String KEY_RECENT_PATHS = "recentPaths";
    private final String KEY_LAST_POSITION = "lastPosition";

    @SneakyThrows
    public XPManConfig load() {
        Preferences prefsRoot = getPrefsRoot();
        XPManConfig config = new XPManConfig();
        config.setLastXPlanePath(prefsRoot.get(KEY_LAST_XPLANE_PATH, null));
        String recentPaths = prefsRoot.get(KEY_RECENT_PATHS, null);
        if (recentPaths != null) {
            config.setRecentPaths(GSON.fromJson(recentPaths, XPManConfig.StringSet.class));
        }
        String lastPosition = prefsRoot.get(KEY_LAST_POSITION, null);
        if (lastPosition != null) {
            config.setLastPosition(GSON.fromJson(lastPosition, Rectangle2D.class));
        }
        return config;
    }

    @SneakyThrows
    public void save(XPManConfig config) {
        Preferences prefsRoot = getPrefsRoot();
        if (config.getLastXPlanePath() != null) {
            prefsRoot.put(KEY_LAST_XPLANE_PATH, config.getLastXPlanePath());
        }
        prefsRoot.put(KEY_RECENT_PATHS, GSON.toJson(config.getRecentPaths()));
        prefsRoot.put(KEY_LAST_POSITION, GSON.toJson(config.getLastPosition()));
    }

    public Preferences getPrefsRoot() {
        return Preferences.userNodeForPackage(XPmanFX.class);
    }
}
