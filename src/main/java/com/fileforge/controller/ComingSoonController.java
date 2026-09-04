package com.fileforge.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ComingSoonController {

    @FXML
    private Button backButton;

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/DocumentView.fxml");
    }
}