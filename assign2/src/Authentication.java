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

                player= New Player(username,password,token,elo);

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
        return null
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
}



