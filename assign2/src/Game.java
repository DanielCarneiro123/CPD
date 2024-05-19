import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Game implements Runnable {
    private List<User> users;
    private final Random random = new Random();
    private final ReentrantLock lock = new ReentrantLock();
    private Map<User, Integer> scores;
    private final int rounds = 3;
    private boolean rankMode;

    public Game(List<User> users, boolean mode) {
        this.users = users;
        this.rankMode = mode;
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
                if (!user.getSocket().isClosed()) {
                    PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(user.getSocket().getInputStream()));

                    out.println("Game start!");
                    out.flush();

                    String confirmation = in.readLine();
                    if (!"Game start received".equals(confirmation)) {
                        System.out.println("Failed to receive confirmation from user: " + user.getUsername());
                    }
                } else {
                    System.out.println("Socket is closed for user: " + user.getUsername());
                }
            } catch (IOException e) {
                System.out.println("Failed to send 'Game start!' to user: " + user.getUsername());
                e.printStackTrace();
            }
        }
    }

    private void playRound() {
        Map<User, String> guesses = new HashMap<>();

        for (User user : users) {
            try {
                if (!user.getSocket().isClosed()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(user.getSocket().getInputStream()));
                    PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);

                    out.println("Make your guess (cara/coroa):");
                    out.flush();
                    String guess = in.readLine().trim().toLowerCase();

                    if (guess.equals("cara") || guess.equals("coroa")) {
                        guesses.put(user, guess);
                    } else {
                        out.println("Invalid guess, please enter 'cara' or 'coroa'.");
                        out.flush();
                        guess = in.readLine().trim().toLowerCase();
                        guesses.put(user, guess);
                    }
                } else {
                    System.out.println("Socket is closed for user: " + user.getUsername());
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
                if (!user.getSocket().isClosed()) {
                    PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
                    if (guess.equals(result)) {
                        lock.lock();
                        try {
                            scores.put(user, scores.get(user) + 10);
                        } finally {
                            lock.unlock();
                        }
                        out.println("Correct! The coin landed on " + result + ". Your score: " + scores.get(user));
                    } else {
                        scores.put(user, scores.get(user) - 10);
                        out.println("Incorrect! The coin landed on " + result + ". Your score: " + scores.get(user));
                    }
                    out.flush();
                } else {
                    System.out.println("Socket is closed for user: " + user.getUsername());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void endGame() {
        if (rankMode) {
            for (Map.Entry<User, Integer> entry : scores.entrySet()) {
                User player = entry.getKey();
                int score = entry.getValue();
                player.setElo(player.getElo() + score);
            }
        }
        for (User user : users) {
            try {
                if (!user.getSocket().isClosed()) {
                    PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
                    out.println("Game over! Final scores:");
                    for (Map.Entry<User, Integer> entry : scores.entrySet()) {
                        User player = entry.getKey();
                        int score = entry.getValue();
                        out.println("Player " + player.getUsername() + ": " + score);
                        if(rankMode) out.println("Player " + player.getUsername() + " new elo: " + player.getElo());
                    }
                    out.flush();
                    user.getSocket().close();
                } else {
                    System.out.println("Socket is closed for user: " + user.getUsername());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}