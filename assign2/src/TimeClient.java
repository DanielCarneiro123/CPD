import java.net.*;
import java.io.*;
 
/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class TimeClient {
 
    public static void main(String[] args) {
        if (args.length < 2) return;
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        try (Socket socket = new Socket(hostname, port)) {
 
            System.out.println("Connected to server. Enter numbers (type 'exit' to finish):");
            
            boolean exit = false;
            
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            while(!exit){

                /* Write number */
                String n = System.console().readLine();
                writer.println(n);
                
                
                /* Read response from server */
                String response = reader.readLine();
                if (n.equals("exit")) {
                    exit = true;
                    System.out.println("Thank you for playing");
                }
            }
            
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

   
}