
package blackjack.Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Scott
 */


public class DealerController 
{
    public static Card card;
    public static ArrayList<Card> deck;
    volatile static ArrayList<PlayerController> players;
    public static ArrayList<Integer> playerCardValues;
    volatile static Iterator<PlayerController> iterator;
    String anyMessage = "";
    public static boolean startGame = false;
    
    
    public void removePlayer(Iterator iterator)
    {
        
        System.out.println("Player disconnected\n");
        iterator.remove();
    }
    
    public Card dealCard()
    {
        Random random = new Random(System.currentTimeMillis());
        int i = random.nextInt(deck.size());
        card = deck.get(i);
        deck.remove(i);
        return card;
    }
     public void addPlayer(PlayerController player)
    {
        players.add(player);
         
    }
    
}


