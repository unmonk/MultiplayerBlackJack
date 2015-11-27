
package blackjack.Server;
import blackjack.Server.DealerForm;
import java.io.*;
import java.net.*;
import java.util.Iterator;
/**
 *
 * @author Scott
 */
//Class talks to the player clientform.
public class PlayerController 
{
    // inStream/outStream =  Input and Output stream from the socket
    // dataOutStream/dataInStream = Input and Output Stream wrapper for Strings
    // objectStream = Input and Output Stream wrapper for Objects
    // socket = connection socket
    // bet = players current bet
    // dealer = instance of DealerController
    // decision = String recieved from the player HIT or STAY
    // endGame = state of the game, over or not
    // threadInterrupted = true when thread is interrupted
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
    
    
    //Instance creator
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
    
    //Send string to players via writeUTF
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
    
    //Sends players a count of players
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
    
    //Removes player from game
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
    
    
    //gets players bet
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
            if(bet == 0 && threadInterrupted == false)
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
    
    //return PlayerController bet
    public int getBet()
    {
        return bet;
    }
    
    //return PlayerControllers card value
    public int getPlayerCardValue()
    {
        return playerCardValue;
    }
    
    //send a random card to the player, update cardValue
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
    
    //gets players HIT/STAY decisoin in a parallel thread
    public void getDecision(Iterator iterator)
    {
        while(endGame == false)
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
            for(int i=0; i<40; i++)
            {
                if(decision.equals("") && threadInterrupted == false)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            System.out.println("Current player decided to: " + decision);
            if(decision.equals("") && threadInterrupted == false)
            {
                disconnectPlayer(iterator);
            }
            
            switch(decision)
            {
                case "HIT":
                    sendCard();
                    break;
                case "STAY":
                    endGame();
                    break;
            }
            
        }
    }
    
    //sets endgame to true
    public void endGame()
    {
        endGame = true;
    }
    
    //creates a new game, resets players info and resets socket connection
    public void newGame()
    {
        bet = 0;
        playerCardValue = 0;
        decision = "";
        endGame = false;
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
    
}