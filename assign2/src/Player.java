import java.nio.channels.SocketChannel;

public class Client {

    private final String username;
    private final String password;
    private final String token;
    private Float elo;
    private SocketChannel socket;
    private int timeInQueue = 0;

    Client(String username, String password, String token, Float elo, SocketChannel socket) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.elo = elo;
        this.socket = socket;
    }

    public String getUsername() {
        return this.username;
    }

    public Float getElo() {
        return this.elo;
    }

    public void changeElo(Float value) {
        this.elo += value;
    }

    public SocketChannel getSocket() {
        return this.socket;
    }

    public void setSocket(SocketChannel socket) {
        this.socket = socket;
    }

    public boolean equals(Client client) {
        return this.username.equals(client.getUsername());
    }
}