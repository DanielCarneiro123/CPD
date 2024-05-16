import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimeServer {

    private static final Lock serverLock = new ReentrantLock(); 

    public static void main(String[] args) {
        if (args.length < 1)
            return;

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            Shared shared = new Shared();

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                processConnection(socket, shared);
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void processConnection(Socket socket, Shared shared) throws IOException {
        serverLock.lock(); 
        try {
            try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            ) {
                String username = reader.readLine();
                String password = reader.readLine();
                User authenticatedUser = processAuthentication(username, password);
                if (authenticatedUser != null) {
                    writer.println("Authenticated");
                    Calc calc = new Calc(socket, shared, authenticatedUser);
                    Thread thread = new Thread(calc);
                    thread.start();
                } else {
                    writer.println("Invalid credentials");
                    socket.close();
                }
            }
        } finally {
            serverLock.unlock(); 
        }
    }

    private static User processAuthentication(String username, String password) {
        serverLock.lock(); 
        try {
            if (Authentication.authenticate(username, password)) {
                return Authentication.getUserByUsername(username);
            } else {
                return null;
            }
        } finally {
            serverLock.unlock(); 
        }
    }
}
