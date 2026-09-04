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
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReorderPagesController {

    @FXML private Button backButton;
    @FXML private Label sourceFileLabel;
    @FXML private ListView<Integer> pageListView;
    @FXML private Label statusLabel;

    private final ObservableList<Integer> pageOrder = FXCollections.observableArrayList();
    private List<Image> thumbnails;
    private File sourceFile;

    @FXML
    public void initialize() {
        pageListView.setItems(pageOrder);
        pageListView.setOrientation(Orientation.HORIZONTAL);
        pageListView.setCellFactory(lv -> new ThumbnailCell<>(
                pageNum -> thumbnails.get(pageNum - 1),
                pageNum -> "Page " + pageNum,
                pageOrder,
                true
        ));
    }

    @FXML
    private void handleSelectPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Window window = backButton.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file == null) return;

        try (PDDocument doc = Loader.loadPDF(file)) {
            sourceFile = file;
            int totalPages = doc.getNumberOfPages();
            thumbnails = PdfThumbnailUtil.renderAllThumbnails(doc);

            pageOrder.clear();
            for (int i = 1; i <= totalPages; i++) pageOrder.add(i);

            sourceFileLabel.setText(file.getName() + "  (" + totalPages + " pages)");
            clearStatus();

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Failed to open PDF: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleReset() {
        if (sourceFile == null) return;

        try (PDDocument doc = Loader.loadPDF(sourceFile)) {
            int totalPages = doc.getNumberOfPages();
            pageOrder.clear();
            for (int i = 1; i <= totalPages; i++) pageOrder.add(i);
            clearStatus();
        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Failed to reset: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSave() {
        if (sourceFile == null || pageOrder.isEmpty()) {
            showStatus("Select a PDF first.", true);
            return;
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Reordered PDF As");
        saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        saveChooser.setInitialFileName(stripExtension(sourceFile.getName()) + "_reordered.pdf");

        Window window = backButton.getScene().getWindow();
        File destination = saveChooser.showSaveDialog(window);
        if (destination == null) return;

        try (PDDocument sourceDoc = Loader.loadPDF(sourceFile);
             PDDocument outputDoc = new PDDocument()) {

            for (int originalPageNumber : pageOrder) {
                outputDoc.addPage(sourceDoc.getPage(originalPageNumber - 1));
            }
            outputDoc.save(destination);
            showStatus("Saved reordered PDF → " + destination.getName(), false);

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Save failed: " + e.getMessage(), true);
        }
    }

    private String stripExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? filename : filename.substring(0, dotIndex);
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