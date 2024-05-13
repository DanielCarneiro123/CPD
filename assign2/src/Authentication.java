import json.jar;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


class Authentication {
    private List<Player> players;
    
    public Authentication(){
        players= getPlayers();
    }

    public List<Player> getPlayers(){
        JSONParser parser = new JSONParser();
        List<Player> players;
        try {     
            Object obj = parser.parse(new FileReader("database.json"));

            for (Object o : a)
            {
                JSONObject jsonObject =  (JSONObject) obj;

                String username = (String) jsonObject.get("username");

                String password = (String) jsonObject.get("password");

                int elo = Integer.parseInt((String) jsonObject.get("elo"));

                String token = (String) jsonObject.get("token");

                player= new Player(username,password,token,elo);

                players.append(player);
            }

            return players;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
        var player= players.getPlayer(username);
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
        Player newPlayer = new Player(username, pass, "", 1400);
        try {
            JSONObject newPlayerJson = new JSONObject();
            newPlayerJson.put("username", newPlayer.getUsername());
            newPlayerJson.put("password", newPlayer.getPassword());
            newPlayerJson.put("elo", String.valueOf(newPlayer.getElo()));
            newPlayerJson.put("token", newPlayer.getToken());

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("database.json"));
            JSONArray jsonArray = (JSONArray) obj;
            jsonArray.add(newPlayerJson);

            FileWriter fileWriter = new FileWriter("database.json");
            fileWriter.write(jsonArray.toJSONString());
            fileWriter.flush();
            fileWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
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



