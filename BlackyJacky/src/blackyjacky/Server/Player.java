/*
 * Copyright (C) 2015 Scott
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package blackyjacky.Server;
import java.io.*;
import java.net.*;
import java.util.*;
/**
 *
 * @author Scott
 */
public class Player 
{
    private volatile InputStream inStream;
    private volatile OutputStream outStream;
    private DataOutputStream dataOut;
    private DataInputStream dataIn;
    private Socket socket;
    private int bet;
    
    volatile boolean Interrupted = false;
    Thread playerThread;
    
    public Player(Socket sock)
    {
        socket = sock;
        try
        {
            inStream = socket.getInputStream();
            outStream = socket.getOutputStream();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        
    }
    
    public void sendMessage(String message)
    {
        dataOut = new DataOutputStream(outStream);
        try
        {
            dataOut.writeUTF(message);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
     
    }
    public int getBet()
    {
        return bet;
    }
    
    public void setBet(Iterator iterator)
    {
        playerThread = new Thread(new Runnable()
        {
            @Override
            public void run() 
            {
                try
                {
                    Interrupted = false;
                    bet = inStream.read();
                }
                catch(IOException ex)
                {
                    ex.printStackTrace();
                    //disconnectPlayer(iterator);
                }     
            }
        });
        playerThread.start();
//        for(int i=0; i<20; i++)
//        {
//            if(bet == 0 && Interrupted == false)
//            {
//                try
//                {
//                    Thread.sleep(1000);
//                }
//                catch(InterruptedException ex)
//                {
//                    ex.printStackTrace();
//                }
//            }
//        }
//        if(bet == 0 && Interrupted == false)
//        {
//            disconnectPlayer(iterator);
//        }
    }
    
  
}
