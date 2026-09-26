package com.fileforge.controller.image;

import com.fileforge.controller.NavigationHelper;
import com.fileforge.util.ActivityLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RotateFlipController {

    @FXML private Button backButton;
    @FXML private Label fileLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView previewImage;
    @FXML private Button saveButton;

    private File sourceFile;
    private BufferedImage currentImage;

    @FXML
    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image to Rotate or Flip");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.webp", "*.bmp"));

        File file = chooser.showOpenDialog(backButton.getScene().getWindow());
        if (file == null) return;

        try {
            currentImage = ImageIO.read(file);
            if (currentImage == null) {
                statusLabel.setText("Unsupported or corrupted image file format.");
                return;
            }

            sourceFile = file;
            updatePreview(file);
            statusLabel.setText("Image loaded successfully.");
            saveButton.setDisable(false);

        } catch (IOException e) {
            statusLabel.setText("Failed to load image: " + e.getMessage());
        }
    }

    @FXML
    private void rotate90CW() {
        if (currentImage == null) return;

        // Setup dimension tracking matrix targets
        int w = currentImage.getWidth();
        int h = currentImage.getHeight();
        BufferedImage rotated = new BufferedImage(h, w, currentImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : currentImage.getType());

        AffineTransform at = new AffineTransform();
        at.translate(h, 0);
        at.rotate(Math.toRadians(90));

        applyTransform(rotated, at);
        statusLabel.setText("Rotated 90° Clockwise.");
    }

    @FXML
    private void rotate90CCW() {
        if (currentImage == null) return;

        int w = currentImage.getWidth();
        int h = currentImage.getHeight();
        BufferedImage rotated = new BufferedImage(h, w, currentImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : currentImage.getType());

        AffineTransform at = new AffineTransform();
        at.translate(0, w);
        at.rotate(Math.toRadians(-90));

        applyTransform(rotated, at);
        statusLabel.setText("Rotated 90° Counter-Clockwise.");
    }

    @FXML
    private void rotate180() {
        if (currentImage == null) return;

        int w = currentImage.getWidth();
        int h = currentImage.getHeight();
        BufferedImage rotated = new BufferedImage(w, h, currentImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : currentImage.getType());

        AffineTransform at = new AffineTransform();
        at.translate(w, h);
        at.rotate(Math.toRadians(180));

        applyTransform(rotated, at);
        statusLabel.setText("Rotated 180°.");
    }

    @FXML
    private void flipHorizontal() {
        if (currentImage == null) return;

        int w = currentImage.getWidth();
        int h = currentImage.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, currentImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : currentImage.getType());

        AffineTransform at = new AffineTransform();
        at.translate(w, 0);
        at.scale(-1.0, 1.0); // Invert structural horizontal coordinates mapping

        applyTransform(flipped, at);
        statusLabel.setText("Flipped horizontally.");
    }

    @FXML
    private void flipVertical() {
        if (currentImage == null) return;

        int w = currentImage.getWidth();
        int h = currentImage.getHeight();
        BufferedImage flipped = new BufferedImage(w, h, currentImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : currentImage.getType());

        AffineTransform at = new AffineTransform();
        at.translate(0, h);
        at.scale(1.0, -1.0); // Invert structural vertical coordinates mapping

        applyTransform(flipped, at);
        statusLabel.setText("Flipped vertically.");
    }

    private void applyTransform(BufferedImage targetImage, AffineTransform transform) {
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        currentImage = op.filter(currentImage, targetImage);

        // Flatten alpha channels if format drops support seamlessly
        try {
            File tempFile = File.createTempFile("forge_preview_", "." + getExtension(sourceFile.getName()));
            tempFile.deleteOnExit();
            String ext = getExtension(sourceFile.getName());
            String formatName = ext.equalsIgnoreCase("jpg") ? "jpeg" : ext.toLowerCase();
            ImageIO.write(currentImage, formatName, tempFile);
            updatePreview(tempFile);
        } catch (IOException e) {
            statusLabel.setText("Preview rendering pipeline encountered an error.");
        }
    }

    @FXML
    private void saveImage() {
        if (currentImage == null || sourceFile == null) return;

        // Capture the pre-save source path — sourceFile is reassigned to the
        // destination below once saving succeeds, for continuous editing.
        File preSaveSourceFile = sourceFile;

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Transformed Image As");
        String ext = getExtension(sourceFile.getName());
        saveChooser.setInitialFileName(stripExtension(sourceFile.getName()) + "_transformed." + ext);
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(ext.toUpperCase() + " Image", "*." + ext));

        File destination = saveChooser.showSaveDialog(backButton.getScene().getWindow());
        if (destination == null) return;

        try {
            String formatName = ext.equalsIgnoreCase("jpg") ? "jpeg" : ext.toLowerCase();
            ImageIO.write(currentImage, formatName, destination);
            statusLabel.setText("Transformed changes committed onto disk: " + destination.getName());
            ActivityLogger.log("Rotate/Flip Image", preSaveSourceFile.getAbsolutePath(), destination.getAbsolutePath());

            // Re-bootstrap workspace states cleanly
            sourceFile = destination;
            updatePreview(destination);

        } catch (IOException e) {
            statusLabel.setText("Failed to save changes: " + e.getMessage());
        }
    }

    private void updatePreview(File target) {
        fileLabel.setText(target.getName() + " [" + currentImage.getWidth() + "x" + currentImage.getHeight() + " px]");
        previewImage.setImage(new Image(target.toURI().toString()));
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? "png" : filename.substring(dot + 1);
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot == -1 ? filename : filename.substring(0, dot);
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/ImageView.fxml");
    }
}
