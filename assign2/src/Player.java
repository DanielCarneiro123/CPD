import java.nio.channels.SocketChannel;

public class Player {

    private String username;
    private String password;
    private String token;
    private Float elo;
    private SocketChannel socket;
    private int timeInQueue = 0;

    public Player(String username, String password, String token, Float elo, SocketChannel socket) {
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

    public void setToken(String token) {
        this.token = token;
    }

    public void setElo(Float value) {
        this.elo = value;
    }

    public SocketChannel getSocket() {
        return this.socket;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public boolean equals(Player Player) {
        return this.username.equals(Player.getUsername());
    }
}