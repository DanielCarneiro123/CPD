import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.xml.crypto;

class Database {

    private final File file;
    private final StringBuilder database;

    public Database(String filename) throws IOException, ParseException {

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
            String storedUsername = (String) obj.get(1);
            String storedPassword = (String) obj.get(2);

            if (storedUsername.equals(username) && BCrypt.checkpw(password, storedPassword)) {
                user.put("token", token);
                Long elo = ((Number) user.get("elo")).longValue();
                return new Player(username, storedPassword, token, elo, socket);
            }
        }

        return null;
    }

    public Player register(String username, String password, String token, SocketChannel socket) {
        JSONArray dbArray = (JSONArray) this.database.get("database");
        if (isUsernameTaken(username, dbArray)) {
            return null; 
        }

        JSONObject newPlayer = createPlayer(username, password, token);
        dbArray.add(newPlayer);
        this.database.put("database", dbArray);

        return createPlayerObject(username, password, token, 1400, socket);
    }

    public Player reconnecting(String token, SocketChannel socket) {
        JSONArray dbArray = (JSONArray) this.database.get("database");
        for (Object obj : dbArray) {
            JSONObject user = (JSONObject) obj;
            String storedToken = (String) user.get("token");

            if (storedToken.equals(token)) {
                return createPlayerObjectFromJson(user, socket);
            }
        }
        return null; 
    }

    private boolean isUsernameTaken(String username, JSONArray dbArray) {
        for (Object obj : dbArray) {
            JSONObject user = (JSONObject) obj;
            String storedUsername = (String) user.get("username");
            if (storedUsername.equals(username)) {
                return true;
            }
        }
        return false;
    }

    private JSONObject createPlayer(String username, String password, String token) {
        JSONObject newPlayer = new JSONObject();
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        newPlayer.put("username", username);
        newPlayer.put("password", passwordHash);
        newPlayer.put("token", token);
        newPlayer.put("elo", 1400);
        return newPlayer;
    }

    private Player createPlayerObject(String username, String password, String token, int elo, SocketChannel socket) {
        return new Player(username, password, token, elo, socket);
    }

    private Player createPlayerObjectFromJson(JSONObject user, SocketChannel socket) {
        String username = (String) user.get("username");
        String password = (String) user.get("password");
        Long elo = ((Number) user.get("elo")).longValue();
        String token = (String) user.get("token");
        return new Player(username, password, token, elo, socket);
    }


    public void updateElo(Player player, int value) {
        JSONArray dbArray = (JSONArray) this.database.get("database");
        for (Object obj : dbArray) {
            JSONObject user = (JSONObject) obj;
            String username = (String) user.get("username");
            if (username.equals(player.getUsername())) {

                Long elo = ((Number) user.get("elo")).longValue() + value;
                user.put("elo", elo);
                return;
            }
        }
    }

    public void invalidToken(Player player) {
        JSONArray dbArray = (JSONArray) this.database.get("database");
        for (Object obj : dbArray) {
            JSONObject user = (JSONObject) obj;
            String username = (String) user.get("username");

            if (username.equals(player.getUsername())) {
                user.put("token", "");
                return;
            }
        }
    }

    public void resetTokens() {
        JSONArray dbArray = (JSONArray) this.database.get("database");
        for (Object obj : dbArray) {
            JSONObject user = (JSONObject) obj;
            user.put("token", "");
        }
    }

    public String[] getEloRanks() {
        String[] eloRanking = new String[5];
        JSONArray dbArray = (JSONArray) this.database.get("database");
        List<JSONObject> userList = new ArrayList<>();
        for (Object obj : dbArray) {
            JSONObject user = (JSONObject) obj;
            userList.add(user);
        }
        userList.sort((a, b) -> Long.compare(((Number) b.get("elo")).longValue(), ((Number) a.get("elo")).longValue()));
        for (int i = 0; i < 5 && i < userList.size(); i++) {
            JSONObject user = userList.get(i);
            String username = (String) user.get("username");
            long elo = ((Number) user.get("elo")).longValue();
            eloRanking[i] = username + " - " + rank;
        }
        return eloRanking;
    }
}