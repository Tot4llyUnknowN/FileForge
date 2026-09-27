package com.fileforge;

import com.fileforge.database.DatabaseManager;
import com.fileforge.database.SettingsEngine;

public class Launcher {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        SettingsEngine.loadSettings();
        Main.main(args);
    }
}
