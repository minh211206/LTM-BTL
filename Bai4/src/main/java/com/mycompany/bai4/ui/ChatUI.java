package com.mycompany.bai4.ui;

import com.mycompany.bai4.util.ChatColor;
import com.mycompany.bai4.util.ChatFile;
import com.mycompany.bai4.util.ChatBubble;
import com.mycompany.bai4.client.ChatClient;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChatUI extends JFrame {

    public static final String GROUP_ID    = "GROUP";
    private static final String GROUP_LABEL = "Nhóm chat";

    private final ChatClient client;
    private final String selfUsername;
    private final String selfDisplayName;
    private final String serverHost;

    private final Map<String, StyledDocument> conversationDocs = new LinkedHashMap<>();
    private final Map<String, Integer>        unreadCounts     = new HashMap<>();
    private final Map<String, String>         lastDatePerConv  = new HashMap<>();
    private String activeConversation = GROUP_ID;

    private JTextPane  chatArea;
    private JTextField inputField;
    private JButton    sendBtn;
    private JButton    fileBtn;
    private JLabel     chatTitleLbl;
    private JLabel     statusLabel;
    private DefaultListModel<ConvItem> convListModel;
    private JList<ConvItem>            convList;

    public ChatUI(ChatClient client, String selfUsername, String selfDisplayName, String serverHost) {
        super("Client-Server Chat");
        this.client          = client;
        this.selfUsername    = selfUsername;
        this.selfDisplayName = selfDisplayName;
        this.serverHost      = serverHost;
        buildUI();
    }

    // ── Inner classes ─────────────────────────────────────────────────────────

    private static class ConvItem {
        final String id;
        final String label;
        final boolean group;

        ConvItem(String id, String label, boolean group) {
            this.id = id; this.label = label; this.group = group;
        }

        @Override public boolean equals(Object o) {
            return o instanceof ConvItem && ((ConvItem) o).id.equals(id);
        }
        @Override public int hashCode() { return id.hashCode(); }
    }

    private static class UnreadBadge extends JLabel {
        private static final int SIZE = 16;
        UnreadBadge(int count) {
            super(count > 9 ? "9+" : String.valueOf(count), SwingConstants.CENTER);
            setFont(ChatColor.FONT_SMALL.deriveFont(Font.BOLD, 10f));
            setForeground(ChatColor.BADGE_TEXT);
            setOpaque(false);
            Dimension d = new Dimension(SIZE, SIZE);
            setPreferredSize(d); setMinimumSize(d); setMaximumSize(d);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(ChatColor.BADGE_BG);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedPanel extends JPanel {
        private final Color bg; private final int arc;
        RoundedPanel(Color bg, int arc) { this.bg = bg; this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose(); super.paintComponent(g);
        }
    }

    private static class RoundButton extends JButton {
        private final Color bg;
        RoundButton(String text, Color bg, Color fg) {
            super(text); this.bg = bg; setForeground(fg);
            setContentAreaFilled(false); setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose(); super.paintComponent(g);
        }
    }

    // ── UI build ──────────────────────────────────────────────────────────────

    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 640);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ChatColor.BG_PAGE);
        setLayout(new BorderLayout(0, 0));

        conversationDocs.put(GROUP_ID, new DefaultStyledDocument());
        convListModel = new DefaultListModel<>();
        convListModel.addElement(new ConvItem(GROUP_ID, GROUP_LABEL, true));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildSidebar(), buildChatPanel());
        split.setDividerLocation(270);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setBackground(ChatColor.DIVIDER);
        add(split, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { client.closeConnection(); }
        });

        chatArea.setDocument(conversationDocs.get(GROUP_ID));
        convList.setSelectedIndex(0);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ChatColor.BG_PANEL);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, ChatColor.DIVIDER));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ChatColor.BG_PANEL);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ChatColor.DIVIDER),
                new EmptyBorder(14, 16, 14, 16)));
        JLabel headerLbl = new JLabel("Đoạn chat");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLbl.setForeground(ChatColor.TEXT_MAIN);
        statusLabel = new JLabel("●");
        statusLabel.setFont(ChatColor.FONT_SMALL);
        statusLabel.setForeground(ChatColor.ACCENT2);
        statusLabel.setToolTipText("Đã kết nối – " + serverHost);
        header.add(headerLbl,   BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);

        convList = new JList<>(convListModel);
        convList.setBackground(ChatColor.BG_PANEL);
        convList.setFixedCellHeight(56);
        convList.setBorder(new EmptyBorder(4, 0, 4, 0));
        convList.setCellRenderer(new ConvCellRenderer());
        convList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            ConvItem sel = convList.getSelectedValue();
            if (sel != null) {
                unreadCounts.remove(sel.id);
                switchConversation(sel.id, sel.label);
                convList.repaint();
            }
        });

        JScrollPane convScroll = new JScrollPane(convList);
        convScroll.setBorder(null);

        sidebar.add(header,     BorderLayout.NORTH);
        sidebar.add(convScroll, BorderLayout.CENTER);
        sidebar.setPreferredSize(new Dimension(270, 0));
        return sidebar;
    }

    private class ConvCellRenderer implements ListCellRenderer<ConvItem> {
        @Override
        public Component getListCellRendererComponent(JList<? extends ConvItem> list, ConvItem value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(value.group
                    ? new CompoundBorder(new MatteBorder(0, 0, 1, 0, ChatColor.DIVIDER), new EmptyBorder(10, 16, 10, 14))
                    : new EmptyBorder(10, 16, 10, 14));
            Color bg = isSelected ? ChatColor.SIDEBAR_SELECTED
                    : value.group ? ChatColor.SIDEBAR_GROUP_TINT
                    : ChatColor.BG_PANEL;
            row.setBackground(bg);
            JLabel nameLbl = new JLabel(value.label);
            nameLbl.setFont(value.group ? ChatColor.FONT_BOLD : ChatColor.FONT_MAIN);
            nameLbl.setForeground(isSelected || value.group ? ChatColor.ACCENT : ChatColor.TEXT_MAIN);
            row.add(nameLbl, BorderLayout.CENTER);
            int unread = unreadCounts.getOrDefault(value.id, 0);
            if (unread > 0) {
                JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                badgeWrap.setOpaque(false);
                badgeWrap.add(new UnreadBadge(unread));
                row.add(badgeWrap, BorderLayout.EAST);
            }
            return row;
        }
    }

    private JPanel buildChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBackground(ChatColor.BG_PAGE);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(ChatColor.BG_PANEL);
        titlePanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, ChatColor.DIVIDER),
                new EmptyBorder(14, 16, 14, 16)));
        chatTitleLbl = new JLabel(GROUP_LABEL);
        chatTitleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chatTitleLbl.setForeground(ChatColor.ACCENT);
        titlePanel.add(chatTitleLbl, BorderLayout.WEST);
        chatPanel.add(titlePanel, BorderLayout.NORTH);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(ChatColor.BG_PAGE);
        chatArea.setForeground(ChatColor.TEXT_MAIN);
        chatArea.setFont(ChatColor.FONT_MAIN);
        chatArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(null);
        chatScroll.getViewport().setBackground(ChatColor.BG_PAGE);
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(ChatColor.BG_PANEL);
        inputPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, ChatColor.DIVIDER),
                new EmptyBorder(10, 14, 10, 14)));

        inputField = new JTextField();
        inputField.setBackground(ChatColor.BG_INPUT);
        inputField.setForeground(ChatColor.TEXT_MAIN);
        inputField.setCaretColor(ChatColor.TEXT_MAIN);
        inputField.setFont(ChatColor.FONT_MAIN);
        inputField.setOpaque(false);
        inputField.setBorder(null);

        RoundedPanel inputWrap = new RoundedPanel(ChatColor.BG_INPUT, 22);
        inputWrap.setLayout(new BorderLayout());
        inputWrap.setBorder(new EmptyBorder(8, 16, 8, 16));
        inputWrap.add(inputField, BorderLayout.CENTER);

        fileBtn = buildRoundButton("File", new Color(224, 226, 230), ChatColor.TEXT_MAIN, 64);
        sendBtn = buildRoundButton("Gửi", ChatColor.ACCENT, Color.WHITE, 76);

        inputPanel.add(inputWrap, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(fileBtn);
        btnPanel.add(sendBtn);
        inputPanel.add(btnPanel, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        fileBtn.addActionListener(e -> sendFile());

        return chatPanel;
    }

    private JButton buildRoundButton(String text, Color bg, Color fg, int width) {
        RoundButton btn = new RoundButton(text, bg, fg);
        btn.setFont(ChatColor.FONT_BOLD);
        btn.setPreferredSize(new Dimension(width, 40));
        return btn;
    }

    // ── Conversation switching ─────────────────────────────────────────────────

    private void switchConversation(String convId, String label) {
        activeConversation = convId;
        chatTitleLbl.setText(label);
        StyledDocument doc = conversationDocs.computeIfAbsent(convId, k -> new DefaultStyledDocument());
        chatArea.setDocument(doc);
        chatArea.setCaretPosition(doc.getLength());
        inputField.requestFocusInWindow();

        // Nếu là tab PM và chưa có lịch sử → yêu cầu server gửi lịch sử
        if (!GROUP_ID.equals(convId) && doc.getLength() == 0) {
            client.requestPrivateHistory(convId);
        }
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        if (GROUP_ID.equals(activeConversation)) {
            client.sendMessage(text);
        } else {
            client.sendPrivateMessage(activeConversation, text);
        }
        String now = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        appendMessage(activeConversation, "Bạn", text, true, now);
        inputField.setText("");
    }

    private void sendFile() {
        JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fc.setDialogTitle("Chọn file để gửi");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        String to = GROUP_ID.equals(activeConversation) ? "ALL" : activeConversation;
        client.sendFile(file, to);
    }

    // ── Public API (gọi từ ChatClient) ────────────────────────────────────────

    public void updateUserList(List<String[]> users) {
        ConvItem selected = convList.getSelectedValue();
        convListModel.clear();
        convListModel.addElement(new ConvItem(GROUP_ID, GROUP_LABEL, true));
        for (String[] u : users) {
            String dn   = u[0];
            String user = u[1];
            if (user.equals(selfUsername)) continue;
            convListModel.addElement(new ConvItem(user, dn, false));
        }
        int idxToSelect = 0;
        if (selected != null) {
            for (int i = 0; i < convListModel.size(); i++) {
                if (convListModel.get(i).id.equals(selected.id)) { idxToSelect = i; break; }
            }
        }
        convList.setSelectedIndex(idxToSelect);
    }

    /** Tin nhắn không có timestamp (fallback). */
    public void appendMessage(String convId, String from, String text, boolean isSelf) {
        appendBlock(convId, from, new ChatBubble(text, isSelf), isSelf);
    }

    /**
     * Tin nhắn kèm timestamp — giờ hiện dưới bubble, divider ngày như Zalo.
     * @param sentAt "HH:mm" (real-time) hoặc "HH.mm dd/MM/yyyy" (lịch sử từ DB)
     */
    public void appendMessage(String convId, String from, String text, boolean isSelf, String sentAt) {
        String timeLabel = "";
        String dateLabel = "";
        if (sentAt != null && sentAt.contains(" ")) {
            // Format lịch sử từ DB: "13.27 27/10/2026"
            String[] parts = sentAt.split(" ", 2);
            timeLabel = parts[0].replace(".", ":");  // "13.27" → "13:27"
            dateLabel = parts[1];                     // "27/10/2026"
        } else if (sentAt != null) {
            timeLabel = sentAt;  // real-time chỉ có "13:27"
        }

        // Chèn divider ngày nếu sang ngày mới (như Zalo)
        if (!dateLabel.isEmpty()) {
            String lastDate = lastDatePerConv.get(convId);
            if (!dateLabel.equals(lastDate)) {
                lastDatePerConv.put(convId, dateLabel);
                appendDateDivider(convId, formatDateDivider(dateLabel));
            }
        }

        appendBlock(convId, from, new ChatBubble(text, isSelf, timeLabel), isSelf);
    }

    /** Tin nhắn hệ thống (join/leave/lỗi...) */
    public void appendSystem(String convId, String msg) {
        StyledDocument doc = conversationDocs.computeIfAbsent(convId, k -> new DefaultStyledDocument());
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, ChatColor.TEXT_DIM);
        StyleConstants.setFontSize(attr, 12);
        StyleConstants.setItalic(attr, true);
        try {
            insertParagraph(doc, msg, attr, attr, StyleConstants.ALIGN_CENTER);
            insertParagraph(doc, "", new SimpleAttributeSet(), new SimpleAttributeSet(), StyleConstants.ALIGN_CENTER);
        } catch (BadLocationException ignored) {}
        if (convId.equals(activeConversation)) chatArea.setCaretPosition(doc.getLength());
    }

    public void setDisconnected() {
        appendSystem(GROUP_ID, "⚠ Mất kết nối với server.");
        statusLabel.setForeground(ChatColor.DANGER);
        statusLabel.setToolTipText("Mất kết nối");
    }

    public void appendFileCard(String convId, String sender, String name, long size, File file, boolean isSelf) {
        appendBlock(convId, sender, new ChatFile(name, size, file, isSelf), isSelf);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Divider ngày ở giữa như Zalo: "Hôm nay", "Hôm qua", "27/10/2026" */
    private void appendDateDivider(String convId, String label) {
        StyledDocument doc = conversationDocs.computeIfAbsent(convId, k -> new DefaultStyledDocument());
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(150, 150, 150));
        StyleConstants.setFontSize(attr, 11);
        try {
            insertParagraph(doc, label, attr, attr, StyleConstants.ALIGN_CENTER);
            insertParagraph(doc, "", new SimpleAttributeSet(), new SimpleAttributeSet(), StyleConstants.ALIGN_CENTER);
        } catch (BadLocationException ignored) {}
    }

    /** Chuyển "dd/MM/yyyy" → "Hôm nay" / "Hôm qua" / nguyên chuỗi */
    private String formatDateDivider(String ddMMyyyy) {
        try {
            java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate date  = java.time.LocalDate.parse(ddMMyyyy, fmt);
            java.time.LocalDate today = java.time.LocalDate.now();
            if (date.equals(today))                return "Hôm nay";
            if (date.equals(today.minusDays(1)))   return "Hôm qua";
            return ddMMyyyy;
        } catch (Exception e) {
            return ddMMyyyy;
        }
    }

    private void insertParagraph(StyledDocument doc, String text, AttributeSet charAttr,
                                 AttributeSet endAttr, int alignment) throws BadLocationException {
        int start = doc.getLength();
        if (!text.isEmpty()) doc.insertString(doc.getLength(), text, charAttr);
        doc.insertString(doc.getLength(), "\n", endAttr);
        SimpleAttributeSet para = new SimpleAttributeSet();
        StyleConstants.setAlignment(para, alignment);
        doc.setParagraphAttributes(start, doc.getLength() - start, para, true);
    }

    private void appendBlock(String convId, String sender, JComponent content, boolean isSelf) {
        StyledDocument doc = conversationDocs.computeIfAbsent(convId, k -> new DefaultStyledDocument());
        int alignment = isSelf ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT;
        boolean showName = GROUP_ID.equals(convId) && !isSelf;

        try {
            if (showName) {
                SimpleAttributeSet nameAttr = new SimpleAttributeSet();
                StyleConstants.setForeground(nameAttr, ChatColor.NAME_OTHER);
                StyleConstants.setBold(nameAttr, true);
                StyleConstants.setFontSize(nameAttr, 12);
                insertParagraph(doc, sender, nameAttr, nameAttr, alignment);
            }
            content.setAlignmentX(isSelf ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            SimpleAttributeSet compAttr = new SimpleAttributeSet();
            StyleConstants.setComponent(compAttr, content);
            insertParagraph(doc, " ", compAttr, new SimpleAttributeSet(), alignment);
            insertParagraph(doc, "", new SimpleAttributeSet(), new SimpleAttributeSet(), alignment);
        } catch (BadLocationException ignored) {}

        afterInsert(convId, doc, isSelf);
    }

    private void afterInsert(String convId, StyledDocument doc, boolean isSelf) {
        if (convId.equals(activeConversation)) {
            chatArea.setCaretPosition(doc.getLength());
        } else if (!isSelf) {
            unreadCounts.merge(convId, 1, Integer::sum);
            convList.repaint();
        }
    }
}
