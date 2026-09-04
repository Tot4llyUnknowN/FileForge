package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        showHome();
    }

    @FXML
    private void showHome() {
        loadView("/view/HomeView.fxml");
    }

    @FXML
    private void showDocument() {
        loadView("/view/DocumentView.fxml");
    }

    @FXML
    private void showImage() {
        loadView("/view/ImageView.fxml");
    }

    @FXML
    private void showAudio() {
        loadView("/view/AudioView.fxml");
    }

    @FXML
    private void showVideo() {
        loadView("/view/VideoView.fxml");
    }

    @FXML
    private void showText() {
        loadView("/view/TextView.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}