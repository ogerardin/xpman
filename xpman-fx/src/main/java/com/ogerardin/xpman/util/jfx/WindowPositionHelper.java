package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xpman.config.XPManPrefs;

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
     * Applies a saved window position to a stage.
     */
    public static void applyPosition(javafx.stage.Stage stage, XPManPrefs.WindowPosition position) {
        if (position != null && stage != null) {
            stage.setX(position.getX());
            stage.setY(position.getY());
            stage.setWidth(position.getWidth());
            stage.setHeight(position.getHeight());
        }
    }

    /**
     * Creates a WindowPosition from a stage's current bounds.
     */
    public static XPManPrefs.WindowPosition fromStage(javafx.stage.Stage stage) {
        if (stage != null) {
            return new XPManPrefs.WindowPosition(
                    stage.getX(), stage.getY(),
                    stage.getWidth(), stage.getHeight()
            );
        }
        return null;
    }

    /**
     * Sets up position persistence for the install wizard.
     */
    public static void setupInstallWizard(com.ogerardin.xpman.util.jfx.wizard.Wizard wizard) {
        wizard.setupPositionPersistence(
                () -> isInitialized() ? config().getWizardPosition() : null,
                pos -> {
                    if (isInitialized()) {
                        config().setWizardPosition(pos);
                        save();
                    }
                }
        );
    }

    /**
     * Sets up position persistence for the Skunkcrafts update wizard.
     */
    public static void setupSkunkcraftsWizard(com.ogerardin.xpman.util.jfx.wizard.Wizard wizard) {
        wizard.setupPositionPersistence(
                () -> isInitialized() ? config().getSkunkcraftsWizardPosition() : null,
                pos -> {
                    if (isInitialized()) {
                        config().setSkunkcraftsWizardPosition(pos);
                        save();
                    }
                }
        );
    }
}
