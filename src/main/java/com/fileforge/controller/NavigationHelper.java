package com.fileforge.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class NavigationHelper {

    public static void navigate(Node sourceNode, String fxmlPath) {
        try {
            StackPane contentArea = (StackPane) sourceNode.getScene().lookup("#contentArea");
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}