package com.ogerardin.xpman.util.jfx;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.ogerardin.xpman.config.XPManPrefs;
import javafx.application.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the application theme (dark or light): applies the AtlantaFX user agent stylesheet matching the theme
 * persisted in {@link XPManPrefs}, and switches between the two while persisting the new value.
 */
@Slf4j
@RequiredArgsConstructor
public final class ThemeManager {

    private static final String DARK = "dark";
    private static final String LIGHT = "light";

    /**
     * Tracks which theme is currently applied, so that non-JavaFX surfaces (e.g. WebView HTML)
     * can match the active appearance without an app reference.
     */
    private static volatile boolean currentThemeDark = true;

    private final XPManPrefs prefs;
    private final Runnable saver;

    public boolean isDark() {
        return !LIGHT.equals(prefs.getTheme());
    }

    /**
     * @return whether the currently applied user agent stylesheet is the dark theme
     */
    public static boolean isCurrentThemeDark() {
        return currentThemeDark;
    }

    public void applySavedTheme() {
        boolean dark = isDark();
        currentThemeDark = dark;
        Theme theme = dark ? new PrimerDark() : new PrimerLight();
        Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
        log.debug("Applied {} theme", dark ? DARK : LIGHT);
    }

    public void toggle() {
        prefs.setTheme(isDark() ? LIGHT : DARK);
        saver.run();
        applySavedTheme();
    }
}
