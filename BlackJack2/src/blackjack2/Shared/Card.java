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
public class Card implements Serializable
{
    private char cardLetter;
    
    
    public Card(char card)
    {
        cardLetter = card;
    }
    
    public int getValue(int HandTotal)
    {
        switch(cardLetter)
        {
            case 'A':
                if(HandTotal > 10)
                {
                    return 1;
                }
                else
                {
                    return 11;
                }
            case '2':
                return 2;
            case '3':
                return 3;
            case '4':
                return 4;
            case '5':
                return 5;
            case '6':
                return 6;
            case '7':
                return 7;
            case '8':
                return 8;
            case '9':
                return 9;
            case 'T':
                return 10;
            case 'J':
                return 11;
            case 'Q':
                return 12;
            case 'K':
                return 13;
            default:
                return -1;
        }
    }
   
   
    public char getCardName()
    {
            return cardLetter;
    }
}


   

