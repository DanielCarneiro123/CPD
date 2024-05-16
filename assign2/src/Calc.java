import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Calc implements Runnable {
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Shared shared;
    private final User player;
    private User opponent;
    private boolean isChoiceMade;
    private String choice;
    private String opponentChoice;
    private final Lock gameLock = new ReentrantLock(); 

    public Calc(Socket socket, Shared shared, User player) {
        this.shared = shared;
        this.player = player;
        this.opponent = null;
        this.isChoiceMade = false;
        this.choice = null;
        this.opponentChoice = null;

        try {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ex) {
            throw new RuntimeException("Error initializing Calc", ex);
        }
    }

    public void run() {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(4);
            while (true) {
                String input = reader.readLine();
                if (input.equals("exit")) {
                    break;
                } else if (opponent == null) {
                    gameLock.lock(); 
                    try {
                        opponent = shared.getUserByName(input);
                    } finally {
                        gameLock.unlock(); 
                    }
                } else if (!isChoiceMade) {
                    gameLock.lock(); 
                    try {
                        choice = input;
                        isChoiceMade = true;
                    } finally {
                        gameLock.unlock();
                    }
                } else {
                    gameLock.lock();
                    try {
                        opponentChoice = input;
                        String result = determineWinner(choice, opponentChoice);
                        writer.println(result);
                        break;
                    } finally {
                        gameLock.unlock();
                    }
                }
            }
            executorService.close();
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    private String determineWinner(String choice, String opponentChoice) {
        if (choice.equalsIgnoreCase("cara") && opponentChoice.equalsIgnoreCase("coroa")) {
            return player.getUsername() + " venceu!";
        } else if (choice.equalsIgnoreCase("coroa") && opponentChoice.equalsIgnoreCase("cara")) {
            return opponent.getUsername() + " venceu!";
        } else {
            return "Empate!";
        }
    }
}
