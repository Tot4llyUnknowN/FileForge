package com.fileforge.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class NavigationHelper {

    private static String lastBackTarget = "/view/HomeView.fxml";

    /** Standard navigation — does not change the recorded back target. */
    public static void navigate(Node sourceNode, String fxmlPath) {
        loadView(sourceNode, fxmlPath);
    }

    /** Use this when navigating TO a shared tool, so its "Back" button returns to the right place. */
    public static void navigate(Node sourceNode, String fxmlPath, String backTarget) {
        lastBackTarget = backTarget;
        loadView(sourceNode, fxmlPath);
    }

    /** Shared tool controllers call this instead of hardcoding a path. */
    public static void goBack(Node sourceNode) {
        loadView(sourceNode, lastBackTarget);
    }

    private static void loadView(Node sourceNode, String fxmlPath) {
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