package com.fileforge.controller.document;

import com.fileforge.controller.NavigationHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class ExcelToCsvController {

    @FXML private Label fileLabel;
    @FXML private Label statusLabel;
    @FXML private ComboBox<String> sheetComboBox;
    @FXML private Button backButton;

    private File selectedFile;

    @FXML
    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Excel File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));

        File file = chooser.showOpenDialog(backButton.getScene().getWindow());
        if (file == null) {
            return;
        }

        selectedFile = file;
        fileLabel.setText(file.getName());
        statusLabel.setText("");
        loadSheetNames();
    }

    private void loadSheetNames() {
        sheetComboBox.getItems().clear();

        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetComboBox.getItems().add(workbook.getSheetName(i));
            }

            if (!sheetComboBox.getItems().isEmpty()) {
                sheetComboBox.getSelectionModel().selectFirst();
            }

        } catch (IOException e) {
            statusLabel.setText("Error reading Excel file: " + e.getMessage());
        }
    }

    @FXML
    private void convertToCsv() {
        if (selectedFile == null) {
            statusLabel.setText("Please select an Excel file first.");
            return;
        }

        String sheetName = sheetComboBox.getValue();
        if (sheetName == null) {
            statusLabel.setText("Please select a sheet.");
            return;
        }

        FileChooser saveChooser = new FileChooser();
        saveChooser.setTitle("Save CSV");
        String baseName = selectedFile.getName().replaceFirst("\\.xlsx$", "");
        saveChooser.setInitialFileName(baseName + ".csv");
        saveChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File outputFile = saveChooser.showSaveDialog(backButton.getScene().getWindow());
        if (outputFile == null) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(selectedFile);
             Workbook workbook = new XSSFWorkbook(fis);
             FileWriter writer = new FileWriter(outputFile)) {

            Sheet sheet = workbook.getSheet(sheetName);
            DataFormatter formatter = new DataFormatter();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");

            for (Row row : sheet) {
                StringBuilder line = new StringBuilder();
                int lastCol = row.getLastCellNum();

                for (int col = 0; col < lastCol; col++) {
                    Cell cell = row.getCell(col);
                    String value = formatCellAsCsvValue(cell, formatter, dateFormat);

                    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                        value = "\"" + value.replace("\"", "\"\"") + "\"";
                    }

                    line.append(value);
                    if (col < lastCol - 1) {
                        line.append(",");
                    }
                }

                writer.write(line.toString());
                writer.write("\n");
            }

            statusLabel.setText("CSV saved successfully: " + outputFile.getName());

        } catch (IOException e) {
            statusLabel.setText("Error converting to CSV: " + e.getMessage());
        }
    }
    private String formatCellAsCsvValue(Cell cell, DataFormatter formatter,
                                        java.text.SimpleDateFormat dateFormat) {
        if (cell == null) {
            return "";
        }

        if (cell.getCellType() == CellType.NUMERIC
                && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
            return dateFormat.format(cell.getDateCellValue());
        }

        return formatter.formatCellValue(cell);
    }
    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/DocumentView.fxml");
    }
}