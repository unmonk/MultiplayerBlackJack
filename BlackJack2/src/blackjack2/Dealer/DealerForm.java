/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack2.Dealer;
import blackjack2.Shared.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Scott
 */
public class DealerForm extends javax.swing.JFrame 
{
    private static final Object lock = new Object();
    private static Dealer dealer;
    private ConnectionConfig[] connectionConfigs;
    private int playerCount;
    private ServerSocket serverSocket;
    private int connectedCount = 0;
    private boolean gameOver;
    private SwingWorker<Void, Void> dealerInitWorker;
    private SwingWorker<Void, String> playerWaitWorker;

    /**
     * Creates new form DealerForm
     */
    public DealerForm() 
    {
        initComponents();
        hidePlayerCountButtons();
    }
    
    private void initDealer()
    {
        gameOver = false;
        dealer = Dealer.getDealerInstance();
        try 
        {
            serverSocket = new ServerSocket(6900);
            System.out.println(serverSocket);
        } 
        catch (IOException ex) 
        {
           System.out.println("Failed");
           ex.printStackTrace();
        }
        System.out.println("DEBUG: Created dealer instance: " + dealer);
        
            dealerInitWorker = new SwingWorker<Void, Void>()
            {
               
                @Override
                protected Void doInBackground() throws Exception 
                {
                    StartServerButton.setVisible(false);
                    showPlayerCountButtons();
                    playerCount = checkPlayerCount();
                    System.out.println("DEBUG: Check Player Done");
                    connectionConfigs = new ConnectionConfig[playerCount];
                    System.out.println("DEBUG: ConnectionConfigs created with length " + connectionConfigs.length);
                    return null;
                }
                @Override
                protected void done()
                {
                    DealerTextArea.append(playerCount + " Slots have been allocated.  \n");
                    hidePlayerCountButtons();
                }
                
            };
            dealerInitWorker.execute();

    }
    
    private void playGame()
    {
        SwingWorker<ServerSocket, Void> playGameWorker = new SwingWorker<ServerSocket, Void>()
        {
            @Override
            protected ServerSocket doInBackground() throws Exception 
            {
                StartButton.setVisible(false);
                try
                {
                    DealerTextArea.append("Waiting for Players...\n");
                    //serverSocket = new ServerSocket(8900, playerCount);
                    System.out.println(serverSocket);
                    try
                    {
                        waitForPlayers();
                        Deal();

                        if(isTurnOver() == true)
                        {
                            getWinner();
                        }
                        else
                        {
                            continueGame();
                        }
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                        DealerTextArea.append(ex.toString() + "\n");
                    }
                    finally
                    {
                        //serverSocket.close();
                    }
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                    DealerTextArea.append(ex.toString() + "\n");
                }
                finally
                {
                    //close();
                }
        return serverSocket;
            }
        };
        
        playGameWorker.execute();
    }
    
