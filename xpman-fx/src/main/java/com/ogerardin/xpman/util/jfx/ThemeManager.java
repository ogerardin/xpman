package com.ogerardin.xpman.util.jfx;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.ogerardin.xpman.config.XPManPrefs;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    /**
     * Observable state of the applied theme, for UI elements (menu item, sidebar footer)
     * to stay in sync regardless of which entry point toggled the theme.
     */
    private final BooleanProperty darkProperty = new SimpleBooleanProperty(this, "dark", true);

    public boolean isDark() {
        return !LIGHT.equals(prefs.getTheme());
    }

    /**
     * @return whether the currently applied user agent stylesheet is the dark theme
     */
    public static boolean isCurrentThemeDark() {
        return currentThemeDark;
    }

    public BooleanProperty darkProperty() {
        return darkProperty;
    }

    public void applySavedTheme() {
        boolean dark = isDark();
        currentThemeDark = dark;
        darkProperty.set(dark);
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
