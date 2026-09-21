package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ImageComingSoonController {

    @FXML private Button backButton;

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/ImageView.fxml");
    }
}