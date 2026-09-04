package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class HomeController {

    @FXML
    private void goToDocument(MouseEvent event) {
        navigate(event, "/view/DocumentView.fxml");
    }

    @FXML
    private void goToImage(MouseEvent event) {
        navigate(event, "/view/ImageView.fxml");
    }

    @FXML
    private void goToAudio(MouseEvent event) {
        navigate(event, "/view/AudioView.fxml");
    }

    @FXML
    private void goToVideo(MouseEvent event) {
        navigate(event, "/view/VideoView.fxml");
    }

    @FXML
    private void goToText(MouseEvent event) {
        navigate(event, "/view/TextView.fxml");
    }

    private void navigate(MouseEvent event, String fxmlPath) {
        Node source = (Node) event.getSource();
        NavigationHelper.navigate(source, fxmlPath);
    }
}