package com.mycompany.bai4.client;

import com.mycompany.bai4.ui.ChatUI;
import com.mycompany.bai4.ui.LoginDialog;
import com.mycompany.bai4.ui.RegisterDialog;
import com.mycompany.bai4.server.ChatServer;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.Base64;


public class ChatClient {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new ChatClient().launch();
        });
    }

    //  Network                                                             
    private Socket         socket;
    private BufferedReader netIn;
    private PrintWriter    netOut;
    
    private String username;
    private String displayName;

    //  UI                                                                  
    private ChatUI window;

    // username → displayName
    private final Map<String, String> userDisplayMap = new HashMap<>();

    //  Misc                                                                
    private final File downloadDir;

    // thư mục lưu file
    public ChatClient() {
        downloadDir = new File(System.getProperty("user.home"),
                "Downloads" + File.separator + "ChatFiles");
        downloadDir.mkdirs();
    }

    //  Luồng khởi động                                                   
    public void launch() {
        showLogin("localhost", ChatServer.PORT, "");
    }

    // Màn hình Đăng nhập
    private void showLogin(String host, int port, String prefillUser) {
        LoginDialog dlg = LoginDialog.showAndGet(host, port, prefillUser);

        if (dlg.openRegisterFlag) {
            String lastHost = (host != null && !host.isEmpty()) ? host : "localhost";
            showRegister(lastHost, ChatServer.PORT);
            return;
        }

        LoginDialog.Result r = dlg.getResult();
        if (r == null) return; // Hủy → thoát

        try {
            openSocket(r.host, r.port);
            String err = doAuth(false, r.username, r.password, null);
            if (err != null) {
                closeSocket();
                JOptionPane.showMessageDialog(null, err, "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
                showLogin(r.host, r.port, r.username);
                return;
            }
            netOut.println("JOIN:" + username);
            window = new ChatUI(this, username, displayName, r.host);
            window.setVisible(true);
            startReceiver();
            // Yêu cầu lịch sử sau khi receiver đã sẵn sàng
            sendLine("GET_HISTORY");

        } catch (IOException ex) {
            closeSocket();
            JOptionPane.showMessageDialog(null,
                    "Không thể kết nối tới " + r.host + ":" + r.port + "\n" + ex.getMessage(),
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            showLogin(r.host, r.port, r.username);
        }
    }

    // Màn hình Đăng ký
    private void showRegister(String host, int port) {
        RegisterDialog dlg = RegisterDialog.showAndGet(host, port);

        if (dlg.openLoginFlag) {
            showLogin(host, port, "");
            return;
        }

        RegisterDialog.Result r = dlg.getResult();
        if (r == null) return; // Hủy → thoát

        try {
            openSocket(r.host, r.port);
            String err = doAuth(true, r.username, r.password, r.displayName);
            if (err != null) {
                closeSocket();
                JOptionPane.showMessageDialog(null, err, "Đăng ký thất bại", JOptionPane.ERROR_MESSAGE);
                showRegister(r.host, r.port);
                return;
            }
            closeSocket();
            JOptionPane.showMessageDialog(null,
                    "✅ Đăng ký tài khoản \"" + r.username + "\" thành công!\nVui lòng đăng nhập để vào phòng chat.",
                    "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);
            showLogin(r.host, r.port, r.username);

        } catch (IOException ex) {
            closeSocket();
            JOptionPane.showMessageDialog(null,
                    "Không thể kết nối tới " + r.host + ":" + r.port + "\n" + ex.getMessage(),
                    "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            showRegister(r.host, r.port);
        }
    }

    //  Network helpers                                                     
    private void openSocket(String host, int port) throws IOException {
        socket = new Socket(host, port);
        netIn  = new BufferedReader(new InputStreamReader(socket.getInputStream(),  "UTF-8"));
        netOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
    }

    private void closeSocket() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        socket = null; netIn = null; netOut = null;
    }

    // Gửi REGISTER hoặc LOGIN, đọc AUTH_OK / AUTH_FAIL
    
    private String doAuth(boolean isRegister, String user, String pwd, String dn) throws IOException {
        if (isRegister) {
            netOut.println("REGISTER:" + user + ":" + pwd + ":" + dn);
        } else {
            netOut.println("LOGIN:" + user + ":" + pwd);
        }
        String resp = netIn.readLine();
        if (resp == null) return "Server đóng kết nối bất ngờ.";
        if (resp.startsWith("AUTH_OK:")) {
            String[] parts = resp.substring(8).split(":", 2);
            username    = parts[0].trim();
            displayName = parts.length > 1 ? parts[1].trim() : username;
            return null;
        }
        if (resp.startsWith("AUTH_FAIL:")) return resp.substring(10);
        return "Phản hồi không hợp lệ: " + resp;
    }

    private void sendLine(String line) {
        if (netOut != null) netOut.println(line);
    }

    public void closeConnection() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

                         

    //Gửi tin nhắn nhóm
    public void sendMessage(String text) {
        // Gửi: MSG:<username>:<text>  (server sẽ ghép thêm displayName khi forward)
        sendLine("MSG:" + username + ":" + text);
    }

    // Gửi tin nhắn riêng 
    public void sendPrivateMessage(String toUsername, String text) {
        // Gửi: PM:<fromUsername>:<toUsername>:<text>
        sendLine("PM:" + username + ":" + toUsername + ":" + text);
    }

    /** Yêu cầu server gửi lịch sử PM với một user cụ thể */
    public void requestPrivateHistory(String withUsername) {
        sendLine("GET_PM_HISTORY:" + withUsername);
    }

    // gửi file
    public void sendFile(File file, String toUsername) {
        new Thread(() -> doSendFile(file, toUsername)).start();
    }

    private void doSendFile(File file, String toUsername) {
        // Quy về đúng "hội thoại" mà file card sẽ hiện lên phía người gửi
        String convId = "ALL".equalsIgnoreCase(toUsername) ? ChatUI.GROUP_ID : toUsername;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String b64   = Base64.getEncoder().encodeToString(bytes);
            sendLine("FILE_META:" + username + ":" + toUsername + ":" + file.getName() + ":" + file.length());
            sendLine("FILE_DATA:" + username + ":" + toUsername + ":" + file.getName() + ":" + b64);
            SwingUtilities.invokeLater(() ->
                    window.appendFileCard(convId, "Bạn", file.getName(), file.length(), file, true));
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() ->
                    window.appendSystem(ChatUI.GROUP_ID, "❌ Lỗi gửi file: " + ex.getMessage()));
        }
    }

    //  Receiver thread                                                   
    private void startReceiver() {
        Thread t = new Thread(() -> {
            try {
                String line;
                while ((line = netIn.readLine()) != null) handleIncoming(line);
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> { if (window != null) window.setDisconnected(); });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void handleIncoming(String line) {
        if (line.startsWith("MSG:")) {
            // MSG:<username>:<displayName>:<text>  → tin nhắn nhóm
            String[] p = line.split(":", 4);
            if (p.length == 4) {
                String fromUser = p[1];
                String fromDN   = p[2];
                String text     = p[3];
                boolean isSelf  = fromUser.equals(username);
                String now      = java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                SwingUtilities.invokeLater(() ->
                        window.appendMessage(ChatUI.GROUP_ID, isSelf ? displayName : fromDN, text, isSelf, now));
            }

        } else if (line.startsWith("PM:")) {
            // PM:<fromUsername>:<fromDisplayName>:<text>  → tin nhắn riêng nhận được
            String[] p = line.split(":", 4);
            if (p.length == 4) {
                String fromUser = p[1];
                String fromDN   = p[2];
                String text     = p[3];
                String now      = java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
                SwingUtilities.invokeLater(() ->
                        window.appendMessage(fromUser, fromDN, text, false, now));
            }

        } else if (line.startsWith("SERVER:")) {
            String msg = line.substring(7);
            SwingUtilities.invokeLater(() -> window.appendSystem(ChatUI.GROUP_ID, "ℹ️ " + msg));

        } else if (line.startsWith("HISTORY_START:")) {
            // không cần xử lý, chờ HISTORY_MSG

        } else if (line.startsWith("HISTORY_MSG:")) {
            // Nhóm:   HISTORY_MSG:GROUP:<fromUser>:<fromDisplay>:<sentAt>:<content>
            // PM:     HISTORY_MSG:<otherUser>:<fromUser>:<fromDisplay>:<sentAt>:<content>
            String[] p = line.split(":", 6);
            if (p.length == 6) {
                String convKey  = p[1];
                String fromUser = p[2];
                String fromDN   = p[3];
                String sentAt   = p[4]; // "HH:mm dd/MM/yyyy"
                String text     = p[5];
                boolean isSelf  = fromUser.equals(username);
                String convId   = "GROUP".equals(convKey) ? ChatUI.GROUP_ID : convKey;
                String label    = isSelf ? displayName : fromDN;
                SwingUtilities.invokeLater(() ->
                        window.appendMessage(convId, label, text, isSelf, sentAt));
            }

        } else if (line.startsWith("HISTORY_END:")) {
            String convKey = line.substring(12);
            String convId  = "GROUP".equals(convKey) ? ChatUI.GROUP_ID : convKey;
            SwingUtilities.invokeLater(() ->
                    window.appendSystem(convId, "── Lịch sử tin nhắn ──"));

        } else if (line.startsWith("USERS:")) {
            // USERS:<dn>|<user>,<dn>|<user>,...
            String payload = line.substring(6);
            String[] entries = payload.isEmpty() ? new String[0] : payload.split(",");
            // Giữ cả displayName lẫn username – cần username thật để định tuyến nhắn riêng
            List<String[]> pairs = new ArrayList<>(); // mỗi phần tử: {displayName, username}
            for (String entry : entries) {
                String[] parts = entry.split("\\|", 2);
                if (parts.length == 2) pairs.add(new String[]{parts[0], parts[1]});
            }
            userDisplayMap.clear();
            for (String[] pair : pairs) userDisplayMap.put(pair[1], pair[0]);
            SwingUtilities.invokeLater(() -> window.updateUserList(pairs));

        } else if (line.startsWith("FILE_DATA:")) {
            // FILE_DATA:<fromUsername>:<toUsername>:<filename>:<base64>
            String[] p = line.split(":", 5);
            if (p.length == 5) {
                String fromUser = p[1];
                String toUser   = p[2];
                String convId   = "ALL".equalsIgnoreCase(toUser) ? ChatUI.GROUP_ID : fromUser;
                receiveFile(convId, fromUser, p[3], p[4]);
            }
        }
    }

    //  Nhận & lưu file                                                    
    private void receiveFile(String convId, String fromUser, String name, String b64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(b64);
            File   out   = resolveUniqueFile(downloadDir, name);
            Files.write(out.toPath(), bytes);
            String fromDN = userDisplayMap.getOrDefault(fromUser, fromUser);
            SwingUtilities.invokeLater(() ->
                    window.appendFileCard(convId, fromDN, out.getName(), out.length(), out, false));
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() ->
                    window.appendSystem(ChatUI.GROUP_ID, "❌ Lỗi lưu file: " + ex.getMessage()));
        }
    }

    // xử lý trùng tên file
    private File resolveUniqueFile(File dir, String name) {
        File f = new File(dir, name);
        if (!f.exists()) return f;
        String base = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
        String ext  = name.contains(".") ? name.substring(name.lastIndexOf('.'))    : "";
        int i = 1;
        while (f.exists()) f = new File(dir, base + "_" + i++ + ext);
        return f;
    }
}