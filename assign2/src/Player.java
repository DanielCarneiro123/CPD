import java.nio.channels.SocketChannel;

public class PLayer {

    private final String username;
    private final String password;
    private final String token;
    private Float elo;
    private SocketChannel socket;
    private int timeInQueue = 0;

    public Player(String username, String password, String token, Float elo) {
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

    public void setElo(Float value) {
        this.elo = value;
    }

    public boolean equals(Player Player) {
        return this.username.equals(Player.getUsername());
    }
}