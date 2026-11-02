package com.mycompany.bai4.model;

import com.mycompany.bai4.util.DatabaseConnection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserStore {

    public UserStore() {
        // Tạo bảng nếu chưa có
        DatabaseConnection.initSchema();
    }

    /**
     * Đăng ký người dùng mới.
     * @return true nếu đăng ký thành công, false nếu username đã tồn tại.
     */
    public synchronized boolean register(String username, String plainPassword, String displayName) {
        if (username == null || username.isBlank()) return false;
        if (exists(username)) return false;

        String dn  = (displayName == null || displayName.isBlank()) ? username : displayName.trim();
        String sql = "INSERT INTO users (username, password_hash, display_name) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash(plainPassword));
            ps.setString(3, dn);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[UserStore] Lỗi đăng ký: " + e.getMessage());
            return false;
        }
    }

    /**
     * Kiểm tra thông tin đăng nhập.
     */
    public boolean login(String username, String plainPassword) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password_hash").equals(hash(plainPassword));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserStore] Lỗi đăng nhập: " + e.getMessage());
        }
        return false;
    }

    /**
     * Lấy display name của người dùng.
     */
    public String getDisplayName(String username) {
        String sql = "SELECT display_name FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("display_name");
            }
        } catch (SQLException e) {
            System.err.println("[UserStore] Lỗi lấy display name: " + e.getMessage());
        }
        return username;
    }

    /**
     * Kiểm tra username đã tồn tại chưa.
     */
    public boolean exists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[UserStore] Lỗi kiểm tra tồn tại: " + e.getMessage());
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------
    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 không khả dụng", e);
        }
    }
}
