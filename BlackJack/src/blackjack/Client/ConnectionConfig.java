/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack.Client;

import java.io.*;
import java.net.*;
/**
 *
 * @author Scott
 */
public class ConnectionConfig 
{
    private Socket socket;
    private InputStream inStream;
    private OutputStream outStream;
    
    public void connect()
    {
        try
        {
            socket = new Socket("localhost", 7776);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
     public OutputStream getOutputStream()
    {
        try
        {
            outStream = socket.getOutputStream();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return outStream;
    }
    
    public InputStream getInputStream()
    {
        try
        {
            inStream = socket.getInputStream();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return inStream;
    }
    
   
    
}
