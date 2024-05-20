import java.io.*;
import java.net.Socket;

public class ReplayGame implements Runnable {
    private final User user;

    public ReplayGame(User user) {
        this.user = user;
    }

    @Override
    public void run() {
        try {
            Socket socket = user.getSocket();
            if (!socket.isClosed()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String response = in.readLine();
                if (response.equals("y")) {
                    GameServer.addToWaitingQueue(user);
                    return;
                } else {
                    socket.close();
                }
            } else {
                System.out.println("Socket is closed for user: " + user.getUsername());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
