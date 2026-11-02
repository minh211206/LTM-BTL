package com.mycompany.bai4.model;

import com.mycompany.bai4.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu và tải lịch sử tin nhắn từ MySQL.
 */
public class MessageStore {

    // Số tin nhắn lịch sử tải về khi user đăng nhập
    private static final int HISTORY_LIMIT = 50;

    /**
     * Đại diện cho một tin nhắn trong lịch sử.
     */
    public record Message(
            String type,        // "group" hoặc "private"
            String fromUser,
            String fromDisplay,
            String toUser,      // null nếu là tin nhắn nhóm
            String content,
            String sentAt
    ) {}

    /**
     * Lưu tin nhắn nhóm vào DB.
     */
    public static void saveGroupMessage(String fromUser, String fromDisplay, String content) {
        String sql = "INSERT INTO messages (type, from_user, from_display, content) VALUES ('group', ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fromUser);
            ps.setString(2, fromDisplay);
            ps.setString(3, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MessageStore] Lỗi lưu tin nhắn nhóm: " + e.getMessage());
        }
    }

    /**
     * Lưu tin nhắn riêng vào DB.
     */
    public static void savePrivateMessage(String fromUser, String fromDisplay, String toUser, String content) {
        String sql = "INSERT INTO messages (type, from_user, from_display, to_user, content) VALUES ('private', ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fromUser);
            ps.setString(2, fromDisplay);
            ps.setString(3, toUser);
            ps.setString(4, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MessageStore] Lỗi lưu tin nhắn riêng: " + e.getMessage());
        }
    }

    /**
     * Tải lịch sử tin nhắn nhóm (50 tin gần nhất).
     */
    public static List<Message> loadGroupHistory() {
        String sql = """
                SELECT type, from_user, from_display, to_user, content,
                       DATE_FORMAT(sent_at, '%H.%i %d/%m/%Y') AS sent_at
                FROM messages
                WHERE type = 'group'
                ORDER BY id DESC
                LIMIT ?
                """;
        return queryMessages(sql, HISTORY_LIMIT);
    }

    /**
     * Tải lịch sử tin nhắn riêng giữa 2 user (50 tin gần nhất).
     */
    public static List<Message> loadPrivateHistory(String user1, String user2) {
        String sql = """
                SELECT type, from_user, from_display, to_user, content,
                       DATE_FORMAT(sent_at, '%H.%i %d/%m/%Y') AS sent_at
                FROM messages
                WHERE type = 'private'
                  AND ((from_user = ? AND to_user = ?)
                    OR (from_user = ? AND to_user = ?))
                ORDER BY id DESC
                LIMIT ?
                """;
        List<Message> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, user2);
            ps.setString(4, user1);
            ps.setInt(5, HISTORY_LIMIT);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Message(
                            rs.getString("type"),
                            rs.getString("from_user"),
                            rs.getString("from_display"),
                            rs.getString("to_user"),
                            rs.getString("content"),
                            rs.getString("sent_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MessageStore] Lỗi tải lịch sử riêng: " + e.getMessage());
        }
        // Đảo lại thứ tự (cũ → mới)
        java.util.Collections.reverse(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------
    private static List<Message> queryMessages(String sql, int limit) {
        List<Message> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Message(
                            rs.getString("type"),
                            rs.getString("from_user"),
                            rs.getString("from_display"),
                            rs.getString("to_user"),
                            rs.getString("content"),
                            rs.getString("sent_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MessageStore] Lỗi tải lịch sử: " + e.getMessage());
        }
        // Đảo lại thứ tự (cũ → mới)
        java.util.Collections.reverse(result);
        return result;
    }
}
