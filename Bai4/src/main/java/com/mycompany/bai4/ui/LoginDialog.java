package com.mycompany.bai4.ui;

import com.mycompany.bai4.server.ChatServer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginDialog extends JDialog {

    // ── Result ────────────────────────────────────────────────────────────────
    public static class Result {
        public final String host;
        public final int    port;
        public final String username;
        public final String password;

        Result(String host, int port, String username, String password) {
            this.host = host; this.port = port;
            this.username = username; this.password = password;
        }
    }

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG_TOP      = new Color(0x1565C0);
    private static final Color BG_BOT      = new Color(0x0D47A1);
    private static final Color CARD_BG     = Color.WHITE;
    private static final Color ACCENT      = new Color(0x1976D2);
    private static final Color ACCENT_HOV  = new Color(0x1565C0);
    private static final Color INPUT_BG    = new Color(0xF5F7FA);
    private static final Color INPUT_BORDER= new Color(0xCFD8DC);
    private static final Color TEXT_MAIN   = new Color(0x212121);
    private static final Color TEXT_DIM    = new Color(0x757575);
    private static final Color TEXT_LINK   = new Color(0x1976D2);
    private static final Color ERROR_BG    = new Color(0xFFEBEE);
    private static final Color ERROR_FG    = new Color(0xC62828);

    // ── Fields ────────────────────────────────────────────────────────────────
    private final JTextField     hostField;
    private final JTextField     userField;
    private final JPasswordField passField;

    private Result  result          = null;
    public  boolean openRegisterFlag = false;

    private JLabel errorLabel;

    // ── Constructor ───────────────────────────────────────────────────────────
    private LoginDialog(String initHost, String prefillUser) {
        super((Frame) null, "Đăng nhập", true);

        hostField = styledTextField(initHost);
        userField = styledTextField(prefillUser);
        passField = styledPasswordField();

        buildUI();
        setUndecorated(true);
        pack();
        setMinimumSize(new Dimension(420, getHeight()));
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    private void buildUI() {
        // Nền gradient
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // Header (logo + tiêu đề)
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(36, 0, 24, 0));

        JLabel iconLbl = new JLabel("💬");
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel("Chat App");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Đăng nhập để tiếp tục");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(new Color(255, 255, 255, 180));
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(iconLbl);
        header.add(Box.createVerticalStrut(8));
        header.add(titleLbl);
        header.add(Box.createVerticalStrut(4));
        header.add(subLbl);

        // Card trắng
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(28, 32, 24, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1; gc.insets = new Insets(0, 0, 0, 0);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_FG);
        errorLabel.setOpaque(true);
        errorLabel.setBackground(ERROR_BG);
        errorLabel.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xEF9A9A), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        errorLabel.setVisible(false);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 10, 0);
        card.add(errorLabel, gc);

        gc.insets = new Insets(0, 0, 14, 0);
        gc.gridy = 1; card.add(fieldBlock("Tên đăng nhập", userField), gc);
        gc.gridy = 2; card.add(fieldBlock("Mật khẩu", passField), gc);
        gc.gridy = 3; card.add(fieldBlock("Địa chỉ IP Server", hostField), gc);

        // Nút đăng nhập
        JButton btnLogin = accentButton("Đăng nhập");
        gc.gridy = 4; gc.insets = new Insets(8, 0, 10, 0);
        card.add(btnLogin, gc);

        // Link đăng ký
        JButton btnRegister = linkButton("Chưa có tài khoản? Đăng ký ngay");
        gc.gridy = 5; gc.insets = new Insets(0, 0, 0, 0);
        card.add(btnRegister, gc);

        // Card bo góc
        JPanel cardRounded = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        cardRounded.setOpaque(false);
        cardRounded.add(card, BorderLayout.CENTER);

        JPanel cardWrap = new JPanel(new BorderLayout());
        cardWrap.setOpaque(false);
        cardWrap.setBorder(new EmptyBorder(0, 24, 32, 24));
        cardWrap.add(cardRounded, BorderLayout.CENTER);

        root.add(header,   BorderLayout.NORTH);
        root.add(cardWrap, BorderLayout.CENTER);
        setContentPane(root);

        // Actions
        userField.addActionListener(e -> passField.requestFocus());
        passField.addActionListener(e -> hostField.requestFocus());
        hostField.addActionListener(e -> onLogin());
        btnLogin.addActionListener(e -> onLogin());
        btnRegister.addActionListener(e -> { openRegisterFlag = true; dispose(); });

        makeDraggable(root);
    }

    private void onLogin() {
        String host = hostField.getText().trim();
        String user = userField.getText().trim();
        String pwd  = new String(passField.getPassword());

        if (host.isEmpty()) { showError("Vui lòng nhập địa chỉ IP server."); return; }
        if (user.isEmpty()) { showError("Vui lòng nhập tên đăng nhập."); return; }
        if (pwd.isEmpty())  { showError("Vui lòng nhập mật khẩu."); return; }

        result = new Result(host, ChatServer.PORT, user, pwd);
        dispose();
    }

    private void showError(String msg) {
        errorLabel.setText("  ⚠  " + msg);
        errorLabel.setVisible(true);
        pack();
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
        setLocationRelativeTo(null);
    }

    // ── Widget helpers ────────────────────────────────────────────────────────

    private JPanel fieldBlock(String labelText, JComponent field) {
        JPanel block = new JPanel(new BorderLayout(0, 5));
        block.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_MAIN);
        block.add(lbl, BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.setColor(INPUT_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(8, 12, 8, 12));
        wrap.setPreferredSize(new Dimension(0, 44));

        field.setOpaque(false);
        field.setBorder(null);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_MAIN);
        if (field instanceof JTextField tf) tf.setCaretColor(ACCENT);
        wrap.add(field, BorderLayout.CENTER);
        block.add(wrap, BorderLayout.CENTER);
        return block;
    }

    private JButton accentButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOV : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 44));
        return btn;
    }

    private JButton linkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(TEXT_LINK);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField styledTextField(String text) {
        JTextField f = new JTextField(text, 20);
        f.setOpaque(false);
        return f;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField(20);
        f.setOpaque(false);
        return f;
    }

    private void makeDraggable(JPanel panel) {
        final int[] drag = new int[2];
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { drag[0] = e.getX(); drag[1] = e.getY(); }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                setLocation(getX() + e.getX() - drag[0], getY() + e.getY() - drag[1]);
            }
        });
    }

    // ── Static factory ────────────────────────────────────────────────────────
    public static LoginDialog showAndGet(String host, int port, String prefillUser) {
        LoginDialog dlg = new LoginDialog(host, prefillUser);
        dlg.setVisible(true);
        return dlg;
    }

    public Result getResult() { return result; }
}
