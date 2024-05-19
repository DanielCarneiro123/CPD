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
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String response = in.readLine();
            if (response.equals("y")) {
                GameServer.addToWaitingQueue(user);
                return;
            } else {                                                
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
