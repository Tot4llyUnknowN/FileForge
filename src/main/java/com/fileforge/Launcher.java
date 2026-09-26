package com.fileforge;

import com.fileforge.database.DatabaseManager;
import com.fileforge.database.SettingsEngine;

public class Launcher {
    public static void main(String[] args) {
        // 1. Initialize relational database constraints structures
        DatabaseManager.initializeDatabase();

        // 2. FIXED: Pull down, parse, and initialize your application configuration parameters from local settings.json
        SettingsEngine.loadSettings();

        // 3. Boot standard JavaFX layout canvas views loops
        Main.main(args);
    }
}
