/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack2.Shared;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
/**
 *
 * @author Scott
 */
public class Player implements Serializable
{
    private final ArrayList<Card> playerCards;
    private final String playerName;
    public boolean stay;
    public boolean bust;
    
    //Player Constructor
    public Player(String name, int money)
    {
        playerName= name;
        playerCards = new ArrayList<>(2);
        stay = false;
        bust = false;
    }
    
    //Overload for getting multiple cards
    public void getCards(Card[] cards)
    {
        Collections.addAll(playerCards, cards);
        if(getHandValue() > 21)
        {
            bust = true;
            stay = true;
        }
    }
    
    //Overload for getting a single card
    public void getCards(Card card)
    {
        playerCards.add(card);
        if(getHandValue() > 21)
        {
            bust = true;
            stay = true;
        }
    }
    
    //Returns the int value of the hand
    public int getHandValue()
    {
        int handValue = 0;
        checkAces();
        for(Card card : playerCards)
        {
            handValue += card.getValue(handValue);
        }
        return handValue;
        
    }
    
    //Gets ACES and adds to end of list
    public void checkAces()
    {
        ArrayList<Card> aces = new ArrayList<>();
        for(int i=0; i<playerCards.size(); i++)
        {
            if(playerCards.get(i).getCardName() =='A')
            {
             Card ace = playerCards.remove(i);
            }
        }
        for(Card ace : aces)
        {
            playerCards.add(ace);
        }
    }
    
    //Is player allowed to hit?
    public boolean CanHit()
    {
        if(stay==false && bust==false)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public String toString()
    {
        String PlayerString = "Player: " + playerName;
        if(bust == true)
        {
            PlayerString += "Busted";
        }
        return PlayerString;
        
    }
    
    //Player Options
    public enum PLAYERMOVES
    {
        HIT, DOUBLE, STAY
    }
   
    
    
    
}
