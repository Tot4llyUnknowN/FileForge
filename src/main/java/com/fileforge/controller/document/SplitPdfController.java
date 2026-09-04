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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

public class SplitPdfController {

    @FXML private Button backButton;
    @FXML private Label sourceFileLabel;
    @FXML private ListView<Integer> pageListView;
    @FXML private TextField rangesField;
    @FXML private Label outputFolderLabel;
    @FXML private Label statusLabel;

    private final ObservableList<Integer> pages = FXCollections.observableArrayList();
    private final TreeSet<Integer> selectedPages = new TreeSet<>();

    private List<Image> thumbnails;
    private File sourceFile;
    private File outputFolder;
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
            cell.setSelectedProvider(selectedPages::contains);
            cell.setOnMouseClicked(e -> {
                Integer pageNum = cell.getItem();
                if (pageNum == null) return;

                if (!selectedPages.remove(pageNum)) {
                    selectedPages.add(pageNum);
                }
                syncRangesField();
                pageListView.refresh();
            });
            return cell;
        });
    }

    @FXML
    private void handleSelectPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select PDF to split");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        Window window = backButton.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file == null) return;

        try (PDDocument doc = Loader.loadPDF(file)) {
            sourceFile = file;
            totalPages = doc.getNumberOfPages();
            thumbnails = PdfThumbnailUtil.renderAllThumbnails(doc);

            selectedPages.clear();
            rangesField.clear();

            pages.clear();
            for (int i = 1; i <= totalPages; i++) pages.add(i);

            sourceFileLabel.setText(file.getName() + "  (" + totalPages + " pages)");
            clearStatus();

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Failed to open PDF: " + e.getMessage(), true);
        }
    }

    private void syncRangesField() {
        StringBuilder sb = new StringBuilder();
        for (Integer page : selectedPages) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(page);
        }
        rangesField.setText(sb.toString());
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
    private void handleSplit() {
        if (sourceFile == null) {
            showStatus("Select a PDF file first.", true);
            return;
        }
        if (outputFolder == null) {
            showStatus("Choose an output folder first.", true);
            return;
        }

        String rangesText = rangesField.getText();
        if (rangesText == null || rangesText.isBlank()) {
            showStatus("Select pages or enter at least one page range.", true);
            return;
        }

        try (PDDocument sourceDoc = Loader.loadPDF(sourceFile)) {
            String[] rangeTokens = rangesText.split(",");
            String baseName = stripExtension(sourceFile.getName());
            int successCount = 0;

            for (String token : rangeTokens) {
                token = token.trim();
                if (token.isEmpty()) continue;

                int[] range = parseRange(token, totalPages);
                if (range == null) {
                    showStatus("Invalid range: \"" + token + "\" (valid pages: 1-" + totalPages + ")", true);
                    return;
                }

                int start = range[0];
                int end = range[1];

                try (PDDocument outputDoc = new PDDocument()) {
                    for (int i = start - 1; i < end; i++) {
                        outputDoc.addPage(sourceDoc.getPage(i));
                    }
                    File outFile = new File(outputFolder, baseName + "_" + start + "-" + end + ".pdf");
                    outputDoc.save(outFile);
                    successCount++;
                }
            }

            showStatus("Split into " + successCount + " file(s) → " + outputFolder.getAbsolutePath(), false);

        } catch (IOException e) {
            e.printStackTrace();
            showStatus("Split failed: " + e.getMessage(), true);
        }
    }

    private int[] parseRange(String token, int totalPages) {
        try {
            int start, end;
            if (token.contains("-")) {
                String[] parts = token.split("-");
                if (parts.length != 2) return null;
                start = Integer.parseInt(parts[0].trim());
                end = Integer.parseInt(parts[1].trim());
            } else {
                start = end = Integer.parseInt(token.trim());
            }
            if (start < 1 || end < 1 || start > end || end > totalPages) return null;
            return new int[]{start, end};
        } catch (NumberFormatException e) {
            return null;
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