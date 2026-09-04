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
import java.util.TreeSet;

public class DeletePagesController {

    @FXML private Button backButton;
    @FXML private Label sourceFileLabel;
    @FXML private ListView<Integer> pageListView;
    @FXML private Label statusLabel;

    private final ObservableList<Integer> pages = FXCollections.observableArrayList();
    private final TreeSet<Integer> selectedForDeletion = new TreeSet<>();

    private List<Image> thumbnails;
    private File sourceFile;
    private int totalPages = 0;

    @FXML
    public void initialize() {
        pageListView.setItems(pages);
        pageListView.setOrientation(Orientation.HORIZONTAL);

        pageListView.setCellFactory(lv -> {
            ThumbnailCell<Integer> cell = new ThumbnailCell<>(
                    pageNum -> thumbnails.get(pageNum - 1),
                    pageNum -> "Page " + pageNum,
                    pages,
                    false
            );
            cell.setSelectedProvider(selectedForDeletion::contains);
            cell.setOnMouseClicked(e -> {
                Integer pageNum = cell.getItem();
                if (pageNum == null) return;

                if (!selectedForDeletion.remove(pageNum)) {
                    selectedForDeletion.add(pageNum);
                }
                pageListView.refresh();
                clearStatus();
            });
            return cell;
        });
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
            totalPages = doc.getNumberOfPages();
            thumbnails = PdfThumbnailUtil.renderAllThumbnails(doc);

            selectedForDeletion.clear();
            pages.clear();
            for (int i = 1; i <= totalPages; i++) pages.add(i);

            sourceFileLabel.setText(file.getName() + "  (" + totalPages + " pages)");
            clearStatus();

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Failed to open PDF: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleDelete() {
        if (sourceFile == null) {
            showStatus("Select a PDF file first.", true);
            return;
        }
        if (selectedForDeletion.isEmpty()) {
            showStatus("Click at least one page to mark it for deletion.", true);
            return;
        }
        if (selectedForDeletion.size() >= totalPages) {
            showStatus("Cannot delete all pages — the PDF would be empty.", true);
            return;
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save PDF As");
        saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        saveChooser.setInitialFileName(stripExtension(sourceFile.getName()) + "_edited.pdf");

        Window window = backButton.getScene().getWindow();
        File destination = saveChooser.showSaveDialog(window);
        if (destination == null) return;

        try (PDDocument doc = Loader.loadPDF(sourceFile)) {
            Integer[] descending = selectedForDeletion.toArray(new Integer[0]);
            for (int i = descending.length - 1; i >= 0; i--) {
                doc.removePage(descending[i] - 1);
            }
            doc.save(destination);
            showStatus("Deleted " + selectedForDeletion.size() + " page(s) → " + destination.getName(), false);

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Delete failed: " + e.getMessage(), true);
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