/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackestjack.Shared;
import java.util.*;
/**
 *
 * @author Scott
 */
public class Player
{
    private String username;
    private ArrayList<Card> playerCards = new ArrayList<Card>();
    private int playerCardsValue;
    private int cardCount;
            
    public Player(String name)
    {
        username = name;
        foldCards();
    }
    
    private void foldCards()
    {
        playerCards.clear();
    }
    
    public boolean getCard(Card card)
    {
        playerCards.add(cardCount,card);
        cardCount++;
        return getCardTotal() <= 21;
    }
    
    public int getCardTotal()
    {
        int aces = 0;
        int total = 0;
        int card;
        
        for(int i=0; i<cardCount; i++)
        {
            card = playerCards.get(i).getValue();
            if(card == 1)
            {
                aces++;
                total +=11;
            }
            else if(card > 10)
            {
                total += 10;
            }
            else
            {
                total += card;
            }
        }
        
        while(total > 21 && aces > 0)
        {
            total -= 10;
            aces--;
        }
        return total;
    }
    
    public String playerCardsInfo(boolean showCard)
    {
        String info = username + " cards: + ";
        for(int i=0; i<cardCount; i++)
        {
            if(i==0 && showCard == false)
            {
                info += "Hidden Card";
            }
            else
            {
                info += playerCards.get(i).toString() + " + ";
            }
        }
        info += "\n";
        return info;
    }
    
    
}
