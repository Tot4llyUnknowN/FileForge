package com.fileforge.controller.image;

import com.fileforge.controller.NavigationHelper;
import com.fileforge.util.ImageConversionUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class WebpToJpgController {

    @FXML private Label fileLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView previewImage;
    @FXML private Button backButton;

    private File selectedFile;

    @FXML
    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select WEBP Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("WEBP Images", "*.webp"));

        File file = chooser.showOpenDialog(backButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        selectedFile = file;
        fileLabel.setText(file.getName());
        statusLabel.setText("");

        try {
            previewImage.setImage(new Image(file.toURI().toString()));
        } catch (Exception e) {
            // JavaFX's built-in Image loader doesn't understand WEBP either,
            // so preview may fail even though conversion (via ImageIO + TwelveMonkeys) will still work.
            statusLabel.setText("Preview unavailable for this format, but conversion will still work.");
        }
    }

    @FXML
    private void convert() {
        if (selectedFile == null) {
            statusLabel.setText("Please select a WEBP file first.");
            return;
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save JPG");
        String baseName = selectedFile.getName().replaceFirst("\\.webp$", "");
        saveChooser.setInitialFileName(baseName + ".jpg");
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPG Images", "*.jpg", "*.jpeg"));

        File outputFile = saveChooser.showSaveDialog(backButton.getScene().getWindow());
        if (outputFile == null) {
            return;
        }

        try {
            ImageConversionUtil.convert(selectedFile, outputFile, "jpg");
            statusLabel.setText("Converted successfully: " + outputFile.getName());
        } catch (IOException e) {
            statusLabel.setText("Error converting image: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/ImageView.fxml");
    }
}