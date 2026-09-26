package com.fileforge.controller.image;

import com.fileforge.controller.NavigationHelper;
import com.fileforge.util.ActivityLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CropImageController {

    @FXML private Button backButton;
    @FXML private Label fileLabel;
    @FXML private Label statusLabel;
    @FXML private ImageView previewImage;
    @FXML private Pane imagePane;
    @FXML private Button cropButton;

    private File sourceFile;
    private BufferedImage sourceImage;
    private Rectangle cropBounds;

    private double startX;
    private double startY;

    @FXML
    public void initialize() {
        // Build the drawing overlay box container
        cropBounds = new Rectangle(0, 0, 0, 0);
        cropBounds.setStroke(Color.web("#6c4fd6")); // Theme purple accent
        cropBounds.setStrokeWidth(2);
        cropBounds.setFill(Color.rgb(108, 79, 214, 0.15)); // Semitransparent fill
        cropBounds.setVisible(false);

        imagePane.getChildren().add(cropBounds);
    }

    @FXML
    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image to Crop");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.webp", "*.bmp"));

        File file = chooser.showOpenDialog(backButton.getScene().getWindow());
        if (file == null) return;

        try {
            sourceImage = ImageIO.read(file);
            if (sourceImage == null) {
                statusLabel.setText("Unsupported image formatting profile.");
                return;
            }

            sourceFile = file;
            fileLabel.setText(file.getName() + " [" + sourceImage.getWidth() + "x" + sourceImage.getHeight() + " px]");

            Image fxImage = new Image(file.toURI().toString());
            previewImage.setImage(fxImage);

            // Constrain visual stack dimensions neatly inside viewport rules
            imagePane.setMaxWidth(previewImage.getFitWidth());
            imagePane.setMaxHeight(previewImage.getFitHeight());

            cropBounds.setVisible(false);
            statusLabel.setText("Click and drag over the image to draw your crop zone.");
            cropButton.setDisable(true);

        } catch (IOException e) {
            statusLabel.setText("Failed to read image structure: " + e.getMessage());
        }
    }

    @FXML
    private void handleMousePressed(MouseEvent e) {
        if (previewImage.getImage() == null) return;

        startX = e.getX();
        startY = e.getY();

        cropBounds.setX(startX);
        cropBounds.setY(startY);
        cropBounds.setWidth(0);
        cropBounds.setHeight(0);
        cropBounds.setVisible(true);
    }

    @FXML
    private void handleMouseDragged(MouseEvent e) {
        if (previewImage.getImage() == null) return;

        double currentX = e.getX();
        double currentY = e.getY();

        // Enforce boundary logic calculations relative to structural viewports
        double endX = Math.max(0, Math.min(currentX, previewImage.getBoundsInParent().getWidth()));
        double endY = Math.max(0, Math.min(currentY, previewImage.getBoundsInParent().getHeight()));

        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double w = Math.abs(startX - endX);
        double h = Math.abs(startY - endY);

        cropBounds.setX(x);
        cropBounds.setY(y);
        cropBounds.setWidth(w);
        cropBounds.setHeight(h);

        if (w > 5 && h > 5) {
            cropButton.setDisable(false);
        }
    }

    @FXML
    private void cropAndSave() {
        if (sourceImage == null || !cropBounds.isVisible()) return;

        // Map layout view geometry scaling properties directly to structural pixel layouts
        double boundsWidth = previewImage.getBoundsInParent().getWidth();
        double boundsHeight = previewImage.getBoundsInParent().getHeight();

        double scaleX = sourceImage.getWidth() / boundsWidth;
        double scaleY = sourceImage.getHeight() / boundsHeight;

        int pixelX = (int) (cropBounds.getX() * scaleX);
        int pixelY = (int) (cropBounds.getY() * scaleY);
        int pixelW = (int) (cropBounds.getWidth() * scaleX);
        int pixelH = (int) (cropBounds.getHeight() * scaleY);

        // Sanity check coordinates to avoid runtime array indexing exceptions
        pixelX = Math.max(0, Math.min(pixelX, sourceImage.getWidth() - 1));
        pixelY = Math.max(0, Math.min(pixelY, sourceImage.getHeight() - 1));
        pixelW = Math.max(1, Math.min(pixelW, sourceImage.getWidth() - pixelX));
        pixelH = Math.max(1, Math.min(pixelH, sourceImage.getHeight() - pixelY));

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Cropped Image As");
        String ext = getExtension(sourceFile.getName());
        saveChooser.setInitialFileName(stripExtension(sourceFile.getName()) + "_cropped." + ext);
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(ext.toUpperCase() + " Image", "*." + ext));

        File destination = saveChooser.showSaveDialog(backButton.getScene().getWindow());
        if (destination == null) return;

        // Capture the pre-crop source path now — sourceFile gets reassigned to
        // the destination below so the user can keep chaining crops on the result.
        File cropSourceFile = sourceFile;

        try {
            BufferedImage croppedSubImage = sourceImage.getSubimage(pixelX, pixelY, pixelW, pixelH);

            // Format fallback standardization framework rules
            String formatName = ext.equalsIgnoreCase("jpg") ? "jpeg" : ext.toLowerCase();
            ImageIO.write(croppedSubImage, formatName, destination);

            statusLabel.setText("Cropped layout successfully committed onto disk!");
            ActivityLogger.log("Crop Image", cropSourceFile.getAbsolutePath(), destination.getAbsolutePath());
            cropBounds.setVisible(false);
            cropButton.setDisable(true);

            // Re-bootstrap working memory layout architectures
            sourceImage = ImageIO.read(destination);
            sourceFile = destination;
            previewImage.setImage(new Image(destination.toURI().toString()));
            fileLabel.setText(destination.getName() + " [" + sourceImage.getWidth() + "x" + sourceImage.getHeight() + " px]");

        } catch (IOException e) {
            statusLabel.setText("Extraction processing failed: " + e.getMessage());
        }
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
