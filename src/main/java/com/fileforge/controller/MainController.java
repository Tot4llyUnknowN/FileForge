package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox sidebar;

    @FXML
    public void initialize() {
        showHome();

        sidebar.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                sidebar.prefWidthProperty().bind(newScene.widthProperty().multiply(0.20));
                sidebar.setMinWidth(160);
                sidebar.setMaxWidth(260);
            }
        });
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
            contentArea.getChildren().setAll(root);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}