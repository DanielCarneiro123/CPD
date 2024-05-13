import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;


class Database {

    private final File file;
    private final String[] database;

    public Database(String filename) throws IOException {

        this.file = new File(filename);
        if (!file.exists()) {
            createEmptyFile();
        }

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line.split(","));
        }
        reader.close();
        this.database = sb.toString();
    }

    private void createEmptyFile() throws IOException {
        FileWriter writer = new FileWriter(this.file);
        writer.close();
    }

    public void backup() throws IOException {
        FileWriter writer = new FileWriter(this.file);
        writer.write(this.database);
        writer.close();
    }

    public Player login(String username, String password, String token, SocketChannel socket) {

        String[] db = this.database;
        for (String obj : db) {
            String[] playerData = obj.split(",");
            String storedUsername = playerData[0];
            String storedPassword = playerData[1];

            if (storedUsername.equals(username) && password.equals(storedPassword)) {
                Float elo = Float.parseFloat(playerData[2]);
                return new Player(username, storedPassword, token, elo, socket);
            }
        }

        return null;
    }

    public Player register(String username, String password, String token, SocketChannel socket) {
        String[] db = this.database;
        if (isUsernameTaken(username, db)) {
            return null; 
        }

        Player newPlayer = createPlayer(username, password, token);
        db.add(newPlayer);

        return newPlayer;
    }

    public Player reconnecting(String token, SocketChannel socket) {
        String[] db = this.database;
        for (String obj : db) {
            String[] playerData = obj.split(",");
            String storedToken = playerData[3];

            if (storedToken.equals(token)) {
                String username = playerData[0];
                String password = playerData[1];
                Float elo = Float.parseFloat(playerData[2]);
                return new Player(username, password, storedToken, elo, socket);
            }
        }
        return null; 
    }

    private boolean isUsernameTaken(String username, String[] dbArray) {
        for (String obj : dbArray) {
            String[] playerData = obj.split(",");
            String storedUsername = playerData[0];
            if (storedUsername.equals(username)) {
                return true;
            }
        }
        return false;
    }

    private Player createPlayer(String username, String password, String token) {
        
        String passwordHash = generatePassword(12);
        Player newPlayer = new Player(username, passwordHash, token, 1400F, null);
        return newPlayer;
    }

    public String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }


    public void updateElo(Player player, Float value) {
        String[] db = this.database;
        for (String obj : db) {
            String[] playerData = obj.split(",");
            String username = playerData[0];
            if (username.equals(player.getUsername())) {

                Float elo = Float.parseFloat(playerData[3]) + value;
                player.setElo(elo);
                return;
            }
        }
    }

    public void invalidToken(Player player) {
        String[] db = this.database;
        for (String obj : db) {
            String[] playerData = obj.split(",");
            String username = playerData[0];

            if (username.equals(player.getUsername())) {
                player.setToken("");
                return;
            }
        }
    }


    public void resetTokens() {
        String[] db = this.database;
        for (String obj : db) {
            String[] playerData = obj.split(",");
            Player newPlayer = new Player(playerData[0], playerData[1], playerData[2], Float.parseFloat(playerData[3]), null);
        }
    }

    public String[] getEloRanks() {
        String[] eloRanking = new String[5];
        String[] db = this.database;
        List<Player> userList = new ArrayList<>();
        for (String obj : db) {
            String[] playerData = obj.split(",");
            Player newPlayer = new Player(playerData[0], playerData[1], playerData[2], Float.parseFloat(playerData[3]), null);
            userList.add(newPlayer);
        
        userList.sort((a, b) -> Float.compare(b.getElo(), a.getElo()));
        for (int i = 0; i < 5 && i < userList.size(); i++) {
            Player user = userList.get(i);
            String username = user.getUsername();
            float elo = user.getElo();
            eloRanking[i] = username + " - " + elo;
        }
        return eloRanking;
        }
    }
}