import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Game implements Runnable {
    private List<User> users;
    private final Random random = new Random();
    private final ReentrantLock lock = new ReentrantLock();
    private Map<User, Integer> scores;
    private final int rounds = 5; // Número de rodadas do jogo

    public Game(List<User> users) {
        this.users = users;
        this.scores = new HashMap<>();
        for (User user : users) {
            scores.put(user, 0);
        }
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        System.out.println("Starting game with " + users.size() + " players");
        notifyGameStart();
        for (int i = 0; i < rounds; i++) {
            playRound();
        }
        endGame();
    }

    private void notifyGameStart() {
        for (User user : users) {
            try {
                PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
                out.println("Game start!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playRound() {
        Map<User, String> guesses = new HashMap<>();

        for (User user : users) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(user.getSocket().getInputStream()));
                PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);

                out.println("Make your guess (cara/coroa):");
                String guess = in.readLine().trim().toLowerCase();

                if (guess.equals("cara") || guess.equals("coroa")) {
                    guesses.put(user, guess);
                } else {
                    out.println("Invalid guess, please enter 'cara' or 'coroa'.");
                    guess = in.readLine().trim().toLowerCase();
                    guesses.put(user, guess);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String result = random.nextBoolean() ? "cara" : "coroa";
        System.out.println("The coin landed on: " + result);

        for (Map.Entry<User, String> entry : guesses.entrySet()) {
            User user = entry.getKey();
            String guess = entry.getValue();
            try {
                PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
                if (guess.equals(result)) {
                    lock.lock();
                    try {
                        int newScore = scores.get(user) + 1;
                        scores.put(user, newScore);
                    } finally {
                        lock.unlock();
                    }
                    out.println("Correct! The coin landed on " + result + ". Your score: " + scores.get(user));
                } else {
                    out.println("Incorrect! The coin landed on " + result + ". Your score: " + scores.get(user));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void endGame() {
        for (User user : users) {
            try {
                PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
                out.println("Game over! Final scores:");
                for (Map.Entry<User, Integer> entry : scores.entrySet()) {
                    User player = entry.getKey();
                    int score = entry.getValue();
                    out.println("Player " + player.getUsername() + ": " + score);
                }
                user.getSocket().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Game ended.");
    }
}
