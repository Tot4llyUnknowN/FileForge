package com.fileforge;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;

import com.fileforge.database.SettingsEngine;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(Main.class.getResource("/css/app.css").toExternalForm());

        // Apply the dark-theme style class (if active) to the actual scene root,
        // now that the Scene exists.
        SettingsEngine.applyVisualStyles(root);

        primaryStage.setTitle("FileForge");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(550);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}