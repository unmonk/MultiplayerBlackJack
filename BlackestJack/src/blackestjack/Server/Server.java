/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackestjack.Server;
import blackestjack.Shared.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/* 
 * @author Scott
 * @author Jessica
 * CS 412 Project
 * 2 December 2015
 */
public class Server extends javax.swing.JFrame 
{

    private ServerSocket server;
    private ExecutorService exec = Executors.newFixedThreadPool(10);
    private int maxPlayers;
    private int turnsLeft;
    private int playerCountTest;
    private int connectionCount = 1;
    private Player dealer = new Player("Dealer");
    private Deck deck = new Deck();
    private ArrayList<Player> players = new ArrayList<Player>();
    private DealerServer[] dealerServer = new DealerServer[10];
    
    /**
     * Creates new form Server
     */
    public Server() 
    {
        initComponents();
        hideConnectionButtons();
        showPlayerCountButtons();
    }
    
 
    public void initServer()
    {
        SwingWorker<Void, Void> initServerWorker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                try
                {
                    initConnections();
                    while((connectionCount -1) < maxPlayers)
                    {
                        try
                        {
                            dealerServer[connectionCount] = new DealerServer(connectionCount);
                            dealerServer[connectionCount].waitForPlayers();
                            exec.execute(dealerServer[connectionCount]);
                            System.out.println("Reached execute exec");
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                            showInfo(ex.toString());
                        }
                        finally
                        {
                            ++connectionCount;
                            playerCountLabel.setText(Integer.toString(connectionCount - 1));
                        }
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    showInfo(ex.toString());
                }
                return null;
            }

            @Override
            protected void done() 
            {
                super.done();
                try 
                {
                    super.get();
                } 
                catch (InterruptedException ex) 
                {
                    ex.printStackTrace();
                } 
                catch (ExecutionException ex) 
                {
                    ex.printStackTrace();
                }
            }
            
            
        };
        initServerWorker.execute();
   
    }
    //end initServer
    
    public void initConnections()
    {
        try 
        {
            server = new ServerSocket(6000);
            showInfo("Created Server Socket");
            showInfo(server.toString());
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
            showInfo(ex.toString());
        }
        
    }
    //end initConnections
    
    private class DealerServer implements Runnable
    {
        private Socket socket;
        private ObjectOutputStream output;
        private ObjectInputStream input;
        
        private int connectionID;
        public DealerServer(int conID)
        {
            connectionID = conID;
        }

        @Override
        public void run() 
        {
            try
            {
                System.out.println("Reached DealerServer run()");
                output = new ObjectOutputStream(socket.getOutputStream());
                output.flush();
                input = new ObjectInputStream(socket.getInputStream());
                
                String username = (String)input.readObject();
                System.out.println("DEALER:" + username);
                players.add(new Player(username));
                System.out.println(players);
                //DealerTextArea.append(players.get(connectionID - 1) + " Joined the game");
                //showInfo(players.get(connectionID - 1) + " joined the game");
                
                if(players.size() == maxPlayers)
                {
                    System.out.println("starting game");
                    //showInfo("Starting Game");
                    deal();
                }
                getMessages();
                
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
                showInfo(ex.toString());
            }
            catch(ClassNotFoundException ex)
            {
                ex.printStackTrace();
                showInfo(ex.toString());
            }
            
        }
        //End runnable
    
        private void getMessages()
        {
            String message = "";
            try
            {
                do
                {
                    message = (String)input.readObject();
                    if(message.compareToIgnoreCase("HIT") == 0)
                    {
                        hit();
                    }
                    if(message.compareToIgnoreCase("STAY") == 0)
                    {
                        sendMessage("WAIT");
                        turnsLeft--;
                        checkTurnsLeft();

                    }
                }
                while(true);
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
                showInfo(ex.toString());
            }
            catch(ClassNotFoundException ex)
            {
                ex.printStackTrace();
                showInfo(ex.toString());
            }
        }
        //end getMessage

        private void hit()
        {
            players.get(connectionID - 1).getCard(deck.dealOne());
            dealerServer[connectionID].sendMessage(players.get(connectionID - 1).playerCardsInfo(true));
            
            if(players.get(connectionID - 1).getCardTotal() > 21)
            {
                dealerServer[connectionID].sendMessage(players.get(connectionID - 1).playerCardsInfo(true));
                dealerServer[connectionID].sendMessage("BUST");
                dealerServer[connectionID].sendMessage("Busted With " + players.get(connectionID - 1).getCardTotal() + ". You Lose!");
                turnsLeft--;
            }
            if(turnsLeft == 0)
            {
                dealersTurn();
            }
        }
        //end Hit
        private void deal()
        {
            System.out.println("Dealing");
            for(int i=0; i<2; i++)
            {
                for(int j=0; j<players.size(); j++)
                {
                    players.get(j).getCard(deck.dealOne());
                    System.out.println(players.get(j).playerCardsInfo(true));
                }
                dealer.getCard(deck.dealOne());
            }
            
            for(int i=1; i<players.size(); i++)
            {
                dealerServer[i].sendMessage(dealer.playerCardsInfo(false));
                dealerServer[i].sendMessage(players.get(i-1).playerCardsInfo(true));
                
                if(players.get(i-1).getCardTotal() == 21)
                {
                    dealerServer[i].sendMessage("BLACKJACK " + players.get(i-1).toString() + " CONGRATS");
                    turnsLeft--;
                }
            }
        }
        //end Deal
        
        private void dealersTurn()
        {
            boolean done = false;
            while(done == false)
            {
                if(dealer.getCardTotal() < 17)
                {
                    dealer.getCard(deck.dealOne());
                    for(int i=1; i<connectionCount; i++)
                    {
                        dealerServer[i].sendMessage("Dealer Hit");
                        dealerServer[i].sendMessage(dealer.playerCardsInfo(true));
                    }
                }
                if(dealer.getCardTotal() > 21)
                {
                    done = true;
                }
                if(dealer.getCardTotal() > 17 && dealer.getCardTotal() <= 21)
                {
                    for(int i=1; i<connectionCount; i++)
                    {
                        dealerServer[i].sendMessage("Dealer Stays");
                        dealerServer[i].sendMessage(dealer.playerCardsInfo(true));
                    }
                    done = true;
                }
            }
            sendResults();
            
        }
        //end DealersTurn
        
        private void sendResults()
        {
            int dealersTotal = dealer.getCardTotal();
            for(int i=1; i<connectionCount; i++)
            {
                int playerTotal = players.get(i - 1).getCardTotal();
                if(playerTotal <= 21)
                {
                    if(dealer.getCardTotal() > 21)
                    {
                        if(players.get(i-1).getCardTotal() <= 21)
                        {
                            dealerServer[i].sendMessage("Dealer Busts, " + players.get(i-1).toString() + " wins!");
                            dealerServer[i].sendMessage("ENDGAME");
                        }
                    }
                    else if(playerTotal > dealersTotal && playerTotal <= 21)
                    {
                        dealerServer[i].sendMessage("You Won");
                        dealerServer[i].sendMessage("ENDGAME");
                    }
                    else if(playerTotal == dealersTotal)
                    {
                        dealerServer[i].sendMessage("Tied the Dealer. Play again!");
                        dealerServer[i].sendMessage("ENDGAME");
                    }
                    else
                    {
                        dealerServer[i].sendMessage("Dealer Won! Play again!");
                    }
                }
            }
        }
        //end sendResults
        
        private void waitForPlayers()
        {
            try
            {
                showInfo("Waiting for player " + connectionID);
                socket = server.accept();
                showInfo("Connected to " + connectionID + " from " + socket.getInetAddress().getHostName());
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
                showInfo(ex.toString());
            }
        }
        //end WaitforPlayers
        
        private void checkTurnsLeft()
        {
            if(turnsLeft == 0)
            {
                dealersTurn();
            }
        }

        private void sendMessage(String message)
        {
            try 
            {
                output.writeObject(message);
            }
            catch (IOException ex) 
            {
                ex.printStackTrace();
                showInfo(ex.toString());
            }
        }
        //end sendMessage
    
    }
    //end DealerServer class
    
    private void showInfo(final String info)
    {
       
        SwingWorker<Void, String> showInfoWorker = new SwingWorker<Void, String>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
               publish(info);
               return null;
            }

            @Override
            protected void done() 
            {
                super.done(); 
            }

            @Override
            protected void process(List<String> chunks) 
            {
                super.process(chunks);
                for(String text : chunks)
                {
                    DealerTextArea.append(text + "\n");
                }
            }  
        };
        showInfoWorker.execute();
    }
    //end showInfo
    
    private void showPlayerCountButtons()
    {
        playerCountButton.setVisible(true);
        jLabel1.setVisible(true);
        playerCountBox.setVisible(true);
    }
    private void hidePlayerCountButtons()
    {
        playerCountButton.setVisible(false);
        jLabel1.setVisible(false);
        playerCountBox.setVisible(false);
    }
    private void showConnectionButtons()
    {
        StartServerButton.setVisible(true);
    }
    private void hideConnectionButtons()
    {
        StartServerButton.setVisible(false);
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
        playerLabel = new javax.swing.JLabel();
        playerCountLabel = new javax.swing.JLabel();
        playerCountButton = new javax.swing.JButton();
        playerCountBox = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        StartServerButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        DealerTextArea.setColumns(20);
        DealerTextArea.setRows(5);
        jScrollPane1.setViewportView(DealerTextArea);

        playerLabel.setText("Player Count:");

        playerCountLabel.setText("0");

        playerCountButton.setText("Enter Players");
        playerCountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playerCountButtonActionPerformed(evt);
            }
        });

        playerCountBox.setText("0");

        jLabel1.setText("How many Players will be playing?");

        StartServerButton.setText("Start Server");
        StartServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartServerButtonActionPerformed(evt);
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
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(playerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(playerCountLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(playerCountBox, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(playerCountButton))
                    .addComponent(jLabel1))
                .addGap(153, 153, 153)
                .addComponent(StartServerButton)
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(playerLabel)
                            .addComponent(playerCountLabel))
                        .addGap(23, 23, 23)
                        .addComponent(StartServerButton)
                        .addGap(47, 47, 47))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(playerCountButton)
                    .addComponent(playerCountBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void playerCountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playerCountButtonActionPerformed
        playerCountTest = Integer.parseInt(playerCountBox.getText());
        if(playerCountTest > 10)
        {
            showInfo("Max players is 10. Please try again.");
        }
        else
        {
            maxPlayers = playerCountTest;
            turnsLeft = maxPlayers;
            hidePlayerCountButtons();
            showInfo("Created " + maxPlayers + " Slots.");
            showConnectionButtons();
        }
    }//GEN-LAST:event_playerCountButtonActionPerformed

    private void StartServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartServerButtonActionPerformed

        StartServerButton.setVisible(false);
        initServer();

    }//GEN-LAST:event_StartServerButtonActionPerformed

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
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Server().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JTextArea DealerTextArea;
    public static javax.swing.JButton StartServerButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField playerCountBox;
    private javax.swing.JButton playerCountButton;
    public static javax.swing.JLabel playerCountLabel;
    private javax.swing.JLabel playerLabel;
    // End of variables declaration//GEN-END:variables
}

