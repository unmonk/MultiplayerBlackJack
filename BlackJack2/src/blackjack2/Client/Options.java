/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack2.Client;
import blackjack2.Shared.Player;
import static blackjack2.Shared.Player.PLAYERMOVES.HIT;
import static blackjack2.Shared.Player.PLAYERMOVES.STAY;
/**
 *
 * @author Scott
 */
public class Options 
{
    public static boolean isDone = false;
    public static Player.PLAYERMOVES move;
    public static String chatboxMessage;
    public static boolean messageSent = false;
    
    public static void setDoneTrue()
    {
        isDone = true;
    }
    
    public static void setDoneFalse()
    {
        isDone = false;
    }
    
    public static boolean getDone()
    {
        return isDone;
    }
    
    public static void setMoveHit()
    {
        move = HIT;
    }
    
    public static void setMoveStay()
    {
        move = STAY;
    }
    
    public static Player.PLAYERMOVES getMove()
    {
        return move;
    }
    
    public static void setMessage(String message)
    {
        chatboxMessage = message;
    }
    
    public static String getMessage()
    {
        return chatboxMessage;
    }
    
    public static boolean getMessageSent()
    {
        return messageSent;
    }
    
    public static void setSentTrue()
    {
        messageSent = true;
    }
    
    public static void setSentFalse()
    {
        messageSent = false;
    }
    
    
    
}
