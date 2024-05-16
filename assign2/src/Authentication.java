import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Authentication {
    private static final String DATABASE_FILE = "database.csv";
    private static List<User> users;
    private static final Lock authenticationLock = new ReentrantLock();

    private static final SecureRandom secureRandom = new SecureRandom(); 
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); 

    static {
        users = new ArrayList<>();
        loadUsersFromCSV();
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
}

    

