import java.io.*;
import java.net.*;
 
/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class Calc implements Runnable {
 
    private BufferedReader reader;
    private PrintWriter writer;
    private Shared shared;
    private double localSum;

    public Calc(Socket socket, Shared shared) {
        this.shared = shared;
        this.localSum = 0;

        try {
            InputStream input = socket.getInputStream();
            this.reader = new BufferedReader(new InputStreamReader(input));
    
            OutputStream output = socket.getOutputStream();
            this.writer = new PrintWriter(output, true);
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
       
    }

    public void run() {
        try {

            while (true) {
                /* read input */
                String input = reader.readLine();
                if (input.equals("exit")) {
                    shared.add(localSum);
                   
                    /* Write global sum in Player */
                    writer.println(shared.getGlobal());

                    System.out.println("Global sum after closing this thread: " + shared.getGlobal());

                    return;
                } 
                    
                /* add number */
                int n = Integer.parseInt(input);
                localSum += n;

                /* Write local sum in Player */
                writer.println(localSum);    
            }
           
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
    
}