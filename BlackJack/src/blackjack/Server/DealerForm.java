/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author Scott
 */
public class DealerForm extends javax.swing.JFrame {

    public boolean gameStarted = false;
    public boolean startGameButton = false;
    /**
     * Creates new form DealerForm
     */
    public DealerForm() 
    {
        initComponents();
        ServerSocket servsocket = null;
        try
        {
            servsocket = new ServerSocket(7676);
        }
        catch(IOException ex)
        {
            DealerTextArea.append(ex.toString());
            ex.printStackTrace();
        }
        gameStarted = false;
        DealerController dealer = new DealerController();
        new Thread(dealer).start();
        while(!gameStarted)
        {
            Socket socket = null;
            try
            {
                socket = servsocket.accept();
            }
            catch(IOException ex)
            {
                DealerTextArea.append("Player Disconnected");
                ex.printStackTrace();
            }
            
            PlayerController player = new PlayerController(socket, dealer);
            dealer.addPlayer(player);
        }
                
                    
                
    }
    
    
    
    
    public void disableStartGameButton()
    {
        StartGameButton.setVisible(false);
    }
    public void enableStartGameButton()
    {
        StartGameButton.setVisible(true);
    }
    
    
    
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
        DealerTextArea.append("Number of cards in current Deck: " + deck.size() );
    }
    
    //if less than 5 players, allows the player to join.
    public void addPlayer(PlayerController player)
    {
        if(players.size() < 5)
        {
            players.add(player);
            DealerTextArea.append("Player joined the game");
        }
        else
        {
            player.sendMessage("Sorry, too many players or game has already started");
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
        DealerTextArea.append("Player disconnected");
        iterator.remove();
    }
    
    
   
    @Override
    public void run() 
    {
        playerCardValues = new ArrayList<>();
        players = new ArrayList<>();
        while(true)
        {
            DealerTextArea.append("Click start to start the game");
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
            
            gameStarted = false;
            playerCardValues = new ArrayList<>();
            anyMessage = "";
                
            
        }
        
    }
    
    
    
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        DealerTextArea = new javax.swing.JTextArea();
        playerCountLabel = new javax.swing.JLabel();
        playerCount = new javax.swing.JLabel();
        StartGameButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        DealerTextArea.setColumns(20);
        DealerTextArea.setRows(5);
        jScrollPane1.setViewportView(DealerTextArea);

        playerCountLabel.setText("Player Count:");

        playerCount.setText("0");

        StartGameButton.setText("Start Game");
        StartGameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartGameButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(playerCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(playerCount)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 395, Short.MAX_VALUE)
                .addComponent(StartGameButton)
                .addGap(66, 66, 66))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(playerCountLabel)
                        .addComponent(playerCount))
                    .addComponent(StartGameButton))
                .addContainerGap(77, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void StartGameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartGameButtonActionPerformed
        startGameButton = true;
    }//GEN-LAST:event_StartGameButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DealerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DealerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DealerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DealerForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new DealerForm().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea DealerTextArea;
    private javax.swing.JButton StartGameButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel playerCount;
    private javax.swing.JLabel playerCountLabel;
    // End of variables declaration//GEN-END:variables
}
