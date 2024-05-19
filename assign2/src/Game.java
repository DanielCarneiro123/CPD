import java.io.*;
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

            while (true) {
                try {
                    if (!user.getSocket().isClosed()) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(user.getSocket().getInputStream()));
                        PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);

                        out.println("Make your guess (cara/coroa):");
                        out.flush();
                        String guess = in.readLine().trim().toLowerCase();

                        if (guess.equals("cara") || guess.equals("coroa")) {
                            guesses.put(user, guess);
                            break;
                        } else {
                            out.println("Invalid guess, please enter 'cara' or 'coroa'.");
                            out.flush();
                        }
                    } else {
                        System.out.println("Socket is closed for user: " + user.getUsername());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    private double calculateEloChange(double playerElo, double opponentElo, double score) {
        double expectedScore = 1 / (1 + Math.pow(10, (opponentElo - playerElo) / 400));
        double newElo = playerElo + 32 * (score - expectedScore);
        return newElo;
    }

    private void endGame() {
        if (rankMode) {
            Map<User, Integer> eloChanges = new HashMap<>();
            for (Map.Entry<User, Integer> entry : scores.entrySet()) {
                User player = entry.getKey();
                int score = entry.getValue();
                if (eloChanges.containsKey(player)) {
                    eloChanges.put(player, eloChanges.get(player) + score);
                } else {
                    eloChanges.put(player, score);
                }
            }

            for (Map.Entry<User, Integer> entry : scores.entrySet()) {
                User player = entry.getKey();
                int score = entry.getValue();
                double playerElo = player.getElo();

                double eloChange = 0;
                for (User opponent : scores.keySet()) {
                    if (!opponent.equals(player)) {
                        double opponentElo = opponent.getElo();
                        eloChange += calculateEloChange(playerElo, opponentElo,
                                score > scores.get(opponent) ? 1 : (score < scores.get(opponent) ? 0 : 0.5));
                    }
                }

                if (eloChanges.containsKey(player)) {
                    eloChanges.put(player, (int) eloChange);
                } else {
                    eloChanges.put(player, (int) eloChange);
                }
            }
            for (Map.Entry<User, Integer> entry : eloChanges.entrySet()) {
                User player = entry.getKey();
                int eloChange = entry.getValue();
                player.setElo(eloChange);
            }

            DatabaseConnection db = new DatabaseConnection();
            db.updateDB();
        }
        for (User user : users) {
            try {
                if (!user.getSocket().isClosed()) {
                    PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
                    out.println("Game over! Final scores:");
                    for (Map.Entry<User, Integer> entry : scores.entrySet()) {
                        User player = entry.getKey();
                        int score = entry.getValue();

                        if (rankMode)
                            out.println("Player " + player.getUsername() + ": " + score + " Elo: " + player.getElo());
                        else
                            out.println("Player " + player.getUsername() + ": " + score);
                    }
                    out.println("Finish");
                    out.flush();

                    Thread replayGame = new Thread(new ReplayGame(user));
                    replayGame.start();
                } else {
                    System.out.println("Socket is closed for user: " + user.getUsername());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}