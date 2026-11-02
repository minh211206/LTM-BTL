package com.mycompany.bai4.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;


public class ChatFile extends JPanel {

    private static final int ARC = 16;
    private final Color bg;

    public ChatFile(String name, long size, File file, boolean isSelf) {
        this.bg = isSelf ? ChatColor.BUBBLE_SELF : ChatColor.BUBBLE_OTHER;
        Color fg      = isSelf ? ChatColor.BUBBLE_SELF_TEXT : ChatColor.BUBBLE_OTHER_TEXT;
        Color fgDim   = isSelf ? new Color(255, 255, 255, 190) : new Color(90, 90, 95);
        Color btnBg   = isSelf ? Color.WHITE : ChatColor.ACCENT;
        Color btnFg   = isSelf ? ChatColor.ACCENT : Color.WHITE;

        setOpaque(false);
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(10, 14, 10, 12));
        setPreferredSize(new Dimension(300, 62));
        setMaximumSize(new Dimension(300, 62));

        // Icon emoji theo loại file
        JLabel iconLbl = new JLabel(getFileIcon(name));
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconLbl.setOpaque(false);

        // Tên file
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(ChatColor.FONT_BOLD);
        nameLbl.setForeground(fg);

        // Dung lượng
        JLabel sizeLbl = new JLabel(formatSize(size));
        sizeLbl.setFont(ChatColor.FONT_SMALL);
        sizeLbl.setForeground(fgDim);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(nameLbl);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(sizeLbl);

        // Nút mở file
        JButton openBtn = new JButton("▶ Mở");
        openBtn.setFont(ChatColor.FONT_SMALL);
        openBtn.setBackground(btnBg);
        openBtn.setForeground(btnFg);
        openBtn.setFocusPainted(false);
        openBtn.setBorderPainted(false);
        openBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        openBtn.setEnabled(file != null && file.exists());
        if (file != null && file.exists()) {
            openBtn.addActionListener(e -> {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Không mở được file:\n" + ex.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        add(iconLbl,   BorderLayout.WEST);
        add(infoPanel, BorderLayout.CENTER);
        add(openBtn,   BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
        g2.dispose();
        super.paintComponent(g);
    }

    // Định dạng dung lượng file (B / KB / MB / GB)
    public static String formatSize(long bytes) {
        if (bytes < 1024)               return bytes + " B";
        if (bytes < 1024 * 1024)        return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    // Trả về icon theo đuôi file 
    public static String getFileIcon(String name) {
        String n = name.toLowerCase();
        if (n.matches(".*\\.(png|jpg|jpeg|gif|bmp|webp|svg)"))       return "\uD83D\uDDBC";  // 🖼
        if (n.matches(".*\\.(mp4|avi|mkv|mov|wmv|flv)"))             return "\uD83C\uDFAC";  // 🎬
        if (n.matches(".*\\.(mp3|wav|flac|aac|ogg|m4a)"))            return "\uD83C\uDFB5";  // 🎵
        if (n.matches(".*\\.(pdf)"))                                  return "\uD83D\uDCC4";  // 📄
        if (n.matches(".*\\.(doc|docx)"))                             return "\uD83D\uDCDD";  // 📝
        if (n.matches(".*\\.(xls|xlsx|csv)"))                         return "\uD83D\uDCCA";  // 📊
        if (n.matches(".*\\.(ppt|pptx)"))                             return "\uD83D\uDCCB";  // 📋
        if (n.matches(".*\\.(zip|rar|7z|tar|gz)"))                    return "\uD83D\uDDDC";  // 🗜
        if (n.matches(".*\\.(exe|msi|apk|dmg)"))                      return "\u2699";         // ⚙
        if (n.matches(".*\\.(java|py|js|ts|html|css|cpp|c|json)"))    return "\uD83D\uDCBB";  // 💻
        return "\uD83D\uDCC1";  // 📁
    }
}