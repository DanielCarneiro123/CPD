import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import java.io.*;
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
                        break;
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
                if (serverMessage.contains("Make your guess")) {
                    String guess = consoleReader.readLine();
                    writer.println(guess);
                    writer.flush(); 
                } else if (serverMessage.startsWith("Game over")) {
                    printResults();
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
            while ((serverMessage = reader.readLine()) != null) {
                System.out.println(serverMessage);
            }
        } catch (IOException e) {
            System.out.println("Error while communicating with the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
