/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack2.Dealer;

/**
 *
 * @author Scott
 */
public class DealerOptions 
{
    public static boolean isDone = false;
    public static int playerCount;
    
    public static void setPlayers(int players)
    {
        playerCount = players;
    }
    
    public static int getPlayers()
    {
        return playerCount;
    }
    
    
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
    
    
}
