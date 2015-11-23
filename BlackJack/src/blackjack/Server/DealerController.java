/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack.Server;
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
    public boolean startGame = false;

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
        DealerForm.appendText("Number of cards in current Deck:" + deck.size() + "\n");
        
    }
    
    //if less than 5 players, allows the player to join.
    public void addPlayer(PlayerController player)
    {
        if(players.size() < 5)
        {
            players.add(player);
            DealerForm.appendText("Player joined the game\n");
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
        
        DealerForm.appendText("Player disconnected\n");
        iterator.remove();
    }
    
   
   
    
   
   @Override
    public void run()
    {
        playerCardValues = new ArrayList<>();
        players = new ArrayList<>();
        
        while (true) 
        {
            //DealerForm.appendDealerBox("Press Start to begin game! \n");
            if (startGame == false)
            {
                
                System.out.println("DEBUG: I am inside IF Statement");
                return;
            }
            else
            {
                System.out.println("DEBUG: Test entered Else");
            
                for (PlayerController pl : players) 
                {
                    pl.sendMessage("START");
                    System.out.println("DEBUG: I sent start");
                }

                DealerForm.gameStarted = true;
                iterator = players.iterator();

                while (iterator.hasNext()) 
                {
                    PlayerController pl = iterator.next();
                    pl.sendMessage("BET");
                    pl.setBet(iterator);
                }
                shuffleDeck();
                iterator = players.iterator();

                while (iterator.hasNext()) 
                {
                    PlayerController pl = iterator.next();
                    pl.sendCard();
                    pl.sendCard();
                }
                iterator = players.iterator();

                while (iterator.hasNext()) 
                {
                    PlayerController pl = iterator.next();
                    pl.sendMessage("DECIDE");
                    pl.getDecision(iterator);
                }

                for (PlayerController pl : players) 
                {
                    if (pl.getPlayerCardValue() > 21) 
                    {
                        pl.sendMessage("LOSE");
                        pl.sendPlayerCount(players.size());
                    } 
                    else 
                    {
                        playerCardValues.add(pl.getPlayerCardValue());
                    }
                }

                Collections.sort(playerCardValues);
                for (PlayerController pl : players) 
                {
                    if (pl.getPlayerCardValue() == playerCardValues.get(playerCardValues.size() - 1)) 
                    {
                        pl.sendMessage("WIN");
                        pl.sendPlayerCount(players.size());
                    }
                    else 
                    {
                        pl.sendMessage("LOSE");
                        pl.sendPlayerCount(players.size());
                    }
                }
                for (PlayerController pl:players)
                {
                    pl.newGame();
                }

                DealerForm.gameStarted = false;
                playerCardValues = new ArrayList<>();
            }

        }
    }
}
    

