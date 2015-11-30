/*
 * Copyright (C) 2015 Scott
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package blackyjacky.Shared;

/**
 *
 * @author Scott
 */
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

