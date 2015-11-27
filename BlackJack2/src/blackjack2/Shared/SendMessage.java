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
public class SendMessage implements Serializable 
{
    public final messageType message;
    public final Object messageInfo;
    
    public SendMessage(messageType message, Object messageInfo)
    {
        this.message = message;
        this.messageInfo = messageInfo;
    }
           
            
    public enum messageType
    {
        INTRODUCE, GET_NEXT_MOVE, HIT, DOUBLE, STAY,
        INTRODUCTION_ACCEPTED, DEALTWO, DEALONE, RESULT, STAY_OK, ENDGAME, CHAT
    }
           
    
}

