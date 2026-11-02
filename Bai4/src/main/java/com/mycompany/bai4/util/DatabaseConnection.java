package com.mycompany.bai4.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/chatapp?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "123456";

    /**
     * Trả về một Connection mới tới MySQL.
     * Caller chịu trách nhiệm đóng connection sau khi dùng.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * Tự động tạo bảng users và messages nếu chưa tồn tại.
     * Gọi một lần khi khởi động server.
     */
    public static void initSchema() {
        String sqlUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    username      VARCHAR(50)  NOT NULL PRIMARY KEY,
                    password_hash VARCHAR(64)  NOT NULL,
                    display_name  VARCHAR(100) NOT NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        // type: 'group' = tin nhắn nhóm, 'private' = tin nhắn riêng
        // to_user: NULL nếu là tin nhắn nhóm
        String sqlMessages = """
                CREATE TABLE IF NOT EXISTS messages (
                    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    type         ENUM('group','private') NOT NULL DEFAULT 'group',
                    from_user    VARCHAR(50)  NOT NULL,
                    from_display VARCHAR(100) NOT NULL,
                    to_user      VARCHAR(50)  NULL,
                    content      TEXT         NOT NULL,
                    sent_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;

        try (Connection conn = getConnection();
             Statement  stmt = conn.createStatement()) {
            stmt.execute(sqlUsers);
            stmt.execute(sqlMessages);
            System.out.println("[DB] Schema khởi tạo thành công.");
        } catch (SQLException e) {
            System.err.println("[DB] Lỗi khởi tạo schema: " + e.getMessage());
        }
    }
}
