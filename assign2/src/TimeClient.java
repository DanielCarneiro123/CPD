import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimeClient {

    public static void main(String[] args) {
        if (args.length < 2)
            return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        Lock clientLock = new ReentrantLock(); 

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to server. Enter your username and password:");
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            String username = consoleReader.readLine();
            String password = consoleReader.readLine();
            
            clientLock.lock(); 
            try {
                writer.println(username);
                writer.println(password);

                String response = reader.readLine();
                System.out.println(response);
                if (response.startsWith("Authenticated")) {
                    System.out.println("Authentication successful.");

                    System.out.println("Enter your choice (cara ou coroa):");
                    String choice = consoleReader.readLine();
                    writer.println(choice);
                    String result = reader.readLine();
                    System.out.println("Result: " + result);
                } else {
                    System.out.println("Authentication failed: " + response);
                }
            } finally {
                clientLock.unlock(); 
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
