package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.FileChooser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

import java.io.File;
import java.io.IOException;

public class UnlockPdfController {

    @FXML private Label fileLabel;
    @FXML private Label statusLabel;
    @FXML private PasswordField passwordField;
    @FXML private Button backButton;

    private File selectedFile;

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
        statusLabel.setText("");
    }

    @FXML
    private void unlockPdf() {
        if (selectedFile == null) {
            statusLabel.setText("Please select a PDF file first.");
            return;
        }

        String password = passwordField.getText();
        if (password == null) {
            password = "";
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Unlocked PDF");
        saveChooser.setInitialFileName("unlocked_" + selectedFile.getName());
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File outputFile = saveChooser.showSaveDialog(backButton.getScene().getWindow());
        if (outputFile == null) {
            return;
        }

        try (PDDocument document = Loader.loadPDF(selectedFile, password)) {
            document.setAllSecurityToBeRemoved(true);
            document.save(outputFile);
            statusLabel.setText("PDF unlocked successfully: " + outputFile.getName());

        } catch (InvalidPasswordException e) {
            statusLabel.setText("Incorrect password.");
        } catch (IOException e) {
            statusLabel.setText("Error unlocking PDF: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/DocumentView.fxml");
    }
}