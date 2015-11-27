/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack.Server;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;


/**
 *
 * @author Scott
 */
public class DealerForm extends javax.swing.JFrame {

    
    public static boolean gameStarted;
    public static boolean startGameButton = false;
    public static int playerCounts = 0;
    public SwingWorker<Void, String> shuffleDeckWorker;
    
    
    public static void updatePlayerCount() 
    {
        SwingWorker<Void, Void> updatePlayerCountWorker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                playerCounts++;
                playerCount.setText(Integer.toString(playerCounts));
                return null;
            }
            
        };
        updatePlayerCountWorker.execute();
    }
    /**
     * Creates new form DealerForm
     */
    public DealerForm() 
    {
        setTitle("Dealer");
        initComponents();
        initDealer();
        
        
    }
    
    
    public static boolean getStartGame()
    {
        return startGameButton;
    }
    private void initDealer()
    {
        
        SwingWorker<Void, String> dealerWorker = new SwingWorker<Void, String>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                ServerSocket servsocket = null;
                    try
                    {
                        servsocket = new ServerSocket(7776);

                    }
                    catch(IOException ex)
                    {
                        
                        DealerTextArea.append(ex.toString());

                        ex.printStackTrace();
                    }
                    gameStarted = false;
                    DealerController dealer = new DealerController();
                    StartGame();

                    while(gameStarted == false)
                    {

                        Socket socket = null;
                        try
                        {
                            socket = servsocket.accept();
                            System.out.println("DEBUG: Player Joined");

                        }
                        catch(IOException ex)
                        {
                            publish("Player disconnected \n");
                            //DealerForm.appendText("Player Disconnected\n");
                            System.out.println("DEBUG: Player Left");
                            ex.printStackTrace();
                        }

                        PlayerController player = new PlayerController(socket, dealer);

                        dealer.addPlayer(player);
                        publish("Player: " + player.toString() + "Connected \n");
                        if(dealer.players.isEmpty() == false)
                        {
                            gameStarted =true;
                        }

                        //DealerForm.appendText("Player: " + player.toString() + "Connected \n");

                    }
                    return null;
            }

            @Override
            protected void process(List<String> chunks) 
            {
                for(String text : chunks)
                {
                    DealerTextArea.append(text);
                }
            }
            
            
        };
        dealerWorker.execute();
                
          
    }
    
    private void shuffleDeck()
    {
        shuffleDeckWorker = new SwingWorker<Void, String>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                DealerController.deck = new ArrayList<Card>();
                for(CardList thisCard : CardList.values())
                {
                    DealerController.card = new Card(thisCard);
                    DealerController.deck.add(DealerController.card);
                    DealerController.card = new Card(thisCard);
                    DealerController.deck.add(DealerController.card);
                    DealerController.card = new Card(thisCard);
                    DealerController.deck.add(0, DealerController.card);
                    DealerController.card = new Card(thisCard);
                    DealerController.deck.add(2, DealerController.card);
                }
                publish("Number of cards in current Deck: " + DealerController.deck.size() + "\n");
                //DealerForm.appendText("Number of cards in current Deck:" + deck.size() + "\n");
                return null;
            }

            @Override
            protected void process(List<String> chunks) 
            {
                for(String text : chunks)
                {
                    DealerTextArea.append(text);
                }
                
            }
            
            
        };
        shuffleDeckWorker.execute();      
    }
    
    
     
     
    //Disconnects player
    
    
    private void StartGame()
    {
        SwingWorker<Void, Void> startGameWorker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                DealerController.playerCardValues = new ArrayList<>();
                DealerController.players = new ArrayList<>();

                while (true) 
                {
                    //DealerForm.appendDealerBox("Press Start to begin game! \n");
                    if (DealerController.startGame == false)
                    {

                        System.out.println("DEBUG: I am inside IF Statement");
                        break;
                    }
                    else
                    {
                        System.out.println("DEBUG: Test entered Else");

                        for (PlayerController pl : DealerController.players) 
                        {
                            pl.sendMessage("START");
                            System.out.println("DEBUG: I sent start");
                        }

                        gameStarted = true;
                        DealerController.iterator = DealerController.players.iterator();

                        while (DealerController.iterator.hasNext()) 
                        {
                            PlayerController pl = DealerController.iterator.next();
                            pl.sendMessage("BET");
                            pl.setBet(DealerController.iterator);
                        }
                        shuffleDeck();
                        shuffleDeckWorker.cancel(true);
                        DealerController.iterator = DealerController.players.iterator();

                        while (DealerController.iterator.hasNext()) 
                        {
                            PlayerController pl = DealerController.iterator.next();
                            pl.sendCard();
                            pl.sendCard();
                        }
                        DealerController.iterator = DealerController.players.iterator();

                        while (DealerController.iterator.hasNext()) 
                        {
                            PlayerController pl = DealerController.iterator.next();
                            pl.sendMessage("DECIDE");
                            pl.getDecision(DealerController.iterator);
                        }

                        for (PlayerController pl : DealerController.players) 
                        {
                            if (pl.getPlayerCardValue() > 21) 
                            {
                                pl.sendMessage("LOSE");
                                pl.sendPlayerCount(DealerController.players.size());
                            } 
                            else 
                            {
                                DealerController.playerCardValues.add(pl.getPlayerCardValue());
                            }
                        }

                        Collections.sort(DealerController.playerCardValues);
                        for (PlayerController pl : DealerController.players) 
                        {
                            if (pl.getPlayerCardValue() == DealerController.playerCardValues.get(DealerController.playerCardValues.size() - 1)) 
                            {
                                pl.sendMessage("WIN");
                                pl.sendPlayerCount(DealerController.players.size());
                            }
                            else 
                            {
                                pl.sendMessage("LOSE");
                                pl.sendPlayerCount(DealerController.players.size());
                            }
                        }
                        for (PlayerController pl:DealerController.players)
                        {
                            pl.newGame();
                        }

                        gameStarted = false;
                        DealerController.playerCardValues = new ArrayList<>();
                    }

        
            }
            return null;
        }
    };
        startGameWorker.execute();
    }
    

    
    public static void disableStartGameButton()
    {
        StartGameButton.setVisible(false);
    }
    public void enableStartGameButton()
    {
        StartGameButton.setVisible(true);
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
        startGameButton=true;
        DealerController.startGame = true;
        StartGame();
        StartGameButton.setVisible(false);
        
        
        
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
        
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            
            public void run() {
                new DealerForm().setVisible(true);
                
            }
        });
        
        
        }
        

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JTextArea DealerTextArea;
    public static javax.swing.JButton StartGameButton;
    private javax.swing.JScrollPane jScrollPane1;
    public static javax.swing.JLabel playerCount;
    private javax.swing.JLabel playerCountLabel;
    // End of variables declaration//GEN-END:variables
}
