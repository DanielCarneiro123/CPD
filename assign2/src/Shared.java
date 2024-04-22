import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class Shared {
 
    private ReentrantLock lock;
    public Shared() {
        this.lock = new ReentrantLock();
    }
}