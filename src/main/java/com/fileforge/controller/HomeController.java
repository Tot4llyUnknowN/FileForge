package com.fileforge.controller;

import com.fileforge.database.DatabaseManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeController {

    @FXML private HBox mainRootContainer;
    @FXML private VBox leftNavigationColumn;
    @FXML private VBox rightActivityColumn;
    @FXML private ListView<String> recentHistoryListView;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            setupResponsiveLayoutBindings();
            readLogsFromDatabase();
        });
    }

    private void setupResponsiveLayoutBindings() {
        if (mainRootContainer.getScene() == null) return;
        rightActivityColumn.prefWidthProperty().bind(mainRootContainer.widthProperty().multiply(0.35));
        rightActivityColumn.minWidthProperty().setValue(280);
        rightActivityColumn.maxWidthProperty().setValue(480);
        leftNavigationColumn.prefWidthProperty().bind(mainRootContainer.widthProperty().multiply(0.65));
    }

    /**
     * FIX: the Recent Activity Log is now fully automatic and read-only.
     * Every real tool operation writes its own row via ActivityLogger at the
     * moment it succeeds (see the various tool controllers), so this view's
     * only job is to read those rows back and format them for display. The
     * old manual create/update/delete/purge controls have been removed from
     * HomeView.fxml along with their handlers.
     */
    private void readLogsFromDatabase() {
        recentHistoryListView.getItems().clear();
        String selectQuery =
                "SELECT operation, file_source, save_destination FROM operational_history ORDER BY log_id DESC;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String operation = rs.getString("operation");
                String source = rs.getString("file_source");
                String destination = rs.getString("save_destination");
                recentHistoryListView.getItems().add(formatLogEntry(operation, source, destination));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String formatLogEntry(String operation, String source, String destination) {
        if (destination == null || destination.isBlank()) {
            return "[" + operation + "] " + source;
        }
        return "[" + operation + "] " + source + "  →  " + destination;
    }

    @FXML private void goToDocument(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/DocumentView.fxml"); }
    @FXML private void goToImage(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/ImageView.fxml"); }
    @FXML private void goToText(MouseEvent event) { NavigationHelper.navigate(mainRootContainer, "/view/TextView.fxml"); }
}
