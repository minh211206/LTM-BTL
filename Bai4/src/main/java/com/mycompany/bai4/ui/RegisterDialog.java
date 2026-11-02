package com.mycompany.bai4.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class RegisterDialog extends JDialog {

    // ── Result ────────────────────────────────────────────────────────────────
    public static class Result {
        public final String host, username, password, displayName;
        public final int    port;
        Result(String host, int port, String username, String password, String displayName) {
            this.host = host; this.port = port;
            this.username = username; this.password = password; this.displayName = displayName;
        }
    }

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG_TOP       = new Color(0x1565C0);
    private static final Color BG_BOT       = new Color(0x0D47A1);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color ACCENT       = new Color(0x1976D2);
    private static final Color ACCENT_HOV   = new Color(0x1565C0);
    private static final Color INPUT_BG     = new Color(0xF5F7FA);
    private static final Color INPUT_BORDER = new Color(0xCFD8DC);
    private static final Color TEXT_MAIN    = new Color(0x212121);
    private static final Color TEXT_LINK    = new Color(0x1976D2);
    private static final Color ERROR_BG     = new Color(0xFFEBEE);
    private static final Color ERROR_FG     = new Color(0xC62828);

    // ── Fields ────────────────────────────────────────────────────────────────
    private final JTextField     displayNameField = styledTextField();
    private final JTextField     userField        = styledTextField();
    private final JPasswordField passField        = styledPasswordField();
    private final JPasswordField pass2Field       = styledPasswordField();

    private final String host;
    private final int    port;

    private Result  result        = null;
    public  boolean openLoginFlag = false;
    private JLabel  errorLabel;

    // ── Constructor ───────────────────────────────────────────────────────────
    private RegisterDialog(String host, int port) {
        super((Frame) null, "Đăng ký tài khoản", true);
        this.host = host; this.port = port;
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
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // ── Header ──
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(32, 0, 20, 0));

        JLabel icon = new JLabel("👤");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Tạo tài khoản");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Điền thông tin để đăng ký");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 180));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(icon);
        header.add(Box.createVerticalStrut(8));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        // ── Card ──
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(24, 32, 24, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;

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

        gc.insets = new Insets(0, 0, 12, 0);
        gc.gridy = 1; card.add(fieldBlock("Tên hiển thị",      displayNameField), gc);
        gc.gridy = 2; card.add(fieldBlock("Tên đăng nhập",     userField),        gc);
        gc.gridy = 3; card.add(fieldBlock("Mật khẩu",          passField),        gc);
        gc.gridy = 4; card.add(fieldBlock("Xác nhận mật khẩu", pass2Field),       gc);

        JButton btnReg = accentButton("Tạo tài khoản");
        gc.gridy = 5; gc.insets = new Insets(8, 0, 8, 0);
        card.add(btnReg, gc);

        JButton btnLogin = linkButton("Đã có tài khoản? Đăng nhập");
        gc.gridy = 6; gc.insets = new Insets(0, 0, 0, 0);
        card.add(btnLogin, gc);

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
        displayNameField.addActionListener(e -> userField.requestFocus());
        userField.addActionListener(e -> passField.requestFocus());
        passField.addActionListener(e -> pass2Field.requestFocus());
        pass2Field.addActionListener(e -> onRegister());
        btnReg.addActionListener(e -> onRegister());
        btnLogin.addActionListener(e -> { openLoginFlag = true; dispose(); });

        makeDraggable(root);
    }

    private void onRegister() {
        String dn   = displayNameField.getText().trim();
        String user = userField.getText().trim();
        String pwd  = new String(passField.getPassword());
        String pwd2 = new String(pass2Field.getPassword());

        if (dn.isEmpty())       { showError("Vui lòng nhập tên hiển thị."); return; }
        if (user.isEmpty())     { showError("Vui lòng nhập tên đăng nhập."); return; }
        if (pwd.length() < 6)   { showError("Mật khẩu phải có ít nhất 6 ký tự."); return; }
        if (!pwd.equals(pwd2))  { showError("Mật khẩu xác nhận không khớp."); pass2Field.setText(""); pass2Field.requestFocus(); return; }

        result = new Result(host, port, user, pwd, dn);
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

    private static JTextField styledTextField() {
        JTextField f = new JTextField(20); f.setOpaque(false); return f;
    }

    private static JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField(20); f.setOpaque(false); return f;
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
    public static RegisterDialog showAndGet(String host, int port) {
        RegisterDialog dlg = new RegisterDialog(host, port);
        dlg.setVisible(true);
        return dlg;
    }

    public Result getResult() { return result; }
}
