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
package blackyjacky.Server;

import blackyjacky.Shared.Card;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author Scott
 */
public class Server extends javax.swing.JFrame 
{
    Card card;
    volatile ArrayList<Player> players;
    private ArrayList<Integer> playerCardValues;
    private ArrayList<Card> deck;
    private int connectedClients = 0;
    private int maxPlayers;
    private ServerSocket server = null;
    private Socket socket = null;
    private boolean gameStarted = false;
    private boolean runDealer = true;
    private boolean gotStartBack = false;
    private Thread dealerThread;
    volatile Iterator<Player> iterator;
 
    

    /**
     * Creates new form Server
     */
    public Server() 
    {
        initComponents();
        hidePlayerCountButtons();
    }
    
    private void initServer()
    {
        SwingWorker<Void, Void> initServerWorker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                //runDealer();
                playerCardValues = new ArrayList<>();
                players = new ArrayList<>();
                DealerTextArea.append("Waiting for players. \n");
                while(gameStarted == false)
                {
                    try
                    {
                        socket = server.accept();
                        Player player = new Player(socket);
                        addPlayer(player);
                        System.out.println(players.size());
                        System.out.println(maxPlayers);
                    }
                    catch(IOException ex)
                    {
                        ex.printStackTrace();
                    }
                    
                    if (players.size() == maxPlayers)
                    {
                        
                        System.out.println(gameStarted + "Reached maxPlayers");
                        gameStarted = true;
                        
                    }
                }
                
                return null;
            }
            @Override
            protected void done()
            {
                super.done();
                System.out.println("done");
                if (players.size() == maxPlayers)
                    {
                        
                        System.out.println(gameStarted + "Reached maxPlayers");
                        gameStarted = true;
                        runDealer();
                        
                    }
                //runDealer();
                
            }
        };
        initServerWorker.execute();
        
        
    }
    
    private void runDealer()
    {
        SwingWorker<Void, String> runDealerWorker = new SwingWorker<Void,String>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                runDealer = true;
                publish("Entered Run\n");
                       // playerCardValues = new ArrayList<>();
                        //players = new ArrayList<>();
                        
                            for(Player p1 : players)
                            {
                                System.out.println("Sending "+ p1 + " Start Message");
                                //gotStartBack = false;
                                p1.sendMessage("START");
                                publish("Start Message Sent \n");
                                
                                
                            }
                        
                
//                dealerThread = new Thread(new Runnable()
//                {
//                    @Override
//                    public void run() 
//                    {
//                        publish("Entered Run\n");
//                       // playerCardValues = new ArrayList<>();
//                        //players = new ArrayList<>();
//                        while(runDealer == true)
//                        {
//                            for(Player p1 : players)
//                            {
//                                System.out.println("Sending "+ p1 + " Start Message");
//                                //gotStartBack = false;
//                                p1.sendMessage("START");
//                                publish("Start Message Sent \n");
//                                runDealer = false;
//                                
//                            }
////                            iterator = players.iterator();
////                            while(iterator.hasNext())
////                            {
////                                Player p1 = iterator.next();
////                                p1.sendMessage("BET");
////                                p1.setBet(iterator);
////                                publish(p1 + "set a bet \n");
////
////                            }
//                           // runDealer = false;
//
//                        }
//                    }
//
//                });
//                dealerThread.start();
                return null;
            }

            @Override
            protected void process(List<String> chunks) 
            {
                super.process(chunks); 
                for(String text : chunks)
                {
                    DealerTextArea.append(text);
                }   
             }

            @Override
            protected void done() 
            {
                super.done(); 
                DealerTextArea.append("Finished runDealer()\n");
            }
        };
            
        runDealerWorker.execute();
        
    }
    
    public void addPlayer(Player player)
    {
        SwingWorker<Void, Void> addPlayerWorker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception
            {
                if(players.size() < maxPlayers)
                {
                    players.add(player);
                    DealerTextArea.append("Player " + player + "Joined. \n");
                    connectedClients++;
                    playerCountLabel.setText(Integer.toString(connectedClients));
                }
                else
                {
                    player.sendMessage("GAMEFULL");
                }
                return null;
            }      
        };
        addPlayerWorker.execute();
    }
    
    private void initConnection(int port)
    {
        SwingWorker<ServerSocket, Void> initServerWorker = new SwingWorker<ServerSocket,Void>()
                {
                    @Override
                    protected ServerSocket doInBackground() throws Exception 
                    {
                        server = new ServerSocket(port);
                        
                        return server;
                    }
                  

                    @Override
                    protected void done() 
                    {
                        super.done();
                        DealerTextArea.append("Created Server at port " + port + "\n");
                        
                    }
                    
                };
        initServerWorker.execute();
      
    }
    private void getPlayerCount()
    {
        SwingWorker<Void, Void> getPlayerCountWorker = new SwingWorker<Void,Void>()
                {
                    @Override
                    protected Void doInBackground() throws Exception 
                    {
                        DealerTextArea.append("How many players?\nAdd Below.\n");
                        showPlayerCountButtons();
                        return null;
                    }
                };
        getPlayerCountWorker.execute();
    }
    
    private void showPlayerCountButtons()
    {
        playerCountBox.setVisible(true);
        playerCountButton.setVisible(true);
        jLabel1.setVisible(true);
    }
    private void hidePlayerCountButtons()
    {
        playerCountBox.setVisible(false);
        playerCountButton.setVisible(false);
        jLabel1.setVisible(false);
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
        StartButton = new javax.swing.JButton();
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

        StartButton.setText("Start Game");
        StartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StartButtonActionPerformed(evt);
            }
        });

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
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(StartButton)
                    .addComponent(StartServerButton))
                .addGap(40, 40, 40))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(playerLabel)
                            .addComponent(playerCountLabel)
                            .addComponent(StartButton))
                        .addGap(18, 18, 18)
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

    private void StartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartButtonActionPerformed
       initServer();
       StartButton.setVisible(false);
       
    }//GEN-LAST:event_StartButtonActionPerformed

    private void StartServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartServerButtonActionPerformed
     getPlayerCount();
     initConnection(6000);
     StartServerButton.setVisible(false);
    
    }//GEN-LAST:event_StartServerButtonActionPerformed

    private void playerCountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playerCountButtonActionPerformed
        maxPlayers = Integer.parseInt(playerCountBox.getText());
        hidePlayerCountButtons();
        DealerTextArea.append("Created "+maxPlayers+" Slots\n");
    }//GEN-LAST:event_playerCountButtonActionPerformed

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
    public static javax.swing.JButton StartButton;
    public static javax.swing.JButton StartServerButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField playerCountBox;
    private javax.swing.JButton playerCountButton;
    public static javax.swing.JLabel playerCountLabel;
    private javax.swing.JLabel playerLabel;
    // End of variables declaration//GEN-END:variables
}
