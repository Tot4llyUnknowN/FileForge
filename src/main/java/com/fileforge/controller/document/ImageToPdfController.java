package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import com.fileforge.util.ThumbnailCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageToPdfController {

    private static final float MARGIN = 20f;

    @FXML private Button backButton;
    @FXML private ListView<File> imageListView;
    @FXML private Label statusLabel;

    private final ObservableList<File> selectedImages = FXCollections.observableArrayList();
    private final Map<File, Image> thumbnailCache = new HashMap<>();

    @FXML
    public void initialize() {
        imageListView.setItems(selectedImages);
        imageListView.setOrientation(Orientation.HORIZONTAL);
        imageListView.setCellFactory(lv -> new ThumbnailCell<>(
                this::getThumbnail,
                File::getName,
                selectedImages,
                true
        ));
    }

    private Image getThumbnail(File file) {
        return thumbnailCache.computeIfAbsent(file,
                f -> new Image(f.toURI().toString(), 90, 120, true, true));
    }

    @FXML
    private void handleAddImages() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select images");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif"));

        Window window = backButton.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);

        if (files != null && !files.isEmpty()) {
            selectedImages.addAll(files);
            clearStatus();
        }
    }

    @FXML
    private void handleRemoveSelected() {
        File selected = imageListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedImages.remove(selected);
            thumbnailCache.remove(selected);
        }
    }

    @FXML
    private void handleClear() {
        selectedImages.clear();
        thumbnailCache.clear();
        clearStatus();
    }

    @FXML
    private void handleConvert() {
        if (selectedImages.isEmpty()) {
            showStatus("Add at least 1 image.", true);
            return;
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save PDF As");
        saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        saveChooser.setInitialFileName("images.pdf");

        Window window = backButton.getScene().getWindow();
        File destination = saveChooser.showSaveDialog(window);
        if (destination == null) return;

        try (PDDocument document = new PDDocument()) {
            for (File imageFile : selectedImages) {
                addImagePage(document, imageFile);
            }
            document.save(destination);
            showStatus("Created PDF with " + selectedImages.size() + " page(s) → " + destination.getName(), false);

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Conversion failed: " + e.getMessage(), true);
        }
    }

    private void addImagePage(PDDocument document, File imageFile) throws IOException {
        PDImageXObject image = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), document);

        float imgWidth = image.getWidth();
        float imgHeight = image.getHeight();

        PDRectangle pageSize = imgWidth > imgHeight
                ? new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth())
                : PDRectangle.A4;

        PDPage page = new PDPage(pageSize);
        document.addPage(page);

        float pageWidth = pageSize.getWidth() - (2 * MARGIN);
        float pageHeight = pageSize.getHeight() - (2 * MARGIN);

        float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);
        float drawWidth = imgWidth * scale;
        float drawHeight = imgHeight * scale;

        float x = (pageSize.getWidth() - drawWidth) / 2f;
        float y = (pageSize.getHeight() - drawHeight) / 2f;

        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            content.drawImage(image, x, y, drawWidth, drawHeight);
        }
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/DocumentView.fxml");
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #d9534f;" : "-fx-text-fill: #2e7d32;");
    }

    private void clearStatus() {
        statusLabel.setText("");
    }
}