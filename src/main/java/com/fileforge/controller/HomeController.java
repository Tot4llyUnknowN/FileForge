package com.fileforge.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML private HBox mainRootContainer;
    @FXML private VBox leftNavigationColumn;
    @FXML private VBox rightActivityColumn;
    @FXML private ListView<String> recentHistoryListView;

    @FXML
    public void initialize() {
        // Prevent layout initialization errors by deferring properties binding execution
        // until the primary stage window framework finishes rendering on screen canvas
        Platform.runLater(this::setupResponsiveLayoutBindings);
    }

    /**
     * Programmatic Properties Bindings Engine
     * Satisfies Layout Responsiveness requirements via real-time mathematical window constraints
     */
    private void setupResponsiveLayoutBindings() {
        if (mainRootContainer.getScene() == null) return;

        // 1. Force the Right Activity Column to always scale to precisely 35% of workspace width boundary lines
        rightActivityColumn.prefWidthProperty().bind(
                mainRootContainer.widthProperty().multiply(0.35)
        );

        // 2. Enforce hard bounding safety parameters to preserve layout stability on small screen resolutions
        rightActivityColumn.minWidthProperty().setValue(280);
        rightActivityColumn.maxWidthProperty().setValue(480);

        // 3. Force the Left Navigation Grid workspace to fluidly claim the remaining 65% space spectrum
        leftNavigationColumn.prefWidthProperty().bind(
                mainRootContainer.widthProperty().multiply(0.65)
        );

        System.out.println("[Responsiveness Engine] Asymmetrical window layout property bindings registered cleanly.");
    }

    @FXML
    private void goToDocument(MouseEvent event) {
        NavigationHelper.navigate(mainRootContainer, "/view/DocumentView.fxml");
    }

    @FXML
    private void goToImage(MouseEvent event) {
        NavigationHelper.navigate(mainRootContainer, "/view/ImageView.fxml");
    }

    @FXML
    private void goToText(MouseEvent event) {
        NavigationHelper.navigate(mainRootContainer, "/view/TextView.fxml");
    }
}
