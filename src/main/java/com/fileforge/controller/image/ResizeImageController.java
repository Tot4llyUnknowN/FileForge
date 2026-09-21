package com.fileforge.controller.image;

import com.fileforge.controller.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ResizeImageController {

    @FXML private Button backButton;
    @FXML private Label sourceFileLabel;
    @FXML private ImageView previewImage;
    @FXML private Label dimensionsLabel;
    @FXML private RadioButton exactModeRadio;
    @FXML private RadioButton percentModeRadio;
    @FXML private HBox exactSizeBox;
    @FXML private HBox percentBox;
    @FXML private TextField widthField;
    @FXML private TextField heightField;
    @FXML private TextField scaleField;
    @FXML private Label statusLabel;

    private File sourceFile;
    private BufferedImage sourceImage;

    @FXML
    private void handleSelectImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.webp", "*.bmp"));

        Window window = backButton.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file == null) return;

        try {
            sourceImage = ImageIO.read(file);
            if (sourceImage == null) {
                statusLabel.setText("Unsupported or unreadable image file.");
                return;
            }

            sourceFile = file;
            sourceFileLabel.setText(file.getName());
            previewImage.setImage(new Image(file.toURI().toString()));
            dimensionsLabel.setText("Original size: " + sourceImage.getWidth() + " x " + sourceImage.getHeight() + " px");

            widthField.setText(String.valueOf(sourceImage.getWidth()));
            heightField.setText(String.valueOf(sourceImage.getHeight()));
            scaleField.setText("100");

            statusLabel.setText("");

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Failed to open image: " + e.getMessage());
        }
    }

    @FXML
    private void handleModeChanged() {
        boolean exact = exactModeRadio.isSelected();
        exactSizeBox.setVisible(exact);
        exactSizeBox.setManaged(exact);
        percentBox.setVisible(!exact);
        percentBox.setManaged(!exact);
    }

    @FXML
    private void handleResize() {
        if (sourceImage == null) {
            statusLabel.setText("Select an image first.");
            return;
        }

        int targetWidth;
        int targetHeight;

        if (exactModeRadio.isSelected()) {
            Integer w = parsePositiveInt(widthField.getText());
            Integer h = parsePositiveInt(heightField.getText());
            if (w == null || h == null) {
                statusLabel.setText("Enter valid positive width and height values.");
                return;
            }
            targetWidth = w;
            targetHeight = h;

        } else {
            Double scale = parsePositiveDouble(scaleField.getText());
            if (scale == null) {
                statusLabel.setText("Enter a valid positive percentage.");
                return;
            }
            targetWidth = Math.max(1, (int) Math.round(sourceImage.getWidth() * (scale / 100.0)));
            targetHeight = Math.max(1, (int) Math.round(sourceImage.getHeight() * (scale / 100.0)));
        }

        String extension = getExtension(sourceFile.getName());
        String formatName = extension.equalsIgnoreCase("jpg") ? "jpeg" : extension.toLowerCase();

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Resized Image As");
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(extension.toUpperCase() + " Image", "*." + extension));
        saveChooser.setInitialFileName(stripExtension(sourceFile.getName()) + "_resized." + extension);

        Window window = backButton.getScene().getWindow();
        File destination = saveChooser.showSaveDialog(window);
        if (destination == null) return;

        try {
            BufferedImage resized = resizeImage(sourceImage, targetWidth, targetHeight, formatName);
            ImageIO.write(resized, formatName, destination);

            statusLabel.setText("Resized to " + targetWidth + " x " + targetHeight + " px → " + destination.getName());

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Resize failed: " + e.getMessage());
        }
    }

    private BufferedImage resizeImage(BufferedImage source, int targetWidth, int targetHeight, String formatName) {
        int imageType = formatName.equalsIgnoreCase("jpeg")
                ? BufferedImage.TYPE_INT_RGB
                : BufferedImage.TYPE_INT_ARGB;

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D g2d = resized.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return resized;
    }

    private Integer parsePositiveInt(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Double parsePositiveDouble(String text) {
        try {
            double value = Double.parseDouble(text.trim());
            return value > 0 ? value : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? "png" : filename.substring(dotIndex + 1);
    }

    private String stripExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? filename : filename.substring(0, dotIndex);
    }

    @FXML
    private void goBack() {
        NavigationHelper.goBack(backButton);
    }
}