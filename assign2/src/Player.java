import java.net.Socket;


public class Player {

    private String username;
    private String password;
    private String token;
    private Float elo;
    private Socket socket;
    private int timeInQueue = 0;

    public Player(String username, String password, Float elo, String token, Socket socket) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        if (elo < 1000) {
            throw new IllegalArgumentException("Elo cannot be lower than 1000");
        }

        this.username = username;
        this.password = password;
        this.token = token;
        this.elo = elo;
        this.socket = socket;
    }

    public String getUsername() {
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
    public Float getElo() {
        return this.elo;
    }
    public String getToken(){
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setElo(Float value) {
        this.elo = value;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean equals(Player Player) {
        return this.username.equals(Player.getUsername());
    }
}