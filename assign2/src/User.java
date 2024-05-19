import java.net.Socket;


class User {
    private String username;
    private String passwordHash;
    private int elo;
    private String token;
    private Socket socket;


    public User(String username, String passwordHash, int elo, String token, Socket socket) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.elo = elo;
        this.token = token;
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getElo() {
        return elo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof User)) {
            return false;
        }

        User user = (User) obj;
        return user.getUsername().equals(username);
    }
}
