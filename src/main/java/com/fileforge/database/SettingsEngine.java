package com.fileforge.database;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fileforge.model.AppSettings;
import javafx.scene.Parent;

import java.io.File;
import java.io.IOException;

public class SettingsEngine {

    private static final String SETTINGS_FILE_PATH = "settings.json";
    private static final String DARK_STYLE_CLASS = "dark-theme";
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private static AppSettings activeSettings = new AppSettings();

    public static void loadSettings() {
        File configFile = new File(SETTINGS_FILE_PATH);
        if (!configFile.exists()) {
            saveSettings(activeSettings);
            return;
        }
        try {
            activeSettings = mapper.readValue(configFile, AppSettings.class);
            System.out.println("[Settings Engine] Configuration parsed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            activeSettings = new AppSettings();
        }
    }

    public static void saveSettings(AppSettings settingsToCommit) {
        activeSettings = settingsToCommit;
        try {
            mapper.writeValue(new File(SETTINGS_FILE_PATH), activeSettings);
            System.out.println("[Settings Engine] Preferences committed directly to hard drive.");
        } catch (IOException e) {
            System.err.println("[Settings Error] Serialization failure: " + e.getMessage());
        }
    }

    public static AppSettings getActiveSettings() {
        return activeSettings;
    }

    /**
     * Applies the active theme by toggling the "dark-theme" style class on the
     * given root node. app.css already defines a full ".dark-theme ..." rule set
     * mirroring every light-theme selector, so no inline styles are needed here
     * — the stylesheet does the work once the class is present.
     *
     * FIX: this class was previously never added anywhere in the codebase,
     * so app.css's entire dark-theme block was dead code and the app always
     * rendered in the light palette regardless of settings.json.
     *
     * Font family is still applied inline per-root since app.css hardcodes
     * a fixed font stack and can't read the user's chosen font dynamically.
     */
    public static void applyVisualStyles(Parent rootNode) {
        if (rootNode == null) return;

        boolean isDark = activeSettings.getActiveTheme().equalsIgnoreCase("STUDIO_DARK");

        if (isDark) {
            if (!rootNode.getStyleClass().contains(DARK_STYLE_CLASS)) {
                rootNode.getStyleClass().add(DARK_STYLE_CLASS);
            }
        } else {
            rootNode.getStyleClass().remove(DARK_STYLE_CLASS);
        }

        String fontName = activeSettings.getActiveFontFamily();
        rootNode.setStyle("-fx-font-family: '" + fontName + "';");
    }
}