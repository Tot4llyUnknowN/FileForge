package com.fileforge.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class NavigationHelper {

    private static String lastBackTarget = "/view/HomeView.fxml";

    public static void navigate(Node sourceNode, String fxmlPath) {
        loadView(sourceNode, fxmlPath);
    }

    public static void navigate(Node sourceNode, String fxmlPath, String backTarget) {
        lastBackTarget = backTarget;
        loadView(sourceNode, fxmlPath);
    }

    public static void goBack(Node sourceNode) {
        loadView(sourceNode, lastBackTarget);
    }

    private static void loadView(Node sourceNode, String fxmlPath) {
        try {
            StackPane contentArea = (StackPane) sourceNode.getScene().lookup("#contentArea");
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource(fxmlPath));
            Parent view = loader.load();

            // Theming no longer needs to be reapplied here: SettingsEngine now
            // toggles the "dark-theme" style class once on the scene root
            // (see Main.java / SettingsController), and app.css's descendant
            // selectors (".dark-theme .card", etc.) cascade automatically to
            // any view swapped into contentArea, since it's a child of that
            // same themed root. No per-view style pass needed.

            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}