package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import com.fileforge.util.ActivityLogger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DocViewerController {

    @FXML private Label fileLabel;
    @FXML private Label pageLabel;
    @FXML private ImageView pageImageView;
    @FXML private Button backButton;

    private File selectedFile;
    private int currentPage = 0;
    private int totalPages = 0;

    @FXML
    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = chooser.showOpenDialog(backButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        selectedFile = file;
        fileLabel.setText(file.getName());
        currentPage = 0;

        try (PDDocument document = Loader.loadPDF(selectedFile)) {
            totalPages = document.getNumberOfPages();
        } catch (IOException e) {
            pageLabel.setText("Error loading document: " + e.getMessage());
            return;
        }

        // View-only operation: no output file is produced/saved, so log source only.
        ActivityLogger.log("View PDF Document", selectedFile.getAbsolutePath());

        renderCurrentPage();
    }

    private void renderCurrentPage() {
        if (selectedFile == null || totalPages == 0) {
            return;
        }

        try (PDDocument document = Loader.loadPDF(selectedFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(currentPage, 100);
            Image fxImage = com.fileforge.util.PdfThumbnailUtil.toFxImage(bufferedImage);

            pageImageView.setImage(fxImage);
            pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);

        } catch (IOException e) {
            pageLabel.setText("Error rendering page: " + e.getMessage());
        }
    }

    @FXML
    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            renderCurrentPage();
        }
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/DocumentView.fxml");
    }
}
