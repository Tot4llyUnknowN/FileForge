package com.fileforge.controller.image;

import com.fileforge.controller.NavigationHelper;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.drew.metadata.Metadata;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;

public class MetadataViewController {

    @FXML private Button backButton;
    @FXML private Label fileLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<MetadataTagRow> metadataTable;
    @FXML private TableColumn<MetadataTagRow, String> categoryColumn;
    @FXML private TableColumn<MetadataTagRow, String> tagColumn;
    @FXML private TableColumn<MetadataTagRow, String> valueColumn;

    @FXML
    public void initialize() {
        // Wire table column data mappings
        categoryColumn.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        tagColumn.setCellValueFactory(cellData -> cellData.getValue().tagProperty());
        valueColumn.setCellValueFactory(cellData -> cellData.getValue().valueProperty());
    }

    @FXML
    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image to Scan Metadata");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.webp", "*.bmp"));

        File file = chooser.showOpenDialog(backButton.getScene().getWindow());
        if (file == null) return;

        fileLabel.setText(file.getName());
        metadataTable.getItems().clear();
        statusLabel.setText("Scanning headers...");

        try {
            // Extract the metadata blocks using Drew Noakes library engine
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            for (Directory directory : metadata.getDirectories()) {
                String directoryName = directory.getName();
                for (Tag tag : directory.getTags()) {
                    String tagName = tag.getTagName();
                    String description = tag.getDescription();

                    // Add row definition mapping to our localized visual array
                    metadataTable.getItems().add(new MetadataTagRow(directoryName, tagName, description));
                }
            }

            if (metadataTable.getItems().isEmpty()) {
                statusLabel.setText("Scan complete: No embedded metadata tags found in this file.");
            } else {
                statusLabel.setText("Successfully parsed " + metadataTable.getItems().size() + " metadata fields!");
            }

        } catch (Exception e) {
            statusLabel.setText("Failed to parse metadata: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        NavigationHelper.navigate(backButton, "/view/ImageView.fxml");
    }

    // Local inner tracking data architecture for clean TableView population mechanics
    public static class MetadataTagRow {
        private final SimpleStringProperty category;
        private final SimpleStringProperty tag;
        private final SimpleStringProperty value;

        public MetadataTagRow(String category, String tag, String value) {
            this.category = new SimpleStringProperty(category);
            this.tag = new SimpleStringProperty(tag);
            this.value = new SimpleStringProperty(value != null ? value : "");
        }

        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty tagProperty() { return tag; }
        public SimpleStringProperty valueProperty() { return value; }
    }
}