    private void waitForPlayers()
    {
       playerWaitWorker = new SwingWorker<Void, String>()
                {
            @Override
            protected Void doInBackground() throws Exception 
            {
               try
                {
                    publish("Waiting for players... \n");
                    synchronized(lock)
                    {
                        while(true)
                        {
                            publish("Connecting... \n");
                            //DealerTextArea.append("Connecting... \n");
                            connectionConfigs[connectedCount] = new ConnectionConfig(serverSocket.accept());
                            //DealerTextArea.append("Connection From " + connectionConfigs[connectedCount].socket.getInetAddress().getHostName() + "\n");
                            publish("Connection From " + connectionConfigs[connectedCount].socket.getInetAddress().getHostName() + "\n");
                            connectionConfigs[connectedCount].outStream = new ObjectOutputStream(connectionConfigs[connectedCount].socket.getOutputStream());
                            connectionConfigs[connectedCount].inStream = new ObjectInputStream(connectionConfigs[connectedCount].socket.getInputStream());
                            connectedCount++;

                            playerCountLabel.setText(Integer.toString(connectedCount));

                            if(connectedCount == playerCount)
                            {
                                lock.notify();
                                System.out.println("DEBUG: Lock Notified, breaking from while");
                                break;
                            }

                        }
                    }
                } catch (IOException ex)
                {
                    DealerTextArea.append(ex.toString() + "\n");
                    ex.printStackTrace();
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
        playerWaitWorker.execute();
    }
    
    private void continueGame()
    {
        SwingWorker<Void, Void> continueWorker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                while(gameOver == false)
                {
                    int playersLeft = getPlayersLeft();
                    Thread[] playerThreads;
                    if(playersLeft > 0)
                    {
                        playerThreads = new Thread[playersLeft];
                        int playerIndex = 0;
                        for(final ConnectionConfig config : connectionConfigs)
                        {
                            if((config.playerObject.stay == false) && (config.playerObject.bust == false))
                            {
                                playerThreads[playerIndex] = new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        getMove(config);
                                    }
                                });
                                synchronized(this)
                                {
                                    playerThreads[playerIndex++].start();
                                }
                            }
                        }
                         for(Thread t : playerThreads)
                        {
                            try
                            {
                             t.join();
                            }
                            catch(InterruptedException ex)
                            {
                                ex.printStackTrace();
                                DealerTextArea.append(ex.toString() + "\n");
                            }
                        }
                    }
                    else
                    {
                        gameOver = true;
                        getWinner();
                        break;
                    }
                }
                return null;
            }  
        };
        continueWorker.execute();
        
    }
    
    private int checkPlayerCount()
    {
        DealerTextArea.append("How many players will be playing?  Submit Below" + "\n");
        showPlayerCountButtons();
        
        while(DealerOptions.isDone == false)
        {
            playerCountButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e) 
                {
                    DealerOptions.setPlayers(Integer.parseInt(playerCountBox.getText()));
                    DealerOptions.setDoneTrue();
                    hidePlayerCountButtons();
                }
                
            });
        }
        return DealerOptions.getPlayers();
    }
    
    private int getPlayersLeft()
    {
        int playersLeft = 0;
        for(ConnectionConfig config : connectionConfigs)
        {
            if((config.playerObject.stay == false) && (config.playerObject.bust == false))
            {
                playersLeft++;
            }
        }
        return playersLeft;
    }
    
    private boolean isTurnOver()
    {
        int playersLeft = getPlayersLeft();
        if(playersLeft > 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    
    
    private void Deal()
    {
        if(connectedCount != playerCount)
        {
            waitForPlayers();
        }
        SwingWorker<Void, Void> DealWorker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                System.out.println("DEBUG: Started Dealing");
                Thread[] playerThreads = new Thread[playerCount];
                for (int i=0; i<playerCount; i++)
                {
                    final int finalI = i;
                    playerThreads[i] = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            dealHelper(connectionConfigs[finalI]);
                        }
                    });
                    playerThreads[i].start();

                }
                for(Thread t : playerThreads)
                {
                    try
                    {
                        t.join();
                    }
                    catch(InterruptedException ex)
                    {
                        ex.printStackTrace();
                        DealerTextArea.append(ex.toString() + "\n");
                    }
                }
                return null;
            }
        };
        DealWorker.execute();
    }
    
    private void dealHelper(ConnectionConfig config)
    {
       SwingWorker<Void, String> DealHelperWorker = new SwingWorker<Void, String>()
       {
           @Override
           protected Void doInBackground() throws Exception 
           {
                try
                {
                    synchronized(lock)
                    {
                        if(config.playerObject.CanHit())
                        {
                            Card[] cards = dealer.DealTwo();
                            config.outStream = new ObjectOutputStream(config.socket.getOutputStream());
                            config.outStream.writeObject(new SendMessage(SendMessage.messageType.DEALTWO, cards));
                            config.outStream.flush();
                            publish("Dealt to: " + config.playerObject + "\n");

                            
                        }
                    }
                }
                catch(IOException ex)
                {
                    ex.printStackTrace();
                    DealerTextArea.append(ex.toString() + "\n");
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
       DealHelperWorker.execute();
    }
    
    private void getMove(ConnectionConfig config)
    {
        SwingWorker<Void, String> moveWorker = new SwingWorker<Void, String>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                try
                {
                    OutputStream stream = config.socket.getOutputStream();
                    config.outStream = new ObjectOutputStream(stream);
                    config.outStream.writeObject(new SendMessage(SendMessage.messageType.GET_NEXT_MOVE, ""));
                    config.outStream.flush();
                    config.inStream = new ObjectInputStream(config.socket.getInputStream());
                    SendMessage message = (SendMessage) config.inStream.readObject();
                    switch(message.message)
                    {
                        case HIT:
                            synchronized(lock)
                            {
                                if(config.playerObject.CanHit()==true)
                                {
                                    Card card = dealer.DealOne();
                                    stream = config.socket.getOutputStream();
                                    config.outStream = new ObjectOutputStream(stream);
                                    config.outStream.writeObject(new SendMessage(SendMessage.messageType.DEALONE, card));
                                    config.outStream.flush();
                                    config.playerObject.getCards(card);
                                    publish(config.playerObject + "\n");
                                    //DealerTextArea.append(config.playerObject + "\n");
                                }
                            }
                            break;
                        case STAY:
                            synchronized(lock)
                            {
                                if((config.playerObject.bust == false) && (config.playerObject.stay == false))
                                {
                                    config.playerObject.stay = true;
                                    stream = config.socket.getOutputStream();
                                    config.outStream = new ObjectOutputStream(stream);
                                    config.outStream.writeObject(new SendMessage(SendMessage.messageType.STAY_OK, ""));
                                    config.outStream.flush();
                                    publish(config.playerObject + "\n");
                                    //DealerTextArea.append(config.playerObject + "\n");
                                }
                            }
                    }
                }
                catch(IOException ex)
                {
                    DealerTextArea.append(ex.toString() + "\n");
                    ex.printStackTrace();
                }
                catch(ClassNotFoundException ex)
                {
                    DealerTextArea.append(ex.toString() + "\n");
                    ex.printStackTrace();
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
        moveWorker.execute();
    }
    
    private void close()
    {
        if(serverSocket.isClosed() == false)
        {
            try
            {
                serverSocket.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
                DealerTextArea.append(ex.toString() + "\n");
            }
        }
    }
    
    private void getWinner()
    {
        SwingWorker<Void, Void> getWinnerWorker = new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                boolean draw = true;
                Player winner = null;
                for(ConnectionConfig config : connectionConfigs)
                {
                    if(config.playerObject.bust == false)
                    {
                        if(winner == null)
                        {
                            winner = config.playerObject;
                            draw = false;
                        }
                        else if(config.playerObject.getHandValue() > winner.getHandValue())
                        {
                            winner = config.playerObject;
                            draw = false;
                        }
                        else if(config.playerObject.getHandValue() == winner.getHandValue())
                        {
                            draw = true;
                        }
                    }
                }
                final boolean isDraw = draw;
                final Player isWinner = winner;
                Thread[] playerThreads = new Thread[playerCount];
                for(int i=0; i<playerCount; i++)
                {
                    final int finalI = i;
                    playerThreads[i] = new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            sendWinResults(connectionConfigs[finalI], isDraw, isWinner);
                        }
                    });
                    playerThreads[i].start();
                }
                for(Thread t : playerThreads)
                {
                    try
                    {
                        t.join();
                    }
                    catch(InterruptedException ex)
                    {
                        ex.printStackTrace();
                        DealerTextArea.append(ex.toString() + "\n");
                    }
                }
                return null;
            }
            };
        getWinnerWorker.execute();
    }
    
    private void sendWinResults(ConnectionConfig config, boolean isDraw, Player isWinner)
    {
        SwingWorker<Void,String> sendWinWorker = new SwingWorker<Void, String>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                try
                {
                    config.outStream = new ObjectOutputStream(config.socket.getOutputStream());
                    if(isDraw)
                    {
                        config.outStream.writeObject(new SendMessage(SendMessage.messageType.RESULT, Results.DRAW));
                        //DealerTextArea.append("Game Draw" + config.playerObject + "\n");
                        publish("Game Draw" + config.playerObject + "\n");
                    }
                    else if(config.playerObject.equals(isWinner))
                    {
                        config.outStream.writeObject(new SendMessage(SendMessage.messageType.RESULT, Results.WIN));
                        //DealerTextArea.append("Game Winner: " + config.playerObject + "\n");
                        publish("Game Winner: " + config.playerObject + "\n");
                    }
                    else
                    {
                        config.outStream.writeObject(new SendMessage(SendMessage.messageType.RESULT, Results.LOSS));
                        //DealerTextArea.append("Game Loser: " + config.playerObject + "\n");
                        publish("Game Loser: " + config.playerObject + "\n");
                    }

                    closeConnection(config);
                }

                catch(IOException ex)
                {
                    ex.printStackTrace();
                    DealerTextArea.append(ex.toString() + "\n");
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
        sendWinWorker.execute();
    }
    
    private void closeConnection(ConnectionConfig config)
    {
        DealerTextArea.append("Closing Connection With: " + config.playerObject + "\n");
        try
        {
            config.outStream.close();
            config.inStream.close();
            config.socket.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            DealerTextArea.append(ex.toString() + "\n");
        }
    }
    
    
    
    private void hidePlayerCountButtons()
    {
        playerCountBox.setVisible(false);
        playerCountButton.setVisible(false);
        jLabel1.setVisible(false);
    }
    
    private void showPlayerCountButtons()
    {
        playerCountBox.setVisible(true);
        playerCountButton.setVisible(true);
        jLabel1.setVisible(true);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
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
                .addContainerGap(64, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void StartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartButtonActionPerformed
        playGame();

    }//GEN-LAST:event_StartButtonActionPerformed

    private void StartServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StartServerButtonActionPerformed
        initDealer();
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
