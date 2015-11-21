/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack.Server;
import static blackjack.Server.DealerForm.DealerTextArea;
import static blackjack.Server.DealerForm.disableStartGameButton;
import static blackjack.Server.DealerForm.gameStarted;
import static blackjack.Server.DealerForm.startGameButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Scott
 */
public class DealerController implements Runnable
{
    Card card;
    private ArrayList<Card> deck;
    volatile ArrayList<PlayerController> players;
    ArrayList<Integer> playerCardValues;
    volatile Iterator<PlayerController> iterator;
    String anyMessage = "";

    //Fills deck with each type of card from enum CardList four times to represent a 52 card deck
    private void shuffleDeck()
    {
        deck = new ArrayList<Card>();
        for(CardList thisCard:CardList.values())
        {
            card = new Card(thisCard);
            deck.add(card);
            card = new Card(thisCard);
            deck.add(card);
            card = new Card(thisCard);
            deck.add(0, card);
            card = new Card(thisCard);
            deck.add(2, card);
        }
        DealerTextArea.append("Number of cards in current Deck: " + deck.size() +"\n" );
    }
    
    //if less than 5 players, allows the player to join.
    public void addPlayer(PlayerController player)
    {
        if(players.size() < 5)
        {
            players.add(player);
            DealerTextArea.append("Player joined the game\n");
        }
        else
        {
            player.sendMessage("Sorry, too many players or game has already started\n");
        }
    }
    
    //gives player random card
    public Card dealCard()
    {
        Random random = new Random(System.currentTimeMillis());
        int i = random.nextInt(deck.size());
        card = deck.get(i);
        deck.remove(i);
        return card;
    }
    
    //Disconnects player
    public void removePlayer(Iterator iterator)
    {
        
        DealerTextArea.append("Player disconnected\n");
        iterator.remove();
    }
    
    
   
    @Override
    public void run() 
    {
        playerCardValues = new ArrayList<>();
        players = new ArrayList<>();
        while(true)
        {
            DealerTextArea.append("Click start to start the game\n");
            if(!startGameButton)
            {
                return;
            }
            for(PlayerController p1 : players)
            {
                p1.sendMessage("START");
            }
            startGameButton=false;
            disableStartGameButton();
            
            gameStarted = true;
            
            iterator = players.iterator();
            while(iterator.hasNext())
            {
                PlayerController p1 = iterator.next();
                p1.sendMessage("BET");
                p1.setBet(iterator);
            }
            
            shuffleDeck();
            iterator = players.iterator();
            while(iterator.hasNext())
            {
                PlayerController p1 = iterator.next();
                p1.sendCard();
                p1.sendCard();
            }
            
            iterator = players.iterator();
            while(iterator.hasNext())
            {
                PlayerController p1 = iterator.next();
                p1.sendMessage("DECIDE");
                p1.getDecision(iterator);
            }
            
            for (PlayerController p1 : players)
            {
                if(p1.getPlayerCardValue() > 21)
                {
                   anyMessage = "You LOSE";
                   DealerTextArea.append("Player: " + anyMessage);
                   p1.sendMessage(anyMessage);
                   p1.sendPlayerCount(players.size());
                }
                else
                {
                    playerCardValues.add(p1.getPlayerCardValue());
                }
            }
            
            Collections.sort(playerCardValues);
            for(PlayerController p1 : players)
            {
                if(p1.getPlayerCardValue() == playerCardValues.get(playerCardValues.size() - 1))
                {
                    anyMessage = "You WIN";
                    DealerTextArea.append("Player: "+ anyMessage);
                    p1.sendMessage(anyMessage);
                    p1.sendPlayerCount(players.size());
                }
                else
                {
                   anyMessage = "You LOSE";
                   DealerTextArea.append("Player: " + anyMessage);
                   p1.sendMessage(anyMessage);
                   p1.sendPlayerCount(players.size());
                }
            }
            
            for(PlayerController p1 : players)
            {
                p1.newGame();
            }
            
            DealerForm.gameStarted = false;
            playerCardValues = new ArrayList<>();
            anyMessage = "";
                
            
        }
        
    }
    
    
    
}