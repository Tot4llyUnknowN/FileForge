package com.fileforge.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppSettings {

    private String activeTheme = "CLASSIC_LIGHT"; // CLASSIC_LIGHT or STUDIO_DARK
    private String activeFontFamily = "Segoe UI";  // Segoe UI, Consolas, or System Monospaced
    private String defaultExportPath = System.getProperty("user.home") + File.separator + "Downloads";
    private boolean autoOverwriteExisting = true;
    private int maxProcessingThreads = 4;

    public AppSettings() {}

    public String getActiveTheme() { return activeTheme; }
    public void setActiveTheme(String activeTheme) { this.activeTheme = activeTheme; }

    public String getActiveFontFamily() { return activeFontFamily; }
    public void setActiveFontFamily(String activeFontFamily) { this.activeFontFamily = activeFontFamily; }

    public String getDefaultExportPath() { return defaultExportPath; }
    public void setDefaultExportPath(String defaultExportPath) { this.defaultExportPath = defaultExportPath; }

    public boolean isAutoOverwriteExisting() { return autoOverwriteExisting; }
    public void setAutoOverwriteExisting(boolean autoOverwriteExisting) { this.autoOverwriteExisting = autoOverwriteExisting; }

    public int getMaxProcessingThreads() { return maxProcessingThreads; }
    public void setMaxProcessingThreads(int maxProcessingThreads) { this.maxProcessingThreads = maxProcessingThreads; }
}
