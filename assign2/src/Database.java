import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.net.Socket;

class Database {

    private File file;
    private String[] database;

    public Database(String filename) throws IOException {
        this.file = new File(filename);
        if (!file.exists()) {
            createEmptyFile();
        }

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append(",");
        }
        reader.close();
        this.database = sb.toString().split(",");

    }

    private void createEmptyFile() throws IOException {
        FileWriter writer = new FileWriter(this.file);
        writer.close();
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (String o : this.database) {
            String[] playerData = o.split(",");
            String username = playerData[0];
            String password = playerData[1];
            Float elo = Float.parseFloat(playerData[2]);
            String token = playerData[3];
            Player player = new Player(username, password, elo, token, null);
            players.add(player);
        }
        return players;
    }

    public void backup() throws IOException {
        String[] db = this.database;
        FileWriter writer = new FileWriter(this.file);
        for (String row : db) {
            String line = String.join(",", row) + "\n";
            writer.write(line);
        }
        writer.close();
    }


    public Player login(String username, String password, String token, Socket socket) {

        String[] db = this.database;
        for (String obj : db) {
            String[] playerData = obj.split(",");
            String storedUsername = playerData[0];
            String storedPassword = playerData[1];

            if (storedUsername.equals(username) && password.equals(storedPassword)) {
                Float elo = Float.parseFloat(playerData[2]);
                return new Player(username, storedPassword, elo, token, socket);
            }
        }

        return null;
    }

    public Player register(String username, String password, String token, Socket socket) {
        String[] db = this.database;
        if (isUsernameTaken(username, db)) {
            return null; 
        }

        Player newPlayer = createPlayer(username, password, token);
        String[] newDatabase = new String[this.database.length + 1];
        System.arraycopy(this.database, 0, newDatabase, 0, this.database.length);
        newDatabase[this.database.length] = playerToString(newPlayer);

        this.database = newDatabase;

        return newPlayer;
    }


    private String playerToString(Player player) {
        return player.getUsername() + "," + player.getPassword() + "," + player.getElo() + "," + player.getToken();
    }

    public Player reconnecting(String token, Socket socket) {
        String[] db = this.database;
        for (String obj : db) {
            String[] playerData = obj.split(",");
            String storedToken = playerData[3];

            if (storedToken.equals(token)) {
                String username = playerData[0];
                String password = playerData[1];
                Float elo = Float.parseFloat(playerData[2]);
                return new Player(username, password, elo, storedToken, socket);
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
        Player newPlayer = new Player(username, passwordHash, 1400F, token, null);
        return newPlayer;
    }

    public String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }


    public void updateElo(Player player, Integer value) {
        String[] db = this.database;
        for (String obj : db) {
            String[] playerData = obj.split(",");
            String username = playerData[0];
            if (username.equals(player.getUsername())) {

                Float elo = Float.parseFloat(playerData[2]) + value;
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
            Player newPlayer = new Player(playerData[0], playerData[1], Float.parseFloat(playerData[2]), playerData[3], null);
        }
    }

    public String[] getEloRanks() {
        String[] eloRanking = new String[5];
        String[] db = this.database;
        List<Player> userList = new ArrayList<>();
        for (String obj : db) {
            String[] playerData = obj.split(",");
            Player newPlayer = new Player(playerData[0], playerData[1], Float.parseFloat(playerData[2]), playerData[3], null);
            userList.add(newPlayer);
        
        userList.sort((a, b) -> Float.compare(b.getElo(), a.getElo()));
        for (int i = 0; i < 5 && i < userList.size(); i++) {
            Player user = userList.get(i);
            String username = user.getUsername();
            Float elo = user.getElo();
            eloRanking[i] = username + " - " + elo;
        }
       
        }
        return eloRanking;
    }
}