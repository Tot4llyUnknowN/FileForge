package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import com.fileforge.util.PdfThumbnailUtil;
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
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfMergerController {

    @FXML private Button backButton;
    @FXML private ListView<File> fileListView;
    @FXML private Label statusLabel;

    private final ObservableList<File> selectedFiles = FXCollections.observableArrayList();
    private final Map<File, Image> thumbnailCache = new HashMap<>();

    @FXML
    public void initialize() {
        fileListView.setItems(selectedFiles);
        fileListView.setOrientation(Orientation.HORIZONTAL);
        fileListView.setCellFactory(lv -> new ThumbnailCell<>(
                this::getThumbnail,
                File::getName,
                selectedFiles,
                true
        ));
    }

    private Image getThumbnail(File file) {
        return thumbnailCache.computeIfAbsent(file, f -> {
            try (PDDocument doc = Loader.loadPDF(f)) {
                return PdfThumbnailUtil.renderFirstPageThumbnail(doc);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @FXML
    private void handleAddFiles() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF files to merge");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Window window = backButton.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);

        if (files != null && !files.isEmpty()) {
            selectedFiles.addAll(files);
            clearStatus();
        }
    }

    @FXML
    private void handleRemoveSelected() {
        File selected = fileListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedFiles.remove(selected);
            thumbnailCache.remove(selected);
        }
    }

    @FXML
    private void handleClear() {
        selectedFiles.clear();
        thumbnailCache.clear();
        clearStatus();
    }

    @FXML
    private void handleMerge() {
        if (selectedFiles.size() < 2) {
            showStatus("Add at least 2 PDF files to merge.", true);
            return;
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Merged PDF As");
        saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        saveChooser.setInitialFileName("merged.pdf");

        Window window = backButton.getScene().getWindow();
        File destination = saveChooser.showSaveDialog(window);
        if (destination == null) return;

        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            for (File file : selectedFiles) {
                merger.addSource(file);
            }
            merger.setDestinationFileName(destination.getAbsolutePath());
            merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());

            showStatus("Merged successfully → " + destination.getName(), false);
        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Merge failed: " + e.getMessage(), true);
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