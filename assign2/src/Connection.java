import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import javafx.util.Pair;

public class Connection {

    private final int port;                                 
    private final String host;                              
    private SocketChannel socket;
    private Selector selector;                        
    private final String TOKEN_PATH = "tokens/";    
    private final long TIMEOUT = 30000;                    
    private MainMenu mainMenu;                            



    public Connection(int port, String host) {
        this.port = port;
        this.host = host;
        this.mainMenu = new MainMenu();
    }

    public void start() throws IOException {
        this.socket =  SocketChannel.open(new InetSocketAddress(this.host, this.port));

    }

    public void stop() throws IOException {
        this.socket.close(); 
    }

    private static void printUsage() {
        System.out.println("usage: java Connection <PORT> [HOST]");
    }

    public static void send(SocketChannel socket, String message) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);      
        buffer.clear();                                    
        buffer.put(message.getBytes());                     
        buffer.flip();                                      
        while (buffer.hasRemaining()) {   
            this.socket.write(buffer);             
        }
    }

    public static String receive(SocketChannel socket) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);         
        InputStream inputStream = socket.getInputStream();
        byte[] byteBuffer = new byte[buffer.capacity()];
        int bytesRead = inputStream.read(byteBuffer);
        buffer.put(byteBuffer, 0, bytesRead);             
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


    public boolean handleLoginResponse() {

        String[] serverAnswer;
        String requestType;

        serverAnswer = Connection.receive(this.socket).split("\n");
        requestType = serverAnswer[0].toUpperCase();

        switch (requestType) {
            case "NACK": {
                System.out.println(serverAnswer[1]);

            }
            case "ACK": {
                System.out.println("Login successful");
                Connection.send(this.socket, "ACK");

            }
        }
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            Connection.printUsage();
            return;
        }
        Connection connection = null;

        try {

            int port = Integer.parseInt(args[0]);
            String host = args.length == 2 ? args[1] : "localhost";
            connection = new Connection(port, host);
            connection.start();
            int option = Integer.parseInt(this.mainMenu.MainMenu());
            switch (option) {
                case 1:
                    int i= 3;
                    while (i > 0) {
                        Pair<String,String> data = this.mainMenu.LoginMenu();
                        if(connection.handleLoginResponse(data.getKey(), data.getValue()))break;
                        i--;
                    }
                    if(i == 0){
                        System.out.println("Too many attempts");
                        connection.stop();
                        return;
                    }
                    break;
                case 2:
                    while(true){
                        Pair<String,String> data = this.mainMenu.RegisterMenu();
                        if(connection.handleRegister(data.getKey(), data.getValue()))break;
                        System.out.println("Try again\n");
                    }
                    break;
                case 3: 
                    connection.handleReconnect();
                    break;
                case 4:
                    connection.stop();
                    return;
                default:
                    System.out.println("Invalid option");
                    break;
            }
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