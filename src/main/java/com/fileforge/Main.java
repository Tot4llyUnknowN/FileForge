package com.fileforge;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;

import com.fileforge.database.SettingsEngine;
import javafx.scene.image.Image;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/view/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(Main.class.getResource("/css/app.css").toExternalForm());

        SettingsEngine.applyVisualStyles(root);

        primaryStage.setTitle("FileForge");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(550);
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/app-icon.png")));
        primaryStage.show();
    }

    @Override
    public void stop() {
        com.fileforge.util.AppExecutor.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}