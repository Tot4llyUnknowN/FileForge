package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import com.fileforge.util.ActivityLogger;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DocxToPdfController {

    @FXML private Button backButton;
    @FXML private ListView<String> fileListView;
    @FXML private Label statusLabel;
    @FXML private Button convertButton;

    private List<File> selectedWordFiles = new ArrayList<>();

    @FXML
    private void handleSelectDocx() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Word Documents (.docx)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Documents (*.docx)", "*.docx"));

        Window window = backButton.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(window);
        if (files == null || files.isEmpty()) return;

        selectedWordFiles = files;
        fileListView.getItems().clear();
        for (File f : selectedWordFiles) {
            fileListView.getItems().add(f.getName() + " (" + (f.length() / 1024) + " KB)");
        }

        statusLabel.setText(selectedWordFiles.size() + " document(s) queued.");
        convertButton.setDisable(false);
    }

    @FXML
    private void handleConvert() {
        if (selectedWordFiles == null || selectedWordFiles.isEmpty()) return;

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Merged PDF");
        saveChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        if (selectedWordFiles.size() == 1) {
            saveChooser.setInitialFileName(stripExtension(selectedWordFiles.get(0).getName()) + ".pdf");
        } else {
            saveChooser.setInitialFileName("merged_word_documents.pdf");
        }

        Window window = backButton.getScene().getWindow();
        File destinationFile = saveChooser.showSaveDialog(window);
        if (destinationFile == null) return;

        statusLabel.setText("Executing high-fidelity merging and rendering...");
        convertButton.setDisable(true);

        List<File> filesUsed = new ArrayList<>(selectedWordFiles);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                IConverter converter = LocalConverter.builder().build();
                List<File> tempPdfFiles = new ArrayList<>();

                try {
                    // 1. Convert each Word file into a quick temporary PDF snippet
                    for (File wordFile : filesUsed) {
                        File tempPdf = File.createTempFile("forge_merge_tmp_", ".pdf");
                        tempPdf.deleteOnExit();
                        tempPdfFiles.add(tempPdf);

                        convertDocx(wordFile, tempPdf, converter);
                    }

                    // 2. STITCH PIPELINE: Use PDFMergerUtility to join them into one output file
                    PDFMergerUtility merger = new PDFMergerUtility();
                    for (File tempPdf : tempPdfFiles) {
                        merger.addSource(tempPdf);
                    }
                    merger.setDestinationFileName(destinationFile.getAbsolutePath());
                    merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache());

                } finally {
                    converter.shutDown();
                    // 3. STORAGE SANITIZATION: Instantly scrub temporary pieces from disk
                    for (File tempPdf : tempPdfFiles) {
                        if (tempPdf.exists()) {
                            tempPdf.delete();
                        }
                    }
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            if (filesUsed.size() == 1) {
                statusLabel.setText("Converted successfully → " + destinationFile.getName());
            } else {
                statusLabel.setText("Successfully merged " + filesUsed.size() + " files into one PDF!");
            }
            ActivityLogger.log("DOCX to PDF", ActivityLogger.joinPaths(filesUsed), destinationFile.getAbsolutePath());
            convertButton.setDisable(false);
        });

        task.setOnFailed(e -> {
            statusLabel.setText("Conversion/Merge failure: " + task.getException().getMessage());
            convertButton.setDisable(false);
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    private void convertDocx(File src, File dest, IConverter converter) throws Exception {
        try (InputStream docxInputStream = new FileInputStream(src);
             OutputStream pdfOutputStream = new FileOutputStream(dest)) {

            converter.convert(docxInputStream).as(DocumentType.DOCX)
                    .to(pdfOutputStream).as(DocumentType.PDF)
                    .execute();
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
}
