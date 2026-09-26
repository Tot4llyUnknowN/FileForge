package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        showHome();
    }

    @FXML private void showHome() { loadView("/view/HomeView.fxml"); }
    @FXML private void showDocument() { loadView("/view/DocumentView.fxml"); }
    @FXML private void showImage() { loadView("/view/ImageView.fxml"); }
    @FXML private void showText() { loadView("/view/TextView.fxml"); }
    @FXML private void showSettings() { loadView("/view/SettingsView.fxml"); }

    private void loadView(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();

            // Theming is handled once, on the scene root, via
            // SettingsEngine.applyVisualStyles() in Main.java (at launch) and
            // SettingsController (on save). app.css's ".dark-theme" descendant
            // selectors cascade down to whatever gets swapped into contentArea,
            // so no per-view style pass is needed here anymore.

            contentArea.getChildren().setAll(root);

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}