import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;

class Authentication {
    private List<Player> players;
    
    public Authentication() throws IOException {
        players = new ArrayList<>();
        Database database = new Database("/database.csv");
        players = database.getPlayers();
    }


    public Player getPlayer(String username){
         for (Player player : players) {
            if (player.getUsername().equals(username)) {
                return player;
            }
        }
        return null;
    }

    public Player login(String username, String password){
        var player = getPlayer(username);
        if(username == null) {
            return null;
        }
        var pass = encryptPass(password);
        if(!player.getPassword().equals(pass)){
            return null;
        }
        return player;
    }

    public String encryptPass(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("UTF-8");
            byte[] hashedBytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String password){
        Player player = getPlayer(username);
        if(player != null){
            return false;
        }
        String pass = encryptPass(password);
        try {
            Player newPlayer = database.createPlayer(username,pass);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        players.add(newPlayer);
        return true;
    }

    public String createToken(){
        String token = "";
        for (int i = 0; i < 10; i++) {
            token += (char) (Math.random() * 26 + 'a');
        }
        return token;
    }

    public void logout(String username){
        Player player = getPlayer(username);
        player.setToken("");
    }
}



