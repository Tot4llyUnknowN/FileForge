package com.fileforge.controller;

import com.fileforge.database.DatabaseManager;
import com.fileforge.model.ActivityLogEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeController {

    @FXML private HBox mainRootContainer;
    @FXML private VBox leftNavigationColumn;
    @FXML private VBox rightActivityColumn;
    @FXML private TableView<ActivityLogEntry> recentHistoryTable;
    @FXML private TableColumn<ActivityLogEntry, String> operationColumn;
    @FXML private TableColumn<ActivityLogEntry, String> fileColumn;

    @FXML
    public void initialize() {
        operationColumn.setCellValueFactory(data -> data.getValue().operationProperty());
        fileColumn.setCellValueFactory(data -> data.getValue().fileProperty());

        hideTableHeader(recentHistoryTable);

        Platform.runLater(() -> {
            setupResponsiveLayoutBindings();
            readLogsFromDatabase();
        });
    }

    /**
     * FIX: CSS alone (-fx-max-height: 0 on .column-header-background) is not
     * reliably respected by JavaFX's TableView skin across versions, so the
     * header row still rendered visually. Forcing the actual header node
     * hidden/unmanaged once the skin is attached removes it completely.
     */
    private void hideTableHeader(TableView<?> table) {
        table.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            Pane header = (Pane) table.lookup("TableHeaderRow");
            if (header != null) {
                header.setMinHeight(0);
                header.setPrefHeight(0);
                header.setMaxHeight(0);
                header.setVisible(false);
                header.setManaged(false);
            }
        });
    }

    private void setupResponsiveLayoutBindings() {
        if (mainRootContainer.getScene() == null) return;
        rightActivityColumn.prefWidthProperty().bind(mainRootContainer.widthProperty().multiply(0.35));
        rightActivityColumn.minWidthProperty().setValue(280);
        rightActivityColumn.maxWidthProperty().setValue(480);
        leftNavigationColumn.prefWidthProperty().bind(mainRootContainer.widthProperty().multiply(0.65));
    }

    private void readLogsFromDatabase() {
        ObservableList<ActivityLogEntry> entries = FXCollections.observableArrayList();
        String selectQuery =
                "SELECT operation, file_source, save_destination FROM operational_history ORDER BY log_id DESC;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String operation = rs.getString("operation");
                String source = rs.getString("file_source");
                String destination = rs.getString("save_destination");

                String displayFile = (destination == null || destination.isBlank()) ? source : destination;
                entries.add(new ActivityLogEntry(operation, displayFile));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        recentHistoryTable.setItems(entries);
    }

    @FXML private void goToDocument(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/DocumentView.fxml"); }
    @FXML private void goToImage(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/ImageView.fxml"); }
    @FXML private void goToText(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/TextView.fxml"); }
}