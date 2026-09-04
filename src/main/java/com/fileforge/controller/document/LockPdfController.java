package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.FileChooser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;

public class LockPdfController {

    @FXML private Label fileLabel;
    @FXML private Label statusLabel;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox allowPrintingCheck;
    @FXML private CheckBox allowModifyCheck;
    @FXML private CheckBox allowCopyCheck;
    @FXML private CheckBox allowAnnotateCheck;
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
    private void lockPdf() {
        if (selectedFile == null) {
            statusLabel.setText("Please select a PDF file first.");
            return;
        }

        String userPassword = passwordField.getText();
        if (userPassword == null || userPassword.isBlank()) {
            statusLabel.setText("Please enter a password.");
            return;
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save Locked PDF");
        saveChooser.setInitialFileName("locked_" + selectedFile.getName());
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File outputFile = saveChooser.showSaveDialog(backButton.getScene().getWindow());
        if (outputFile == null) {
            return;
        }

        try (PDDocument document = Loader.loadPDF(selectedFile)) {
            AccessPermission permission = new AccessPermission();
            permission.setCanPrint(allowPrintingCheck.isSelected());
            permission.setCanModify(allowModifyCheck.isSelected());
            permission.setCanExtractContent(allowCopyCheck.isSelected());
            permission.setCanModifyAnnotations(allowAnnotateCheck.isSelected());

            String ownerPassword = generateOwnerPassword();

            StandardProtectionPolicy policy =
                    new StandardProtectionPolicy(ownerPassword, userPassword, permission);
            policy.setEncryptionKeyLength(128);

            document.protect(policy);
            document.save(outputFile);

            statusLabel.setText("PDF locked successfully: " + outputFile.getName());

        } catch (IOException e) {
            statusLabel.setText("Error locking PDF: " + e.getMessage());
        }
    }

    private String generateOwnerPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 24; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/DocumentView.fxml");
    }
}