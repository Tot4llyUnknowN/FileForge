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