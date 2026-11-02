package com.mycompany.bai4.server;

import com.mycompany.bai4.model.UserStore;
import com.mycompany.bai4.model.MessageStore;
import com.mycompany.bai4.model.MessageStore.Message;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;



public class ChatServer {

    public static final int PORT = 9999;

    private final UserStore userStore = new UserStore();

    // danh sách người dùng
    private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        new ChatServer().start();
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("[Server] Đang lang nghe tren cong " + PORT);
        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }

    //  gửi cho tất cả                                                           
    private void broadcast(String line, String excludeUser) {
        for (Map.Entry<String, ClientHandler> e : clients.entrySet()) {
            if (!e.getKey().equals(excludeUser)) e.getValue().sendLine(line);
        }
    }

    private void sendTo(String username, String line) {
        ClientHandler h = clients.get(username);
        if (h != null) h.sendLine(line);
    }

    // cập nhật danh sách onl. 
    private void broadcastUserList() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ClientHandler> e : clients.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            String dn = userStore.getDisplayName(e.getKey());
            sb.append(dn).append("|").append(e.getKey());
        }
        String packet = "USERS:" + sb;
        for (ClientHandler h : clients.values()) h.sendLine(packet);
    }

    //  ClientHandler                                                      
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter    out;
        private String username;    // tên đăng nhập 
        private String displayName; // tên hiển thị

        ClientHandler(Socket socket) { this.socket = socket; }

        void sendLine(String line) { if (out != null) out.println(line); }

        @Override
        public void run() {
            try {
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),  "UTF-8"));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

                if (!authenticate()) { socket.close(); return; }

                // Chờ JOIN
                String joinLine = in.readLine();
                if (joinLine == null || !joinLine.startsWith("JOIN:")) { socket.close(); return; }
                if (!joinLine.substring(5).trim().equals(username))    { socket.close(); return; }

                // Xử lý trùng username (mở nhiều tab)
                String base = username; int suffix = 2;
                while (clients.containsKey(username)) username = base + "_" + suffix++;

                clients.put(username, this);
                System.out.printf("[Server] %s (%s) kết nối từ %s%n",
                        displayName, username, socket.getInetAddress());

                broadcast("SERVER:" + displayName + " đã tham gia phòng chat.", username);
                broadcastUserList();

                String line;
                while ((line = in.readLine()) != null) handleLine(line);

            } catch (IOException ignored) {
            } finally {
                cleanup();
            }
        }

        private boolean authenticate() throws IOException {
            String line = in.readLine();
            if (line == null) return false;

            if (line.startsWith("REGISTER:")) {
                // REGISTER:<username>:<password>:<displayName>
                String[] parts = line.substring(9).split(":", 3);
                if (parts.length < 3) { sendLine("AUTH_FAIL:Dữ liệu đăng ký không hợp lệ."); return false; }

                String user = parts[0].trim();
                String pwd  = parts[1];
                String dn   = parts[2].trim();

                if (user.isEmpty()) { sendLine("AUTH_FAIL:Tên đăng nhập không được để trống."); return false; }
                if (dn.isEmpty())   { sendLine("AUTH_FAIL:Tên người dùng không được để trống."); return false; }
                if (pwd.length() < 6) { sendLine("AUTH_FAIL:Mật khẩu phải có ít nhất 6 ký tự."); return false; }
                if (!user.matches("[\\w\\-\\.]{2,30}")) {
                    sendLine("AUTH_FAIL:Tên đăng nhập chỉ gồm chữ, số, _ - . (2-30 ký tự).");
                    return false;
                }
                if (!userStore.register(user, pwd, dn)) {
                    sendLine("AUTH_FAIL:Tên đăng nhập đã tồn tại.");
                    return false;
                }
                username    = user;
                displayName = dn;
                sendLine("AUTH_OK:" + username + ":" + displayName);
                System.out.println("[Server] Tài khoản mới: " + username + " / " + displayName);
                return true;

            } else if (line.startsWith("LOGIN:")) {
                // LOGIN:<username>:<password>
                String[] parts = line.substring(6).split(":", 2);
                if (parts.length < 2) { sendLine("AUTH_FAIL:Dữ liệu đăng nhập không hợp lệ."); return false; }

                String user = parts[0].trim();
                String pwd  = parts[1];

                if (!userStore.login(user, pwd)) {
                    sendLine("AUTH_FAIL:Sai tên đăng nhập hoặc mật khẩu.");
                    return false;
                }
                if (clients.containsKey(user)) {
                    sendLine("AUTH_FAIL:Tài khoản đang được sử dụng ở nơi khác.");
                    return false;
                }
                username    = user;
                displayName = userStore.getDisplayName(user);
                sendLine("AUTH_OK:" + username + ":" + displayName);
                System.out.println("[Server] Đăng nhập: " + username + " / " + displayName);
                return true;

            } else {
                sendLine("AUTH_FAIL:Vui lòng đăng nhập hoặc đăng ký.");
                return false;
            }
        }

        private void handleLine(String line) {
            if (line.startsWith("MSG:")) {
                // Client gửi: MSG:<username>:<text>
                String[] p = line.split(":", 3);
                if (p.length == 3) {
                    String text        = p[2];
                    String forwardLine = "MSG:" + username + ":" + displayName + ":" + text;
                    MessageStore.saveGroupMessage(username, displayName, text);
                    broadcast(forwardLine, username);
                }
            } else if (line.startsWith("PM:")) {
                // Client gửi: PM:<fromUsername>:<toUsername>:<text>
                String[] p = line.split(":", 4);
                if (p.length == 4) {
                    String toUser      = p[2];
                    String text        = p[3];
                    String forwardLine = "PM:" + username + ":" + displayName + ":" + text;
                    MessageStore.savePrivateMessage(username, displayName, toUser, text);
                    sendTo(toUser, forwardLine);
                }
            } else if (line.equals("GET_HISTORY")) {
                sendGroupHistory();
            } else if (line.startsWith("GET_PM_HISTORY:")) {
                // GET_PM_HISTORY:<otherUsername>
                String otherUser = line.substring(15).trim();
                sendPrivateHistory(otherUser);
            } else if (line.startsWith("FILE_META:") || line.startsWith("FILE_DATA:")) {
                routeFilePacket(line);
            }
        }

        /**
         * Gửi lịch sử tin nhắn nhóm cho user vừa đăng nhập.
         */
        private void sendGroupHistory() {
            List<Message> history = MessageStore.loadGroupHistory();
            if (history.isEmpty()) return;
            sendLine("HISTORY_START:GROUP");
            for (Message msg : history) {
                sendLine("HISTORY_MSG:GROUP:" + msg.fromUser() + ":" + msg.fromDisplay()
                        + ":" + msg.sentAt() + ":" + msg.content());
            }
            sendLine("HISTORY_END:GROUP");
        }

        /**
         * Gửi lịch sử tin nhắn riêng giữa user hiện tại và otherUser.
         */
        private void sendPrivateHistory(String otherUser) {
            List<Message> history = MessageStore.loadPrivateHistory(username, otherUser);
            if (history.isEmpty()) return;
            sendLine("HISTORY_START:" + otherUser);
            for (Message msg : history) {
                sendLine("HISTORY_MSG:" + otherUser + ":" + msg.fromUser() + ":" + msg.fromDisplay()
                        + ":" + msg.sentAt() + ":" + msg.content());
            }
            sendLine("HISTORY_END:" + otherUser);
        }

        private void routeFilePacket(String line) {
            String[] parts = line.split(":", 4);
            if (parts.length < 4) return;
            String to = parts[2];
            if ("ALL".equalsIgnoreCase(to)) broadcast(line, username);
            else sendTo(to, line);
        }

        private void cleanup() {
            if (username != null) {
                clients.remove(username);
                System.out.println("[Server] " + displayName + " (" + username + ") ngắt kết nối.");
                broadcast("SERVER:" + displayName + " đã rời phòng chat.", null);
                broadcast("LEAVE:" + username, null);
                broadcastUserList();
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}