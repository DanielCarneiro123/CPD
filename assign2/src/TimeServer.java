import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TimeServer {

    private static final ReentrantLock serverLock = new ReentrantLock();
    private static final int TEAM_SIZE = 2;
    private static final List<User> waitingQueue = new ArrayList<>();
    private static boolean rankMode = false;

    public static void main(String[] args) {
        if (args.length < 1)
            return;

        int port = Integer.parseInt(args[0]);
        // rankMode = args.length > 1 && args[1].equalsIgnoreCase("rank");

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                Authentication auth = new Authentication(socket);
                Thread thread = new Thread(auth);
                thread.start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void addToWaitingQueue(User user) {
        serverLock.lock();
        try {
            waitingQueue.add(user);
            System.out.println(waitingQueue.size());
            if (waitingQueue.size() == TEAM_SIZE) {
                List<User> team;
                if (rankMode) {
                    team = getBalancedOpp();
                } else {
                    team = new ArrayList<>(waitingQueue.subList(0, TEAM_SIZE));
                }
                waitingQueue.removeAll(team);
                Thread game = new Thread(new Game(team));
                game.start();
                try {
                    game.join();
                } catch (InterruptedException e) {
                    System.err.println("Game thread interrupted: " + e.getMessage());
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            serverLock.unlock();
        }
    }

    private static List<User> getBalancedOpp() {
        waitingQueue.sort(Comparator.comparingInt(User::getElo));
        List<User> team = new ArrayList<>();
        int maxDifference = 200;

        for (int i = 0; i <= waitingQueue.size() - TEAM_SIZE; i++) {
            int minLevel = waitingQueue.get(i).getElo();
            int maxLevel = waitingQueue.get(i + TEAM_SIZE - 1).getElo();
            if (maxLevel - minLevel <= maxDifference) {
                team = new ArrayList<>(waitingQueue.subList(i, i + TEAM_SIZE));
                break;
            }
        }
        if (team.isEmpty()) {

            team = new ArrayList<>(waitingQueue.subList(0, TEAM_SIZE));
        }
        return team;
    }
}
