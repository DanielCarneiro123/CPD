import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class GameManager implements Runnable {
    private List<User> users;
    private final Random random = new Random();
    private final ReentrantLock lock = new ReentrantLock();
    private boolean rankMode;

    private Game game;

    public GameManager(List<User> users, boolean mode) {
        this.users = users;
        this.rankMode = mode;
        this.game = new Game();
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        System.out.println("Starting game with " + users.size() + " players");
        notifyGameStart();
        gameLoop();
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

    private void gameLoop() {
        while (!game.isGameOver()) {
            String boardDisplay = game.getBoardDisplay();
            for (User user : users) {
                try {
                    if (!user.getSocket().isClosed()) {
                        PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);

                        out.println(boardDisplay);
                    } else {
                        System.out.println("Socket is closed for user: " + user.getUsername());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            User currentPlayer = game.isWhiteTurn() ? users.get(0) : users.get(1);

            try {
                if (!currentPlayer.getSocket().isClosed()) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(currentPlayer.getSocket().getInputStream()));
                    PrintWriter out = new PrintWriter(currentPlayer.getSocket().getOutputStream(), true);

                    while (true) {
                        out.println("Your turn!\nMake your move in algebraic chess notation (e.g. a2, cxd5):");
                        out.flush();
                        String move = in.readLine().trim().toLowerCase();

                        if (!game.validateAndExecuteMove(move)) {
                            out.println("Invalid move, please try again.");
                            out.flush();
                            continue;
                        }
                        break;
                    }

                } else {
                    System.out.println("Socket is closed for user: " + currentPlayer.getUsername());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String boardDisplay = game.getBoardDisplay();
        for (User user : users) {
            try {
                if (!user.getSocket().isClosed()) {
                    PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);

                    out.println(boardDisplay);
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
        User winner = game.isWhiteTurn() ? users.get(1) : users.get(0);
        User loser = game.isWhiteTurn() ? users.get(0) : users.get(1);
        if (rankMode) {
            double winnerElo = winner.getElo();
            double loserElo = loser.getElo();
            winner.setElo((int) calculateEloChange(winnerElo, loserElo, 1));
            loser.setElo((int) calculateEloChange(loserElo, winnerElo, 0));


            DatabaseConnection db = new DatabaseConnection();
            db.updateDB();
        }
        for (User user : users) {
            try {
                if (!user.getSocket().isClosed()) {
                    PrintWriter out = new PrintWriter(user.getSocket().getOutputStream(), true);
                    out.println("Game over!\nWinner: " + winner.getUsername() + "\nLoser: " + loser.getUsername());
                    out.println("Your new elo: " + user.getElo());


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