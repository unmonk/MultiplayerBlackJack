/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack.Server;
import blackjack.Server.DealerForm.DealerController;
import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.concurrent.*;
/**
 *
 * @author Scott
 */
//Class talks to the player clientform.
public class PlayerController 
{
    private volatile InputStream inStream;
    private volatile OutputStream outStream;
    private DataOutputStream dataOutStream;
    private DataInputStream dataInStream;
    private Socket socket;
    private volatile int bet;
    private int playerCardValue;
    private ObjectOutputStream objectStream;
    private DealerController dealer;
    private volatile String decision;
    private volatile boolean endGame = false;
    volatile boolean threadInterrupted = false;
    Thread thread;
    
    
    
    public PlayerController(Socket socket, DealerController dealer)
    {
        this.socket=socket;
        this.dealer = dealer;
        
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
        dataOutStream = new DataOutputStream(outStream);
        try
        {
            dataOutStream.writeUTF(message);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void sendPlayerCount(int count)
    {
        try
        {
            outStream.write(count);
            outStream.flush();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void disconnectPlayer(Iterator iterator)
    {
        dealer.removePlayer(iterator);
        try
        {
            socket.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        if(thread != null)
        {
            thread.interrupt();
            threadInterrupted = true;
        }
    }
    
    
    
    public void setBet(Iterator iterator)
    {
        thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    threadInterrupted = false;
                    bet = inStream.read();
                }
                catch(IOException ex)
                {
                    System.out.println("Disconnected because AFK");
                    disconnectPlayer(iterator);
                }
            }
        });
        
        thread.start();
        
        for(int i=0; i<40; i++)
        {
            if(bet == 0 && !threadInterrupted)
                try
                {
                    thread.sleep(1000);
                }
            catch(InterruptedException ex)
            {
                ex.printStackTrace();
            }
        }
        
        if(bet == 0 && !threadInterrupted)
        {
            disconnectPlayer(iterator);
        }
        System.out.println("Bet placed was" + bet);
    }
    
    public int getBet()
    {
        return bet;
    }
    
    public void sendCard()
    {
        Card sendCard = dealer.dealCard();
        
        try
        {
            objectStream = new ObjectOutputStream(outStream);
            objectStream.writeObject(sendCard);
            playerCardValue = playerCardValue + sendCard.getValue();
            if(playerCardValue > 21)
            {
                endGame();
            }
            
        }
        catch(IOException ex)
        {
            System.out.println("Failed to send card");
            ex.printStackTrace();
        }
    }
    
    public void getDecision(Iterator iterator)
    {
        while(!endGame)
        {
            dataInStream = new DataInputStream(inStream);
            decision = "";
            threadInterrupted = false;
            thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        decision = dataInStream.readUTF();
                    }
                    catch(Exception ex)
                    {
                        System.out.println("Player AFK, Disconnected (40 seconds)");
                        ex.printStackTrace();
                    }
                }
                
            });
            
            thread.start();
        }
    }
    
}
