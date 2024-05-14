import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Connection {

    private final int port;                                 
    private final String host;                            
    private Socket socket;                           
    private final String TOKEN_PATH = "tokens/";    
    private static final String DEFAULT_HOST = "localhost"; 
    private final long TIMEOUT = 30000;                    
    private MainMenu MainMenu;                            
    private int authenticationOption = 0;

    public Connection(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void start() throws IOException {
        this.socket = new Socket(this.host, this.port);
    }

    public void stop() throws IOException {
        this.socket.close(); 
    }

    private static void printUsage() {
        System.out.println("usage: java Connection <PORT> [HOST]");
    }

    // Static method to send a message through a Socket
    public static void send(Socket socket, String message) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);      
        buffer.clear();                                    
        buffer.put(message.getBytes());                     
        buffer.flip();                                      
        while (buffer.hasRemaining()) {                    
            socket.write(buffer);
        }
    }

    // Static method to receive a message from a Socket
    public static String receive(Socket socket) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);         
        int bytesRead = socket.read(buffer);                   
        return new String(buffer.array(), 0, bytesRead); 
    }

    public String readToken(String filename) {

        if (filename == null || filename.equals("")) {
            return null;
        }

        File file = new File(this.TOKEN_PATH + filename);

        if (!file.exists()) {
            System.out.println("File - "+ filename  + " - does not exist");
            return null;
        }

        StringBuilder fileContent = new StringBuilder(); 
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file)); 
            String line;
            while ((line = reader.readLine()) != null) {   
                fileContent.append(line);
            }
            reader.close(); 
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent.toString(); 
    }

    public void writeToken(String filename, String content) {

        try {
            File file = new File(this.TOKEN_PATH + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            bufferedWriter.write(content); 
            bufferedWriter.close(); 

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean authenticate() throws Exception {

        String[] serverAnswer;
        String requestType;
        boolean takenUsername = false;
        boolean invalidCredentials = false;

        do {
            serverAnswer = Connection.receive(this.socket).split("\n");
            requestType = serverAnswer[0].toUpperCase();

            switch (requestType) {
                case "OPT" -> { 
                    String menu = String.join("\n", Arrays.copyOfRange(serverAnswer, 1, serverAnswer.length));
                    System.out.println(menu);
                    Connection.send(this.socket, this.mainMenu());
                }
                case "USR" -> {

                    String[] credentials;

                    do {
                        credentials = this.loginAndRegister(invalidCredentials, takenUsername);
                    } while ((credentials[0].equals("") || credentials[1].equals("")) && !(credentials[2].equals("BACK")));


                    if (credentials[2].equals("BACK")) {
                        Connection.send(this.socket, credentials[2]);
                        takenUsername = false;
                        invalidCredentials = false;
                    }
                    else {
                        Connection.send(this.socket, credentials[0].toLowerCase());

                        serverAnswer = Connection.receive(this.socket).split("\n");
                        requestType = serverAnswer[0].toUpperCase(); 

                        if (!requestType.equals("FIN")) {
                            Connection.send(this.socket, credentials[1]);
                        }
                    }
                }
                case "TKN" -> {
                    System.out.println(serverAnswer[1]);
                    System.out.println("Token file name: ");
                    String token;

                    boolean invalidToken = false;

                    do {
                        token = this.getTokenFromGUI(invalidToken);
                        invalidToken = token == null;
                    } while (token == null);

                    System.out.println("Token: " + token);
                    Connection.send(this.socket, token == null ? "invalid" : token);
                }
                case "NACK" -> { 
                    System.out.println(serverAnswer[1]);

                    if(serverAnswer[1].equals("Username already in use")) {
                        takenUsername = true;
                    } else if(serverAnswer[1].equals("Wrong username or password")) {
                        invalidCredentials = true;
                    }

                    Connection.send(this.socket, "ACK");

                    if (authenticationOption > 0) {
                        serverAnswer = Connection.receive(this.socket).split("\n");
                        requestType = serverAnswer[0].toUpperCase();

                        Connection.send(this.socket,Integer.toString(authenticationOption));
                    }
                }
                case "AUTH" -> { 
                    System.out.println("Success. Session token was received.");
                    Connection.send(this.socket, "ACK");
                    this.writeToken(serverAnswer[1], serverAnswer[2]);
                }
                case "FIN" -> System.out.println(serverAnswer[1]);
                default -> System.out.println("Unknown server request type :" + requestType);
            }
        } while (!requestType.equals("AUTH") && !requestType.equals("FIN"));
        return requestType.equals("AUTH"); 
    }

    public void listening() throws Exception {

        String[] serverAnswer;
        String requestType = "";
        long lastTime = System.currentTimeMillis();
        long currentTime;
        Selector selector = Selector.open();
        this.socket.configureBlocking(false);
        this.socket.register(selector, SelectionKey.OP_READ);

        do {

            int readyChannels = selector.select(TIMEOUT);
            if (readyChannels == 0) {
                currentTime = System.currentTimeMillis() - lastTime;
                if(currentTime > TIMEOUT) {
                    System.out.println("Server is not responding. Closing connection...");
                    break;
                }
                continue;
            } else {
                lastTime = System.currentTimeMillis();
                selector.selectedKeys().clear();
            }

            serverAnswer = Connection.receive(this.socket).split("\n");
            requestType = serverAnswer[0].toUpperCase();
            System.out.println("REQUEST TYPE: " + Arrays.toString(serverAnswer));

            switch (requestType) {
                case "QUEUE" -> {
                    Connection.send(this.socket, "ACK");
                    queueGUI(serverAnswer[1]);
                }
                case "FIN" -> {
                    Connection.send(this.socket, "ACK");
                }
                case "INFO", "TURN", "SCORE" -> {
                    gameGUI(serverAnswer, requestType);
                    Connection.send(this.socket, "ACK");
                }
                case "GAMEOVER" -> {
                    Connection.send(this.socket, gameOverGUI(serverAnswer[1]));
                }
                case "PING" -> {
                    ; 
                }
                default -> System.out.println("Unknown server request type");
            }

        } while (!requestType.equals("FIN"));
        selector.close();

    }

    public void initGUI() {
        this.MainMenu = new MainMenu(10000);
    }

    public String mainMenuGUI() {
        authenticationOption = Integer.parseInt(this.MainMenu.mainMenu());
        return Integer.toString(authenticationOption);
    }

    public String[] loginAndRegister(boolean invalidCredentials, boolean takenUsername) {
        return this.MainMenu.loginAndRegister(invalidCredentials, takenUsername);
    }

    public String getTokenFromGUI(boolean invalidToken) {
        String[] result = this.MainMenu.reconnect(invalidToken);

        if(result[1].equals("BACK"))
            return result[1];
        else
            return readToken(result[0]);
    }

    public void queueGUI(String serverMessage) {
        this.MainMenu.queue(serverMessage);
    }

    public void gameGUI(String[] serverMessages, String requestType) {
        switch (requestType) {
            case "INFO" -> this.MainMenu.info();
            case "TURN" -> this.MainMenu.turn();
            case "SCORE" -> this.MainMenu.updateScore(serverMessages);
        }
    }

    public String gameOverGUI(String serverMessage) {
        return this.MainMenu.gameOver(serverMessage);
    }

    public void closeGUI() {
        if (this.MainMenu != null) this.MainMenu.close();
    }

    public static void main(String[] args) {

        // Check if there are enough arguments
        if (args.length < 1) {
            Connection.printUsage();
            return;
        }
        Connection connection = null;

        // Parse port and host arguments and create a Connection object
        try {

            int port = Integer.parseInt(args[0]);
            String host = args.length == 2 ? args[1] : Connection.DEFAULT_HOST;
            connection = new Connection(port, host);
            connection.start();
            connection.initGUI();

            // Start the connection and authenticate
            if (connection.authenticate())
                connection.listening();
            else
                connection.stop();

            connection.closeGUI();

        } catch (UnknownHostException exception) {
            System.out.println("Host not found: " + exception.getMessage());

        } catch (Exception exception) {
            System.out.println("I/O error: " + exception.getMessage());

        } finally {
            if (connection != null) {
                try {
                    connection.stop();
                    connection.closeGUI();
                } catch (IOException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}