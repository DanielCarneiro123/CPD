import java.io.BufferedReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.net.*;

public class Authentication implements Runnable {
    private Socket socket;
    private static final DatabaseConnection db = new DatabaseConnection();

    

    public Authentication(Socket socket) {
        this.socket = socket;
    }

    public static User authenticate(String username, String password) {
        User user = db.getUserByUsername(username);
        if (user == null)
            return null;

        if (user.getPasswordHash().equals(hashPassword(password))) {
            db.generateSessionToken(user);
            return user; 
        }

        return null;
    }

    public static User register(String username, String password) {
        if (db.userExists(username)) {
            return null;
        }
        String hashedPassword = hashPassword(password);

        User user = new User(username, hashedPassword, 1000, null, null);
        db.addUser(user);

        return user;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            User user = null;
            int attempts = 3;
            while (attempts-- > 0) {
                String action = reader.readLine();
                System.out.println(action);
                if ("login".equals(action)) {
                    String username = reader.readLine();
                    String password = reader.readLine();
                    user = authenticate(username, password);
                } else if ("register".equals(action)) {
                    String username = reader.readLine();
                    String password = reader.readLine();
                    user = register(username, password);
                } else if ("reconnect".equals(action)) {
                    String username = reader.readLine();
                    String token = db.getUserByUsername(username).getToken();
                    if (token.length() == 32) {
                        user = db.getUserByToken(token);
                        user.getSocket().close();
                        user.setSocket(socket);
                        writer.println("Reconnected");
                        GameServer.addToWaitingQueue(user);
                        return;
                    } else {
                        writer.println("Session expired");
                        continue;
                    }
                } else {
                    writer.println("Invalid action");
                }

                if (user != null) {
                    user.setSocket(socket);
                    writer.println("Authenticated");
                    GameServer.addToWaitingQueue(user);
                    return;
                } else {
                    writer.println("Invalid credentials");
                }
            }
            if (attempts == 0) {
                writer.println("Too many attempts");
                socket.close();
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
