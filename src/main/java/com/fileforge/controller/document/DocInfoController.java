package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DocInfoController {

    @FXML private TextArea infoArea;
    @FXML private Button backButton;

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

        try (PDDocument document = Loader.loadPDF(file)) {
            PDDocumentInformation info = document.getDocumentInformation();

            StringBuilder sb = new StringBuilder();
            sb.append("File name: ").append(file.getName()).append("\n");
            sb.append("File size: ").append(formatSize(file.length())).append("\n");
            sb.append("Pages: ").append(document.getNumberOfPages()).append("\n");
            sb.append("Title: ").append(orNA(info.getTitle())).append("\n");
            sb.append("Author: ").append(orNA(info.getAuthor())).append("\n");
            sb.append("Subject: ").append(orNA(info.getSubject())).append("\n");
            sb.append("Creator: ").append(orNA(info.getCreator())).append("\n");
            sb.append("Producer: ").append(orNA(info.getProducer())).append("\n");
            sb.append("Creation date: ").append(formatDate(info.getCreationDate())).append("\n");
            sb.append("Encrypted: ").append(document.isEncrypted()).append("\n");

            infoArea.setText(sb.toString());

        } catch (IOException e) {
            infoArea.setText("Error reading PDF: " + e.getMessage());
        }
    }

    private String orNA(String value) {
        return (value == null || value.isBlank()) ? "N/A" : value;
    }

    private String formatDate(Calendar calendar) {
        if (calendar == null) {
            return "N/A";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/DocumentView.fxml");
    }
}