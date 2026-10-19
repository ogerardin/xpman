package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.util.jfx.wizard.Wizard;

/**
 * Utility class for managing window position persistence.
 * Initialized once at application startup with the config and a save callback.
 */
public class WindowPositionHelper {

    private static XPManPrefs config;
    private static Runnable saveCallback;

    /**
     * Initializes the helper with the config and save callback.
     * Should be called once during application startup.
     */
    public static void initialize(XPManPrefs config, Runnable saveCallback) {
        WindowPositionHelper.config = config;
        WindowPositionHelper.saveCallback = saveCallback;
    }

    /**
     * Returns true if the helper has been initialized.
     */
    public static boolean isInitialized() {
        return config != null;
    }

    private static XPManPrefs config() {
        if (config == null) {
            throw new IllegalStateException("WindowPositionHelper not initialized; call initialize() at startup");
        }
        return config;
    }

    private static void save() {
        if (saveCallback != null) {
            saveCallback.run();
        }
    }

    /**
     * Sets up size persistence for the install wizard.
     */
    public static void setupInstallWizard(Wizard wizard) {
        wizard.setupSizePersistence(
                () -> isInitialized() ? config().getWizardSize() : null,
                size -> {
                    if (isInitialized()) {
                        config().setWizardSize(size);
                        save();
                    }
                }
        );
    }

    /**
     * Sets up size persistence for the Skunkcrafts update wizard.
     */
    public static void setupSkunkcraftsWizard(Wizard wizard) {
        wizard.setupSizePersistence(
                () -> isInitialized() ? config().getSkunkcraftsWizardSize() : null,
                size -> {
                    if (isInitialized()) {
                        config().setSkunkcraftsWizardSize(size);
                        save();
                    }
                }
        );
    }
}
