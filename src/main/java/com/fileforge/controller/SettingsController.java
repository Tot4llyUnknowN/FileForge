package com.fileforge.controller;

import com.fileforge.database.SettingsEngine;
import com.fileforge.model.AppSettings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SettingsController {

    @FXML private ComboBox<String> themeComboBox;
    @FXML private ComboBox<String> fontComboBox;
    @FXML private TextField exportDirectoryField;
    @FXML private ComboBox<Integer> threadsComboBox;
    @FXML private CheckBox overwriteCheckbox;
    @FXML private Label statusMessageLabel;

    @FXML
    public void initialize() {
        // Populate selection collections inside structural dropdown component nodes
        themeComboBox.setItems(FXCollections.observableArrayList("CLASSIC_LIGHT", "STUDIO_DARK"));
        fontComboBox.setItems(FXCollections.observableArrayList("Segoe UI", "Consolas", "Courier New", "Arial"));
        threadsComboBox.setItems(FXCollections.observableArrayList(2, 4, 8, 12));

        // Pull active saved states straight out of the JSON properties cache
        AppSettings settings = SettingsEngine.getActiveSettings();

        // Map values accurately into current UI fields views
        themeComboBox.setValue(settings.getActiveTheme());
        fontComboBox.setValue(settings.getActiveFontFamily());
        exportDirectoryField.setText(settings.getDefaultExportPath());
        threadsComboBox.setValue(settings.getMaxProcessingThreads());
        overwriteCheckbox.setSelected(settings.isAutoOverwriteExisting());
    }

    @FXML
    private void handleSaveConfiguration() {
        String path = exportDirectoryField.getText().trim();
        if (path.isEmpty()) {
            statusMessageLabel.setText("System configuration path criteria cannot stand blank!");
            statusMessageLabel.setStyle("-fx-text-fill: #d9534f;");
            return;
        }

        AppSettings updated = new AppSettings();
        updated.setActiveTheme(themeComboBox.getValue());
        updated.setActiveFontFamily(fontComboBox.getValue());
        updated.setDefaultExportPath(path);
        updated.setMaxProcessingThreads(threadsComboBox.getValue());
        updated.setAutoOverwriteExisting(overwriteCheckbox.isSelected());

        // Commit preferences data object variables to local settings.json file
        SettingsEngine.saveSettings(updated);

        // FIXED: Fetch the absolute master Scene root tree container window node
        // to force your sidebar menu text and dashboard boundaries to refresh colors instantly!
        if (exportDirectoryField.getScene() != null) {
            SettingsEngine.applyVisualStyles(exportDirectoryField.getScene().getRoot());
        }

        statusMessageLabel.setText("Preferences compiled and saved successfully to settings.json!");
        statusMessageLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

}
