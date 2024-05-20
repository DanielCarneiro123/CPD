import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {

    private static final ReentrantLock serverLock = new ReentrantLock();
    private static final int TEAM_SIZE = 2;
    private static final List<User> waitingQueue = new ArrayList<>();
    private static boolean rankMode = false;
    private static final long START_TIME = System.currentTimeMillis();

    public static void main(String[] args) {
        if (args.length < 1)
            return;

        int port = Integer.parseInt(args[0]);
        rankMode = Integer.parseInt(args[1]) == 0 ? false : true;

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
            boolean userExists = false;
            for(User u : waitingQueue) {
                if(u.equals(user)) 
                    userExists = true;
            }
            if(!userExists)
                waitingQueue.add(user);
            System.out.println(waitingQueue.size());
            if (waitingQueue.size() >= TEAM_SIZE) {
                List<User> team;
                if (rankMode) {
                    team = getBalancedOpp();
                } else {
                    team = new ArrayList<>(waitingQueue.subList(0, TEAM_SIZE));
                }
                waitingQueue.removeAll(team);
                Thread game = new Thread(new GameManager(team, rankMode));
                game.start();
            }
        } finally {
            serverLock.unlock();
        }
    }

    private static int calculateMaxDifference() {
        long elapsedTime = System.currentTimeMillis() - START_TIME;
        int initialDifference = 200;
        int increaseRate = 50; 

        int additionalDifference = (int) (elapsedTime / 10000) * increaseRate;

        return initialDifference + additionalDifference;
    }

    private static List<User> getBalancedOpp() {
        List<User> team = new ArrayList<>();
        while (true){
            waitingQueue.sort(Comparator.comparingInt(User::getElo));
            int maxDifference = calculateMaxDifference();

            for (int i = 0; i <= waitingQueue.size() - TEAM_SIZE; i++) {
                int minLevel = waitingQueue.get(i).getElo();
                int maxLevel = waitingQueue.get(i + TEAM_SIZE - 1).getElo();
                if (Math.abs(maxLevel - minLevel) <= maxDifference) {
                    return (team = new ArrayList<>(waitingQueue.subList(i, i + TEAM_SIZE)));
                }
            }
        }
    }
}
