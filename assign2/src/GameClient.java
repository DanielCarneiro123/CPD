import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GameClient {

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 2 ) {
            System.out.println("Usage: java GameClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to server.");
            Scanner scanner = new Scanner(System.in);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            int attempts = 3;
            while (attempts-- >= 0) {
                System.out.println("Choose an option:");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Reconnect");
                System.out.println("4. Exit");
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                if (choice.equals("1")) {

                    System.out.println("Enter your username:");
                    String username = consoleReader.readLine();
                    System.out.println("Enter your password:");
                    String password = consoleReader.readLine();

                    writer.println("login");
                    writer.println(username);
                    writer.println(password);

                } else if (choice.equals("2")) {
                    System.out.println("Enter your desired username:");
                    String username = consoleReader.readLine();
                    System.out.println("Enter your desired password:");
                    String password = consoleReader.readLine();

                    writer.println("register");
                    writer.println(username);
                    writer.println(password);
                } else if (choice.equals("3")) {
                    System.out.println("Enter your username:");
                    String username = consoleReader.readLine();
                    writer.println("reconnect");
                    writer.println(username);
                } else if (choice.equals("4")) {
                    writer.println("exit");
                    socket.close();
                    break;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                    continue;
                }

                String response = reader.readLine();
                if (response != null && (response.startsWith("Authenticated") || response.startsWith("Reconnected"))) {
                    System.out.println("Authentication successful.");
                    ServerCommunication serverComm = new ServerCommunication(socket, reader, writer, consoleReader);
                    serverComm.run();
                    break;
                } else {
                    System.out.println("Authentication failed: " + response);
                }
            }
            if (attempts == 0) 
                System.out.println("Too many failed attempts. Exiting...");
            
            if (scanner != null)
                scanner.close();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
