import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;

public class ServerCommunication implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private BufferedReader consoleReader;

    public ServerCommunication(Socket socket, BufferedReader reader, PrintWriter writer, BufferedReader consoleReader) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
        this.consoleReader = consoleReader;
    }

    @Override
    public void run() {
        enterGameQueue();
    }

    public void enterGameQueue() {
        System.out.println("Waiting for the game to start...");
        while (true) {
            try {
                String serverMessage = reader.readLine();
                if (serverMessage != null) {
                    System.out.println("Received from server: " + serverMessage);
                    if (serverMessage.equals("Game start!")) {
                        writer.println("Game start received");
                        writer.flush();
                        playGame();
                        if (!socket.isClosed() && replayGame() == false) {
                            break;
                        }
                        System.out.println("Waiting for new game to start...");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error while communicating with the server: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }

    public void playGame() {
        System.out.println("Playing the game...");
        try {
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                System.out.println("Server: " + serverMessage);
                if (serverMessage.startsWith("Make your move")) {
                    String play = consoleReader.readLine();
                    writer.println(play);
                    writer.flush();
                } else if (serverMessage.startsWith("Game over")) {
                    printResults();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error while communicating with the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void printResults() {
        try {
            String serverMessage;
            while (!(serverMessage = reader.readLine()).equals("Finish")) {
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            System.out.println("Error while communicating with the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean replayGame() {
        System.out.println("Would you like to play again? (y/n)");
        try {
            while (true) {
                String response = consoleReader.readLine();

                if (response.equals("y") || response.equals("n")) {
                    writer.println(response);
                    writer.flush();
                    if (response.equals("y")) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    System.out.println("Invalid response. Please try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
