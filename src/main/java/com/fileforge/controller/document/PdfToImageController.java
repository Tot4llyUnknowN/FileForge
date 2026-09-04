package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfToImageController {

    private static final float DPI = 150f;

    @FXML private Button backButton;
    @FXML private Label sourceFileLabel;
    @FXML private Label outputFolderLabel;
    @FXML private Label statusLabel;

    private File sourceFile;
    private File outputFolder;

    @FXML
    private void handleSelectPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF to convert");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Window window = backButton.getScene().getWindow();
        File file = chooser.showOpenDialog(window);

        if (file != null) {
            sourceFile = file;
            sourceFileLabel.setText(file.getName());
            clearStatus();
        }
    }

    @FXML
    private void handleChooseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select output folder");

        Window window = backButton.getScene().getWindow();
        File folder = chooser.showDialog(window);

        if (folder != null) {
            outputFolder = folder;
            outputFolderLabel.setText(folder.getAbsolutePath());
            clearStatus();
        }
    }

    @FXML
    private void handleConvert() {
        if (sourceFile == null) {
            showStatus("Select a PDF file first.", true);
            return;
        }
        if (outputFolder == null) {
            showStatus("Choose an output folder first.", true);
            return;
        }

        try (PDDocument document = Loader.loadPDF(sourceFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            int pageCount = document.getNumberOfPages();
            String baseName = stripExtension(sourceFile.getName());

            for (int i = 0; i < pageCount; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, DPI, ImageType.RGB);
                File outFile = new File(outputFolder, baseName + "_page" + (i + 1) + ".png");
                ImageIO.write(image, "png", outFile);
            }

            showStatus("Converted " + pageCount + " page(s) → " + outputFolder.getAbsolutePath(), false);

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Conversion failed: " + e.getMessage(), true);
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