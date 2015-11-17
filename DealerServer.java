/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DealerServer {
    private ServerSocket server;
    private int currentPlayer;
    
    public DealerServer()
    {
      
        try {
            server = new ServerSocket(5000, 2);
        } catch (IOException ex) {
            Logger.getLogger(DealerServer.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
    
}