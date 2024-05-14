import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocket;
import java.nio.channels.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    private final int port;
    private final int mode;
    private ServerSocket serverSocket;
    private final ExecutorService threadPoolGame;
    private final ExecutorService threadPoolAuth;
    private int time;
    private long startTime;
    private final ReentrantLock time_lock;

    private final int TIMEOUT = 30000;          
    private final int PING_INTERVAL = 10000;    
    private long lastPing;

    private final int MAX_CONCURRENT_GAMES = 5;
    private final int PLAYERS_PER_GAME = 2;

    private final int TIME_FACTOR = 1;

    private Database database;
    private ReentrantLock database_lock;
    private final String DATABASE_PATH = "database";

    private List<Player> waiting_queue;
    private ReentrantLock waiting_queue_lock;
    private final int MAX_CONCURRENT_AUTH = 5;

    private int token_index;
    private ReentrantLock token_lock;


    public Server(int port, int mode, String filename) throws IOException {

        this.port = port;
        this.mode = mode;
        this.startTime = System.currentTimeMillis();

        this.threadPoolGame = Executors.newFixedThreadPool(this.MAX_CONCURRENT_GAMES);
        this.threadPoolAuth = Executors.newFixedThreadPool(this.MAX_CONCURRENT_AUTH);
        this.waiting_queue = new ArrayList<Player>();
        this.database = new Database(this.DATABASE_PATH + filename);
        this.token_index = 0;
        this.time = 0;

        this.waiting_queue_lock = new ReentrantLock();
        this.database_lock = new ReentrantLock();
        this.token_lock = new ReentrantLock();
        this.time_lock = new ReentrantLock();

        this.lastPing = System.currentTimeMillis();
    }

    public static void printUsage() {
        System.out.println("usage: java Server <PORT> <MODE> <DATABASE>");
        System.out.println("       <MODE>");
        System.out.println("           0 - Simple Mode");
        System.out.println("           1 - Ranking Mode");
        System.out.println("       <DATABASE>");
        System.out.println("           JSON file name inside Server/databases folder");
    }

    public void start() throws IOException {
        this.serverSocket = ServerSocket.open();
        serverSocket.bind(new InetSocketAddress(this.port));
        System.out.println("Server is listening on port " + this.port + " with " + (this.mode == 1 ? "rank" : "simple") + " mode");
    }

    private void updateServerTime() {
        this.time_lock.lock();
        long elapsedTime = System.currentTimeMillis() - this.startTime;
        this.time = (int) (elapsedTime / 1000);
        this.time_lock.unlock();
    }

    private void resetServerTime() {
        this.time_lock.lock();
        this.startTime = System.currentTimeMillis();
        this.time = 0;
        this.time_lock.unlock();
    }

    private void gameSchedulerSimple() {

        this.waiting_queue_lock.lock();

        if (this.waiting_queue.size() >= this.PLAYERS_PER_GAME) { 
            List<Player> gamePlayers = new ArrayList<>();
            for (int i = 0; i < this.PLAYERS_PER_GAME; i++) {
                gamePlayers.add(this.waiting_queue.remove(0)); 
                System.out.println("Player " + gamePlayers.get(i).getUsername() + " removed from waiting queue");
            }
            Runnable gameRunnable = new Game(gamePlayers, this.database, this.database_lock, this.waiting_queue, this.waiting_queue_lock);

            this.threadPoolGame.execute(gameRunnable); 
        }

        this.waiting_queue_lock.unlock();
    }

    private void gameSchedulerRank() {

        this.waiting_queue_lock.lock();
        if (this.waiting_queue.size() >= this.PLAYERS_PER_GAME) { 
            this.updateServerTime();       
            this.sortPlayers();             
            int slack = this.getSlack();    
            for (int i = 0 ; i < this.waiting_queue.size() - this.PLAYERS_PER_GAME + 1; i++) {

                Player first = this.waiting_queue.get(i + this.PLAYERS_PER_GAME - 1);
                Player second = this.waiting_queue.get(i);

                if (first.getElo() - second.getElo() > slack) {
                    continue;
                }

                List<Player> gamePlayers = new ArrayList<>();
                for (int j = 0; j < this.PLAYERS_PER_GAME; j++) {
                    gamePlayers.add(this.waiting_queue.remove(i));
                }

                Runnable gameRunnable = new Game(gamePlayers, this.database, this.database_lock, this.waiting_queue, this.waiting_queue_lock);
                this.threadPoolGame.execute(gameRunnable);
                this.waiting_queue_lock.unlock();
                this.resetServerTime();
                return;
            }
        }

        this.waiting_queue_lock.unlock();
    }

    private int getSlack() {
        this.time_lock.lock();
        int current_time = this.time;
        this.time_lock.unlock();
        return current_time / this.TIME_FACTOR;
    }

    private void connectionAuthenticator() {
        while (true) {
            try {
                Socket PlayerSocket = this.serverSocket.accept();
                System.out.println("Player connected: " + PlayerSocket.getRemoteAddress());

                Runnable newPlayerRunnable = () -> {
                    try {
                        handlePlayer(PlayerSocket);
                    } catch (Exception exception) {
                        System.out.println("Error handling Player: " + exception);
                    }
                };
                this.threadPoolAuth.execute(newPlayerRunnable);

            } catch (Exception exception) {
                System.out.println("Error handling Player: " + exception);
            }
        }
    }

    private void pingPlayers() {
        if(System.currentTimeMillis() - this.lastPing > this.PING_INTERVAL) {
            this.lastPing = System.currentTimeMillis();

            this.waiting_queue_lock.lock();
            if (this.waiting_queue.size() == 0) {
                this.waiting_queue_lock.unlock();
                return;
            }

            System.out.println("Pinging Players...");

            Iterator<Player> iterator = this.waiting_queue.iterator();
            while (iterator.hasNext()) {
                Player Player = iterator.next();
                try {
                    Server.request(Player.getSocket(), "PING", "");
                } catch (IOException exception) {
                    System.out.println("Error pinging Player: " + exception);
                    iterator.remove();
                } catch (Exception e) {
                    this.waiting_queue_lock.unlock();
                    throw new RuntimeException(e);
                }
            }
            this.waiting_queue_lock.unlock();
        }
    }

    public void run() throws IOException {

        Thread gameSchedulerThread = new Thread(() -> {
            while (true) {
                pingPlayers();
                if (mode == 0)
                    gameSchedulerSimple();
                else
                    gameSchedulerRank();
            }
        });

        Thread connectionAuthenticatorThread = new Thread(() -> {
            while (true) connectionAuthenticator();
        });

        this.database_lock.lock();
        this.database.resetTokens();
        this.database_lock.unlock();

        gameSchedulerThread.start();
        connectionAuthenticatorThread.start();
    }

    private String getToken(String username) {
        this.token_lock.lock();
        int index = this.token_index;
        this.token_index++;
        this.token_lock.unlock();
        return BCrypt.hashpw(username + index, BCrypt.gensalt());
    }

    private void insertPlayer(Player Player) {

        try {
            this.waiting_queue_lock.lock();
            for (Player c : this.waiting_queue) {
                if (c.equals(Player)) {
                    c.setSocket(Player.getSocket());
                    System.out.println("Player " + Player.getUsername() + " reconnected. Queue size: " + this.waiting_queue.size());
                    Server.request(Player.getSocket(), "QUEUE", "You are already in the waiting queue with " + Player.getElo() + " points.");
                    Connection.receive(Player.getSocket());
                    this.waiting_queue_lock.unlock();
                    return;
                }
            }

            this.waiting_queue.add(Player);
            Server.request(Player.getSocket(), "QUEUE", "You entered in waiting queue with ranking  " + Player.getElo() + " points.");
            Connection.receive(Player.getSocket());
            System.out.println("Player " + Player.getUsername() + " is now in waiting queue. Queue size: " + this.waiting_queue.size());

        } catch (Exception exception) {
            System.out.println("Error during insert in waiting queue. Info: " + exception.getMessage());
        } finally {
            this.waiting_queue_lock.unlock();
        }
    }

    private void sortPlayers() {
        this.waiting_queue_lock.lock();
        this.waiting_queue.sort(Comparator.comparingLong(Player::getElo));
        this.waiting_queue_lock.unlock();
    }

    public Player login(Socket PlayerSocket, String username, String password) throws Exception {

        if (Objects.equals(username, "BACK") || Objects.equals(password, "BACK"))
            return null;

        String token = this.getToken(username);
        Player Player;

        try {
            this.database_lock.lock();
            Player = this.database.login(username, password, token, PlayerSocket);
            this.database.backup();
            this.database_lock.unlock();

            if (Player != null) {
                Server.request(PlayerSocket, "AUTH", "token-" + username + ".txt\n" + token);
                Connection.receive(PlayerSocket);
                return Player;
            } else {
                Server.request(PlayerSocket, "NACK", "Wrong username or password");
                Connection.receive(PlayerSocket);
            }

        } catch (Exception e) {
            Server.request(PlayerSocket, "NACK", e.getMessage());
            Connection.receive(PlayerSocket);
        }
        return null;
    }

    public Player register(Socket PlayerSocket, String username, String password) throws Exception {

        if (Objects.equals(username, "BACK") || Objects.equals(password, "BACK"))
            return null;

        String token = this.getToken(username);
        Player Player;

        try {
            this.database_lock.lock();
            Player = this.database.register(username, password, token, PlayerSocket);
            this.database.backup();
            this.database_lock.unlock();

            if (Player != null) {
                Server.request(PlayerSocket, "AUTH", "token-" + username + ".txt\n" + token);
                Connection.receive(PlayerSocket);
                return Player;
            } else {
                Server.request(PlayerSocket, "NACK", "Username already in use");
                Connection.receive(PlayerSocket);
            }

        } catch (Exception e) {
            Server.request(PlayerSocket, "NACK", e.getMessage());
            Connection.receive(PlayerSocket);
        }
        return null;
    }

    public Player reconnect(Socket PlayerSocket, String token) throws Exception {

        this.database_lock.lock();
        Player Player = this.database.reconnect(token, PlayerSocket);
        this.database.backup();
        this.database_lock.unlock();

        if (Player != null) {
            Server.request(PlayerSocket, "AUTH", "token-" + Player.getUsername() + ".txt\n" + token);
            Connection.receive(PlayerSocket);
        } else {
            Server.request(PlayerSocket, "NACK","Invalid session token");
            Connection.receive(PlayerSocket);
        }
        return Player;
    }

    public static void request(Socket socket, String requestType, String message) throws Exception {
        Connection.send(socket, requestType + "\n" + message);
    }

    public void handlePlayer(Socket PlayerSocket) throws Exception {

        String input;
        Player Player = null;
        long startTime = System.currentTimeMillis();

        do {

            if (System.currentTimeMillis() - startTime >= this.TIMEOUT) {
                System.out.println("Connection timeout");
                Server.request(PlayerSocket, "FIN", "Connection terminated");
                return;
            }

            Server.request(PlayerSocket, "OPT", "1 - Login\n2 - Register\n3 - Reconnect\n4 - Quit");
            input = Connection.receive(PlayerSocket).toUpperCase();

            if (input.equals("4")) {
                Server.request(PlayerSocket, "FIN", "Connection terminated");
                PlayerSocket.close();
                return;
            }

            if (!(input.equals("1") || input.equals("2") || input.equals("3"))) {
                Server.request(PlayerSocket, "NACK", "Option refused");
                Connection.receive(PlayerSocket);
                continue;
            }

            String username, password, token;
            switch (input) {
                case "1" -> {
                    Server.request(PlayerSocket, "USR", "Username?");
                    username = Connection.receive(PlayerSocket);
                    System.out.println(username);
                    if (username.equals("BACK")) continue;
                    Server.request(PlayerSocket, "PSW", "Password?");
                    password = Connection.receive(PlayerSocket);
                    Player = this.login(PlayerSocket, username, password);
                }
                case "2" -> {
                    Server.request(PlayerSocket, "USR", "Username?");
                    username = Connection.receive(PlayerSocket);
                    if (username.equals("BACK")) continue;
                    Server.request(PlayerSocket, "PSW", "Password?");
                    password = Connection.receive(PlayerSocket);
                    Player = this.register(PlayerSocket, username, password);
                }
                case "3" -> {
                    Server.request(PlayerSocket, "TKN", "Token?");
                    token = Connection.receive(PlayerSocket);
                    System.out.println("TOKEN: " + token);
                    if (token.equals("BACK")) continue;
                    Player = this.reconnect(PlayerSocket, token);
                }
                default -> {
                    Server.request(PlayerSocket, "FIN", "Connection terminated");
                    PlayerSocket.close();
                    return;
                }
            }

            if (Player != null) {
                this.insertPlayer(Player);
                if (this.mode == 1) {
                    this.sortPlayers();
                    this.resetServerTime();
                }
            }

        } while (Player == null);
    }


    public static void main(String[] args) {

        if (args.length != 3) {
            Server.printUsage();
            return;
        }

        int port = Integer.parseInt(args[0]);
        int mode = Integer.parseInt(args[1]);
        String filename = args[2];
        if (mode != 0 && mode != 1) {
            Server.printUsage();
            return;
        }

        try {
            Server server = new Server(port, mode, filename);
            server.start();
            server.run();
        } catch (IOException | ParseException exception) {
            System.out.println("Server exception: " + exception.getMessage());
        }
    }

    public void game(){
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        String[] coinSides = {"Heads", "Tails"};

        System.out.println("Welcome to the Coin Toss Game! Please choose Heads or Tails:");
        String playerChoice = scanner.nextLine();

        String coinTossResult = coinSides[random.nextInt(2)];

        if (playerChoice.equalsIgnoreCase(coinTossResult)) {
            System.out.println("Congratulations! You won! The coin landed on " + coinTossResult);
        } else {
            System.out.println("Sorry, you lost. The coin landed on " + coinTossResult);
        }

        scanner.close();
    }
}