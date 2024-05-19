import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.io.*;
import java.net.*;

public class Authentication implements Runnable {
    private static final String DATABASE_FILE = "database.csv";
    private static List<User> users;
    private static final Lock authenticationLock = new ReentrantLock();

    private Socket socket;
    private static final SecureRandom secureRandom = new SecureRandom(); 
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); 

    static {
        users = new ArrayList<>();
        loadUsersFromCSV();
    }

    public Authentication(Socket socket) {
        this.socket = socket;
    }

    private static void loadUsersFromCSV() {
        authenticationLock.lock(); 
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(DATABASE_FILE))) {
                br.readLine();

                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    users.add(new User(data[0], data[1], Integer.parseInt(data[2]), data[3], null));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            authenticationLock.unlock(); 
        }
    }

    public static boolean authenticate(String username, String password) {
        authenticationLock.lock();
        try {
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    String hashedPassword = hashPassword(password);
                    if (user.getPasswordHash().equals(hashedPassword)) {
                        String token = generateToken();
                        user.setToken(token);
                        return true;
                    }
                }
            }
            return false;
        } finally {
            authenticationLock.unlock();
        }
    }

    public static boolean register(String username, String password) {
        authenticationLock.lock();
        try {
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    return false; 
                }
            }
            String hashedPassword = hashPassword(password);
            User newUser = new User(username, hashedPassword, 1400, null, null);
            users.add(newUser);
            saveUserToCSV(newUser);
            return true;
        } finally {
            authenticationLock.unlock();
        }
    }

    private static void saveUserToCSV(User user) {
        try (FileWriter writer = new FileWriter(DATABASE_FILE, true)) {
            writer.write(user.getUsername() + "," + user.getPasswordHash() + "," + user.getElo() + "," + ""+ "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static String generateToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public static List<User> getUsers() {
        return users;
    }

    public static User getUserByUsername(String username) {
        for (User user : getUsers()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static User getUserByToken(String token) {
        authenticationLock.lock();
        try {
            for (User user : users) {
                if (user.getToken().equals(token)) {
                    return user;
                }
            }
            return null;
        } finally {
            authenticationLock.unlock();
        }
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            User user = null;
            int attempts = 3;
            while(attempts-- > 0){
                String action = reader.readLine();
                System.out.println(action);
                if ("login".equals(action)) {
                    String username = reader.readLine();
                    String password = reader.readLine();
                    user = processAuthentication(username, password);
                } else if ("register".equals(action)) {
                    String username = reader.readLine();
                    String password = reader.readLine();
                    if (register(username, password)) {
                        user = getUserByUsername(username);
                    }
                } else if ("reconnect".equals(action)) {
                    String username = reader.readLine();
                    String token = getUserByUsername(username).getToken();
                    if (token.length() == 32) {
                        user = getUserByToken(token);
                        user.getSocket().close();
                        user.setSocket(socket);
                        writer.println("Reconnected");
                        GameServer.addToWaitingQueue(user);
                        return;
                    }
                    else {
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
                if(attempts == 0){
                    writer.println("Too many attempts");
                    socket.close();
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private static User processAuthentication(String username, String password) {
        authenticationLock.lock();
        try {
            if (authenticate(username, password)) {
                return getUserByUsername(username);
            } else {
                return null;
            }
        } finally {
            authenticationLock.unlock();
        }
    }
}
