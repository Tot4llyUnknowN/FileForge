package com.fileforge.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:fileforge.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String enableForeignKeys = "PRAGMA foreign_keys = ON;";

        String createUserTable = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(enableForeignKeys);
            stmt.execute(createUserTable);
            initializeHistoryTable(conn);

            System.out.println("[SQLite Engine] Relational structures initialized successfully.");
            seedDefaultUser(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void initializeHistoryTable(Connection conn) throws SQLException {
        String createHistoryTable = """
            CREATE TABLE IF NOT EXISTS operational_history (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_fk INTEGER NOT NULL,
                operation TEXT NOT NULL,
                file_source TEXT NOT NULL,
                save_destination TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_fk) REFERENCES users(user_id) ON DELETE CASCADE
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            boolean tableExists = false;
            boolean hasOperationColumn = false;

            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(operational_history);")) {
                while (rs.next()) {
                    tableExists = true;
                    if ("operation".equalsIgnoreCase(rs.getString("name"))) {
                        hasOperationColumn = true;
                    }
                }
            }

            if (tableExists && !hasOperationColumn) {
                stmt.execute("DROP TABLE IF EXISTS operational_history;");
                System.out.println("[SQLite Engine] Migrated operational_history to new schema.");
            }

            stmt.execute(createHistoryTable);
        }
    }

    private static void seedDefaultUser(Connection conn) throws SQLException {
        String checkUserQuery = "SELECT COUNT(*) FROM users;";
        String insertUserQuery = "INSERT OR IGNORE INTO users (user_id, username) VALUES (1, 'Workspace_User');";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkUserQuery)) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute(insertUserQuery);
                System.out.println("[SQLite Engine] Seeded default user profile identity.");
            }
        }
    }
}