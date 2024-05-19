import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseConnection {
    private static final String DATABASE_FILE = "database.csv";
    private static List<User> users;
    private static final Lock databaseLock = new ReentrantLock();

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    static {
        users = new ArrayList<>();
        loadAllUsersFromCSV();
    }

    public DatabaseConnection() {
    }

    private static void loadAllUsersFromCSV() {
        databaseLock.lock();
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
            databaseLock.unlock();
        }
    }

    private static void addUserToCSV(User user) {
        try (FileWriter writer = new FileWriter(DATABASE_FILE, true)) {
            writer.write("\n" + user.getUsername() + "," + user.getPasswordHash() + "," + user.getElo() + ","
                    + user.getToken() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generateToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public void updateDB() {
        databaseLock.lock();
        try {
            try (FileWriter writer = new FileWriter(DATABASE_FILE)) {
                writer.write("username,password,elo,token\n");
                for (User user : users) {
                    writer.write(user.getUsername() + "," + user.getPasswordHash() + "," + user.getElo() + ","
                            + user.getToken() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            databaseLock.unlock();
        }
    }

    public boolean userExists(String username) {
        databaseLock.lock();
        try {
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    return true;
                }
            }
            return false;
        } finally {
            databaseLock.unlock();
        }
    }

    public User getUserByUsername(String username) {
        databaseLock.lock();
        try {
            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    return user;
                }
            }
            return null;
        } finally {
            databaseLock.unlock();
        }
    }

    public User getUserByToken(String token) {
        databaseLock.lock();
        try {
            for (User user : users) {
                if (user.getToken().equals(token)) {
                    return user;
                }
            }
            return null;
        } finally {
            databaseLock.lock();
        }
    }

    public void generateSessionToken(User user) {
        databaseLock.lock();
        try {
            user.setToken(generateToken());
        } finally {
            databaseLock.unlock();
        }
    }

    public void addUser(User user) {
        databaseLock.lock();
        try {
            users.add(user);
            addUserToCSV(user);
        } finally {
            databaseLock.unlock();
        }
    }

    public List<User> getUsers() {
        return users;
    }
}
