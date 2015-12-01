/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackestjack.Shared;
import java.util.*;
import blackestjack.Shared.Card;
/* 
 * @author Scott
 * @author Jessica
 * CS 412 Project
 * 2 December 2015
 */
public final class Deck 
{
    private ArrayList<Card> deckCards = new ArrayList<>();
    private int cardCount;
    
    public Deck()
    {
       cardCount = 52;
       int count = 0;
       for(int i=0; i<4; i++)
       {
           for(int j=1; j<13; j++)
           {
               deckCards.add(count, new Card(j));
               count++;
           }
       }
        shuffle();
    }
    
    public void shuffle()
    {
        Collections.shuffle(deckCards);
    }
    
    public Card dealOne()
    {
        try
        {
            return deckCards.remove(0);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
    
    public int deckSize()
    {
        return deckCards.size();
    }
    
    public boolean deckEmpty()
    {
        return deckCards.isEmpty();
    }
    
}
