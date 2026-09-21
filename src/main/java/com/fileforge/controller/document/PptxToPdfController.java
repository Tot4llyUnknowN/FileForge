package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PptxToPdfController {

    @FXML private Button backButton;
    @FXML private ListView<String> fileListView;
    @FXML private Label statusLabel;
    @FXML private Button convertButton;

    private List<File> selectedPptxFiles = new ArrayList<>();

    @FXML
    private void handleSelectPptx() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PowerPoint Presentations (.pptx)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PowerPoint Presentations (*.pptx)", "*.pptx"));

        Window window = backButton.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);
        if (files == null || files.isEmpty()) return;

        selectedPptxFiles = files;
        fileListView.getItems().clear();
        for (File f : selectedPptxFiles) {
            fileListView.getItems().add(f.getName() + " (" + (f.length() / 1024) + " KB)");
        }

        statusLabel.setText(selectedPptxFiles.size() + " presentation(s) queued.");
        convertButton.setDisable(false);
    }

    @FXML
    private void handleConvert() {
        if (selectedPptxFiles == null || selectedPptxFiles.isEmpty()) return;

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Merged PDF As");
        saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        saveChooser.setInitialFileName("merged_powerpoint_presentation.pdf");

        Window window = backButton.getScene().getWindow();
        File destinationFile = saveChooser.showSaveDialog(window);
        if (destinationFile == null) return;

        statusLabel.setText("Executing high-fidelity visual slide conversion...");
        convertButton.setDisable(true);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1. Initialize a unified vector PDF document
                try (PDDocument finalPdf = new PDDocument()) {

                    // 2. Loop through every selected presentation deck sequentially
                    for (File pptxFile : selectedPptxFiles) {
                        try (FileInputStream fis = new FileInputStream(pptxFile);
                             XMLSlideShow ppt = new XMLSlideShow(fis)) {

                            Dimension pageSize = ppt.getPageSize();
                            float width = (float) pageSize.getWidth();
                            float height = (float) pageSize.getHeight();
                            PDRectangle pdfPageSize = new PDRectangle(width, height);

                            // 3. Render slide graphics onto high-DPI raster buffers natively
                            for (XSLFSlide slide : ppt.getSlides()) {
                                // 2x Scaling factor handles text clarity beautifully on 1080p/4K panels
                                int scale = 2;
                                BufferedImage img = new BufferedImage(
                                        pageSize.width * scale, pageSize.height * scale, BufferedImage.TYPE_INT_RGB);

                                Graphics2D graphics = img.createGraphics();
                                try {
                                    // Set rendering hints to optimize font antialiasing and shape smoothing definitions
                                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                                    graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                                    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                                    graphics.scale(scale, scale);
                                    graphics.setColor(Color.white);
                                    graphics.fillRect(0, 0, pageSize.width, pageSize.height);

                                    // Render the full slide shapes, tables, text layouts, and backgrounds onto our vector grid buffer
                                    slide.draw(graphics);
                                } finally {
                                    graphics.dispose();
                                }

                                // 4. Draw the generated high-fidelity slide buffer directly onto a new PDFBox page layer
                                PDPage pdfPage = new PDPage(pdfPageSize);
                                finalPdf.addPage(pdfPage);

                                try (PDPageContentStream contentStream = new PDPageContentStream(finalPdf, pdfPage)) {
                                    // Compress slide image buffer as high-quality JPEG elements to keep file sizes slim
                                    PDImageXObject xObject = JPEGFactory.createFromImage(finalPdf, img, 0.92f);
                                    contentStream.drawImage(xObject, 0, 0, width, height);
                                }
                            }
                        }
                    }
                    // Save the final unified multi-page compiled presentation file safely
                    try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
                        finalPdf.save(fos);
                    }
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            statusLabel.setText("Converted successfully → " + destinationFile.getName());
            convertButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Native graphics conversion failed: " + task.getException().getMessage());
            convertButton.setDisable(false);
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private String stripExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? filename : filename.substring(0, dotIndex);
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/DocumentView.fxml");
    }
}
