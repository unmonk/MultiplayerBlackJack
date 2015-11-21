/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack.Server;

import java.io.Serializable;
/**
 *
 * @author Scott
 */
public class Card implements Serializable
{
    private CardList card;
    private int value;
    
    public Card(CardList card)
    {
        switch(card)
        {
            case TWO:
                value=2;
                break;
            case THREE:
                value=3;
                break;
            case FOUR:
                value=4;
                break;
            case FIVE:
                value=5;
                break;
            case SIX:
                value=6;
                break;
            case SEVEN:
                value=7;
                break;
            case EIGHT:
                value=8;
                break;
            case NINE:
                value=9;
                break;
            case TEN:
                value=10;
                break;
            case JACK:
                value=10;
                break;
            case QUEEN:
                value=10;
                break;
            case KING:
                value=10;
                break;
            case ACE:
                value=11;
                break;
        }
    }
    public int getValue()
    {
        return value;
    }
    
    public CardList getCardName()
    {
            return card;
    }
}

enum CardList 
{
 TWO,
 THREE,
 FOUR,
 FIVE,
 SIX,
 SEVEN,
 EIGHT,
 NINE,
 TEN,
 JACK,
 QUEEN,
 KING,
 ACE
}
