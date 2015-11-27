/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack2.Shared;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
/**
 *
 * @author Scott
 */
public class CardDeck implements Serializable 
{
    Card card;
    static ArrayList<Card> cards;
    private static CardDeck CardListInstance = null;
    private final int MAX_CARDS = 52;
    
    private CardDeck()
    {
        cards = new ArrayList<>(MAX_CARDS); 
        for(int i=1; i<4; i++)
        {
            for(int j=1; j<MAX_CARDS; j++)
            {
                if(j == 1)
                {
                    cards.add(new Card('A'));
                }
                else if (j==10)
                {
                    cards.add(new Card('T'));
                }
                else if (j==11)
                {
                    cards.add(new Card('J'));
                }
                else if (j==12)
                {
                    cards.add(new Card('Q'));
                }
                else if (j == 13)
                {
                    cards.add(new Card('K'));
                }
                else
                {
                    cards.add(new Card((char)(48+j)));
                }
            
            }
            
        }
       
        
    }
    
   public static CardDeck CardListInstance()
   {
       if(CardListInstance == null)
       {
           return CardListInstance = new CardDeck();
       }
       return CardListInstance;
   }
   
   
   public void shuffle()
   {
       Collections.shuffle(cards);
       Collections.shuffle(cards);
   }
    
}
