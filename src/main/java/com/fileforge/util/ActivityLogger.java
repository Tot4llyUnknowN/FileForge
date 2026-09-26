package com.fileforge.util;

import com.fileforge.database.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ActivityLogger {

    private ActivityLogger() {}

    public static void log(String operation, String fileSource, String saveDestination) {
        String insertQuery =
                "INSERT INTO operational_history (user_fk, operation, file_source, save_destination) VALUES (1, ?, ?, ?);";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, operation);
            pstmt.setString(2, fileSource);
            pstmt.setString(3, saveDestination);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void log(String operation, String fileSource) {
        log(operation, fileSource, null);
    }

    public static String joinPaths(List<File> files) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            if (i > 0) sb.append("; ");
            sb.append(files.get(i).getAbsolutePath());
        }
        return sb.toString();
    }
}