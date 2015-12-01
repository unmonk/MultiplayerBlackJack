/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackestjack.Shared;

/**
 *
 * @author Scott
 */
public final class Card 
{
    private int value;
    public Card(int value)
    {
       this.value = value;
    }

    public int getValue()
    {
        return value;
    }
    
    @Override
    public String toString()
    {
        switch(value)
        {
            case 1:
                return "ACE";
            case 11:
                return "JACK";
            case 12:
                return "QUEEN";
            case 13:
                return "KING";
            default:
                return (Integer.toString(value));
        }
    }
    
}
