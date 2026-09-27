package com.fileforge.controller;

import com.fileforge.database.DatabaseManager;
import com.fileforge.model.ActivityLogEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class HomeController {

    @FXML private HBox mainRootContainer;
    @FXML private VBox leftNavigationColumn;
    @FXML private VBox rightActivityColumn;
    @FXML private TableView<ActivityLogEntry> recentHistoryTable;
    @FXML private TableColumn<ActivityLogEntry, String> operationColumn;
    @FXML private TableColumn<ActivityLogEntry, String> fileColumn;
    @FXML private TableColumn<ActivityLogEntry, String> notesColumn;

    @FXML
    public void initialize() {
        operationColumn.setCellValueFactory(data -> data.getValue().operationProperty());
        fileColumn.setCellValueFactory(data -> data.getValue().fileProperty());

        setupWrappingCell(operationColumn);
        setupWrappingCell(fileColumn);

        notesColumn.setCellValueFactory(data -> data.getValue().notesProperty());
        notesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        notesColumn.setOnEditCommit(event -> {
            ActivityLogEntry entry = event.getRowValue();
            String newNotes = event.getNewValue();
            entry.setNotes(newNotes);
            updateNotesInDatabase(entry.getLogId(), newNotes);
        });
        recentHistoryTable.setEditable(true);

        hideTableHeader(recentHistoryTable);

        Platform.runLater(() -> {
            setupResponsiveLayoutBindings();
            readLogsFromDatabase();
        });
    }

    private void setupWrappingCell(TableColumn<ActivityLogEntry, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            private final Text text = new Text();

            {
                text.getStyleClass().add("wrap-cell-label");
                text.wrappingWidthProperty().bind(col.widthProperty().subtract(12));
                setPrefHeight(Control.USE_COMPUTED_SIZE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                }
                setText(null);
            }
        });
    }

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
                "SELECT log_id, operation, file_source, save_destination, notes FROM operational_history ORDER BY log_id DESC;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                int logId = rs.getInt("log_id");
                String operation = rs.getString("operation");
                String source = rs.getString("file_source");
                String destination = rs.getString("save_destination");
                String notes = rs.getString("notes");

                String displayFile = (destination == null || destination.isBlank()) ? source : destination;
                entries.add(new ActivityLogEntry(logId, operation, displayFile, notes));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        recentHistoryTable.setItems(entries);
    }

    private void updateNotesInDatabase(int logId, String notes) {
        String updateQuery = "UPDATE operational_history SET notes = ? WHERE log_id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, notes);
            pstmt.setInt(2, logId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteSelected() {
        ActivityLogEntry selected = recentHistoryTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String deleteQuery = "DELETE FROM operational_history WHERE log_id = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, selected.getLogId());
            pstmt.executeUpdate();
            recentHistoryTable.getItems().remove(selected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearAll() {
        if (recentHistoryTable.getItems().isEmpty()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will permanently delete all activity log entries. Continue?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Clear Activity Log");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            String deleteAllQuery = "DELETE FROM operational_history WHERE user_fk = 1;";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteAllQuery)) {
                pstmt.executeUpdate();
                recentHistoryTable.getItems().clear();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML private void goToDocument(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/DocumentView.fxml"); }
    @FXML private void goToImage(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/ImageView.fxml"); }
    @FXML private void goToText(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/TextView.fxml"); }
}