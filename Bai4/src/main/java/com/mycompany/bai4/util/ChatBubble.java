package com.mycompany.bai4.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class ChatBubble extends JPanel {

    private static final int MAX_WIDTH = 340;
    private static final int ARC       = 18;

    private final Color bg;
    private final Color border;

    /**
     * @param text   nội dung tin nhắn
     * @param isSelf true nếu là tin của chính mình
     * @param time   giờ hiển thị dưới bubble, ví dụ "07:11" — null để ẩn
     */
    public ChatBubble(String text, boolean isSelf, String time) {
        this.bg     = isSelf ? ChatColor.BUBBLE_SELF : ChatColor.BUBBLE_OTHER;
        this.border = isSelf ? null : ChatColor.BUBBLE_OTHER_BORDER;
        Color fg    = isSelf ? ChatColor.BUBBLE_SELF_TEXT : ChatColor.BUBBLE_OTHER_TEXT;

        setOpaque(false);
        setLayout(new BorderLayout(0, 2));
        setBorder(new EmptyBorder(9, 13, 6, 13));

        // --- Nội dung ---
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setBorder(null);
        textArea.setFont(ChatColor.FONT_MAIN);
        textArea.setForeground(fg);

        FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
        int naturalWidth = fm.stringWidth(text) + 4;
        int width = Math.min(naturalWidth, MAX_WIDTH);
        textArea.setSize(width, Short.MAX_VALUE);
        Dimension pref = textArea.getPreferredSize();
        textArea.setPreferredSize(new Dimension(width, pref.height));

        add(textArea, BorderLayout.CENTER);

        // --- Giờ nhỏ bên dưới (như Zalo) ---
        if (time != null && !time.isEmpty()) {
            JLabel timeLbl = new JLabel(time);
            timeLbl.setFont(ChatColor.FONT_SMALL.deriveFont(10f));
            timeLbl.setForeground(isSelf
                    ? new Color(255, 255, 255, 160)
                    : new Color(120, 120, 120));
            timeLbl.setHorizontalAlignment(isSelf ? SwingConstants.RIGHT : SwingConstants.LEFT);
            add(timeLbl, BorderLayout.SOUTH);
        }

        Dimension bubblePref = getPreferredSize();
        setPreferredSize(bubblePref);
        setMinimumSize(bubblePref);
        setMaximumSize(bubblePref);
    }

    /** Constructor tương thích ngược – không có giờ */
    public ChatBubble(String text, boolean isSelf) {
        this(text, isSelf, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        if (border != null) {
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
