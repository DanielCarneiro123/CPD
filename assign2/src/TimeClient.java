import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class TimeClient {

    private static String token = null;

    public static void main(String[] args) {
        if (args.length < 2)
            return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to server. Enter your username and password:");
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            if (token != null) {
                writer.println(token);
            } else {
                String username = consoleReader.readLine();
                String password = consoleReader.readLine();

                writer.println("");
                writer.println(username);
                writer.println(password);
            }

            String response = reader.readLine();
            if (response.startsWith("Authenticated")) {
                System.out.println("Authentication successful.");
                if (token == null) {
                    token = reader.readLine();
                }
                ServerCommunication serverComm = new ServerCommunication(socket, reader, writer, consoleReader);
                serverComm.run(); 
            } else {
                System.out.println("Authentication failed: " + response);
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
