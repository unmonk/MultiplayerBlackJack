/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack2.Shared;
import java.io.Serializable;
/**
 *
 * @author Scott
 */
public class Dealer extends Player implements Serializable
{
    private static Dealer dealer = null;
    private final CardDeck deck;
    
    private Dealer(String name, int money)
    {
        super("Dealer", 10000000);
        deck = CardDeck.CardListInstance();
        deck.shuffle();
        
    }
    
    public static Dealer getDealerInstance()
    {
        if(dealer == null)
        {
            dealer = new Dealer("Dealer", 10000000);
            return dealer;
        }
        else
        {
            return dealer;
        }
    }
    
    public Card[] DealTwo()
    {
        Card[] returnCards = new Card[]{CardDeck.cards.remove(0), CardDeck.cards.remove(0)};
        return returnCards;
    }
    
    public Card DealOne()
    {
        Card returnCard = CardDeck.cards.remove(0);
        return returnCard;
    }
}
