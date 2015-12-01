/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackestjack.Client;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
/* 
 * @author Scott
 * @author Jessica
 * CS 412 Project
 * 2 December 2015

Notes: Cards total does not update, making player's decision hard to make at times
        Bid buttons have disappeared since I edited it, but can't find why
        Cash total does not update, either
        Chat box doesn't work
        

 */
public class Client extends javax.swing.JFrame {

    private Socket socket = null;
    private int cash;
    private int bet;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    /**
     * Creates new form Client
     */
    public Client() 
    {
        initComponents();
        hideBetButtons();
    }
    
    
    
    
    public void initConnection()
    {
        SwingWorker<Void, String> initConnectionWorker = new SwingWorker<Void, String>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                try
                {
                    /*
                    Change the parameters of the socket instantiation to play over 
                    a network with another computer. 
                    */
                    socket = new Socket("localhost", 6000);
                    output = new ObjectOutputStream(socket.getOutputStream());
                    output.flush();
                    input = new ObjectInputStream(socket.getInputStream());
                    publish("Streams Created!");
                    //showInfo("Streams Created!");
                    publish("Connected to server at " + socket.getInetAddress().getHostName());
                    //showInfo("Connected to server at: " + socket.getInetAddress().getHostName());
                    initPlayer();
                }
                catch(IOException ex)
                {
                    ex.printStackTrace();
                    publish(ex.toString());
                    //showInfo(ex.toString());
                }
                finally
                {
                    closeConnections();
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) 
            {
                super.process(chunks); 
                for(String text : chunks)
                {
                    ChatBoxArea.append(text + "\n");
                }
            }
            
            
        };
        initConnectionWorker.execute();
       
    }
    
    public void initPlayer()
    {
        
        String name = JOptionPane.showInputDialog("Enter Your Name:");
        sendMessage(name);
        System.out.println(name);
        do
        {
            try
            {
                message = (String) input.readObject();
                System.out.println(message);
                showInfo(message);
                showCards(message);
                if(message.compareToIgnoreCase("BUST")==0 || message.compareToIgnoreCase("WAIT")==0)
                {
                    hideOptionButtons();
                }
                if(message.compareToIgnoreCase("You Won")==0 || message.compareToIgnoreCase("Tied the Dealer. Play again!")==0)
                {
                    //IF THE GAME IS WON,
                    //INCREASE cash BY bet AMOUNT
                    //update display variable: CurrentCashCHANGE
                    cash = Integer.parseInt(CurrentCashCHANGE.getText()) + bet;
                    CurrentCashCHANGE.setText(Integer.toString(cash));
                }
                else if(message.compareToIgnoreCase("Dealer Won! Play again!")==0 || message.compareToIgnoreCase("BUST")==0)
                {
                     cash = Integer.parseInt(CurrentCashCHANGE.getText()) - bet;
                    CurrentCashCHANGE.setText(Integer.toString(cash));
                }
                
            } 
            catch (IOException ex) 
            {
                ex.printStackTrace();
                showInfo(ex.toString());
            } 
            catch (ClassNotFoundException ex) 
            {
                ex.printStackTrace();
                showInfo(ex.toString());
            }
           
        } while(message.equals("ENDGAME") == false);
        
        String newGame = JOptionPane.showInputDialog("Play again? y/n");
        if (newGame.equals("y")) initPlayer();  
        
    }
    
    public void closeConnections()
    {
        showInfo("Disconnecting");
        try
        {
            output.close();
            input.close();
            socket.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            showInfo(ex.toString());
        }
    }
    
    public void sendMessage(String message)
    {
        try
        {
            output.writeObject(message);
            output.flush();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            showInfo(ex.toString());
        }
    }
    
    public void showCards(String cardz)
    {
        String[] test;
        SwingWorker<String[], String> showCardsWorker = new SwingWorker<String[], String>()
        {
            @Override
            protected String[] doInBackground() throws Exception
            {
                String cardInfo = cardz;
                String delims = "[+]";
                String[] cards = cardInfo.split(delims);
                
                if(!"Dealer cards: ".equals(cards[0]))
                {
                    for(int i=1; i<cards.length; i++)
                    { 
                       String playerCard = "PlayerCard"+i;
                       String cardToGet = cards[i];
                       publish("Images/" + cardToGet + ".png");
                    
                    }

                }
                
                return cards;
                
            }  
            
            @Override
            protected void process(List<String> chunks) 
            {
                
                super.process(chunks); //To change body of generated methods, choose Tools | Templates.
                String[] cards = null;
                try {
                    cards = super.get();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
                for(String text : chunks)
                {
                    ImageIcon cardImage = new ImageIcon(text);
                    for(int i=1; i<cards.length; i++)
                       switch(i)
                       {
                           case 1:
                               playerCard1.setIcon(cardImage);
                               break;
                           case 2:
                               playerCard2.setIcon(cardImage);
                               break;
                           case 3:
                               playerCard3.setIcon(cardImage);
                               break;
                            case 4:
                               playerCard4.setIcon(cardImage);
                               break;
                            case 5:
                               playerCard5.setIcon(cardImage);
                               break;
                            case 6:
                               playerCard6.setIcon(cardImage);
                               break;
                            default:
                                break;


                       }
                }
            }
            
            

            @Override
            protected void done() 
            {
                super.done();
                try {
                    super.get();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ExecutionException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        showCardsWorker.execute();
        
    }
    
    public void showInfo(final String info)
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
                    ChatBoxArea.append(text + "\n");
                }
            }  
        };
        showInfoWorker.execute();
//       SwingUtilities.invokeLater(new Runnable()
//       {
//           public void run()
//           {
//               ChatBoxArea.append(info + "\n");
//           }
//       });
    }
    
    public void showBetButtons()
    {
        IncreaseBidButton.setVisible(true);
        DecreaseBidButton.setVisible(true);
        SubmitBidButton.setVisible(true);
        BidAmountBox.setVisible(true);
    }
    public void hideBetButtons()
    {
        IncreaseBidButton.setVisible(false);
        DecreaseBidButton.setVisible(false);
        SubmitBidButton.setVisible(false);
        BidAmountBox.setVisible(false);
    }
    public void showOptionButtons()
    {
        HitButton.setVisible(true);
        StayButton.setVisible(true);
    }
    public void hideOptionButtons()
    {
        HitButton.setVisible(false);
        StayButton.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        DealerCardsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        dealerCard1 = new javax.swing.JLabel();
        dealerCard2 = new javax.swing.JLabel();
        dealerCard3 = new javax.swing.JLabel();
        dealerCard4 = new javax.swing.JLabel();
        dealerCard5 = new javax.swing.JLabel();
        dealerCard6 = new javax.swing.JLabel();
        PlayerCardsPanel = new javax.swing.JPanel();
        yourCards = new javax.swing.JLabel();
        playerCard1 = new javax.swing.JLabel();
        playerCard2 = new javax.swing.JLabel();
        playerCard3 = new javax.swing.JLabel();
        playerCard4 = new javax.swing.JLabel();
        playerCard5 = new javax.swing.JLabel();
        playerCard6 = new javax.swing.JLabel();
        PlayerControlPanel = new javax.swing.JPanel();
        BidAmountBox = new javax.swing.JTextField();
        IncreaseBidButton = new javax.swing.JButton();
        DecreaseBidButton = new javax.swing.JButton();
        BidLabel = new javax.swing.JLabel();
        SubmitBidButton = new javax.swing.JButton();
        CurrentCashLabel = new javax.swing.JLabel();
        CurrentCashCHANGE = new javax.swing.JLabel();
        HitButton = new javax.swing.JButton();
        StayButton = new javax.swing.JButton();
        cardsTotalLbael = new javax.swing.JLabel();
        CardsTotal = new javax.swing.JLabel();
        ConnectButton = new javax.swing.JButton();
        ChatPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ChatBoxArea = new javax.swing.JTextArea();
        ChatBoxMessageBox = new javax.swing.JTextField();
        ChatBoxSendButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        DealerCardsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Dealer");

        javax.swing.GroupLayout DealerCardsPanelLayout = new javax.swing.GroupLayout(DealerCardsPanel);
        DealerCardsPanel.setLayout(DealerCardsPanelLayout);
        DealerCardsPanelLayout.setHorizontalGroup(
            DealerCardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DealerCardsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(20, 20, 20))
            .addGroup(DealerCardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(DealerCardsPanelLayout.createSequentialGroup()
                    .addGap(33, 33, 33)
                    .addComponent(dealerCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(dealerCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(dealerCard3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(dealerCard4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(dealerCard5, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(dealerCard6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(40, Short.MAX_VALUE)))
        );
        DealerCardsPanelLayout.setVerticalGroup(
            DealerCardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DealerCardsPanelLayout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(0, 133, Short.MAX_VALUE))
            .addGroup(DealerCardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(DealerCardsPanelLayout.createSequentialGroup()
                    .addGap(29, 29, 29)
                    .addGroup(DealerCardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dealerCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dealerCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dealerCard3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dealerCard4, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dealerCard5, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dealerCard6, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(29, Short.MAX_VALUE)))
        );

        PlayerCardsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        yourCards.setText("Your Cards");

        javax.swing.GroupLayout PlayerCardsPanelLayout = new javax.swing.GroupLayout(PlayerCardsPanel);
        PlayerCardsPanel.setLayout(PlayerCardsPanelLayout);
        PlayerCardsPanelLayout.setHorizontalGroup(
            PlayerCardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlayerCardsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(playerCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(playerCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(playerCard3, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(playerCard4, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(playerCard5, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(playerCard6, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(63, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PlayerCardsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(yourCards)
                .addGap(19, 19, 19))
        );
        PlayerCardsPanelLayout.setVerticalGroup(
            PlayerCardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlayerCardsPanelLayout.createSequentialGroup()
                .addComponent(yourCards)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlayerCardsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(playerCard1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playerCard2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playerCard3, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playerCard4, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playerCard5, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(playerCard6, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41))
        );

        PlayerControlPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        BidAmountBox.setText("001");

        IncreaseBidButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        IncreaseBidButton.setText("↑");
        IncreaseBidButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        IncreaseBidButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IncreaseBidButtonActionPerformed(evt);
            }
        });

        DecreaseBidButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        DecreaseBidButton.setText("↓");
        DecreaseBidButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        DecreaseBidButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DecreaseBidButtonActionPerformed(evt);
            }
        });

        BidLabel.setText("Bid: $");

        SubmitBidButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        SubmitBidButton.setText("Submit Bid");
        SubmitBidButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SubmitBidButtonActionPerformed(evt);
            }
        });

        CurrentCashLabel.setText("Current Cash:");

        CurrentCashCHANGE.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        CurrentCashCHANGE.setText("100");

        HitButton.setText("HIT");
        HitButton.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(250, 0, 50), 1, true));
        HitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                HitButtonActionPerformed(evt);
            }
        });

        StayButton.setText("STAY");
        StayButton.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(250, 0, 50), 1, true));
        StayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StayButtonActionPerformed(evt);
            }
        });

        cardsTotalLbael.setText("Cards Total:");

        CardsTotal.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        CardsTotal.setText("15");

        ConnectButton.setText("Connect");
        ConnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConnectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout PlayerControlPanelLayout = new javax.swing.GroupLayout(PlayerControlPanel);
        PlayerControlPanel.setLayout(PlayerControlPanelLayout);
        PlayerControlPanelLayout.setHorizontalGroup(
            PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(BidLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(DecreaseBidButton)
                    .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                        .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(IncreaseBidButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(BidAmountBox))
                        .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                                .addGap(267, 267, 267)
                                .addComponent(HitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(StayButton, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                                .addComponent(ConnectButton))
                            .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                                        .addComponent(CurrentCashLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(CurrentCashCHANGE, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(78, 78, 78)
                                        .addComponent(cardsTotalLbael)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(CardsTotal))
                                    .addComponent(SubmitBidButton))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        PlayerControlPanelLayout.setVerticalGroup(
            PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PlayerControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PlayerControlPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(IncreaseBidButton))
                    .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                        .addComponent(ConnectButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(HitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(StayButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BidAmountBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BidLabel)
                    .addComponent(SubmitBidButton))
                .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(DecreaseBidButton))
                    .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(CurrentCashLabel)
                            .addComponent(CurrentCashCHANGE, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cardsTotalLbael)
                            .addComponent(CardsTotal))))
                .addGap(8, 8, 8))
        );

        ChatPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        ChatBoxArea.setColumns(20);
        ChatBoxArea.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        ChatBoxArea.setRows(5);
        jScrollPane1.setViewportView(ChatBoxArea);

        ChatBoxMessageBox.setText("Message");

        ChatBoxSendButton.setText("Send");

        javax.swing.GroupLayout ChatPanelLayout = new javax.swing.GroupLayout(ChatPanel);
        ChatPanel.setLayout(ChatPanelLayout);
        ChatPanelLayout.setHorizontalGroup(
            ChatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChatPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ChatBoxMessageBox, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ChatBoxSendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        ChatPanelLayout.setVerticalGroup(
            ChatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ChatPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(ChatPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ChatBoxMessageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ChatBoxSendButton))
                .addGap(0, 11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(PlayerCardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DealerCardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(48, 48, 48)
                        .addComponent(ChatPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(PlayerControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(ChatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(DealerCardsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(PlayerCardsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(PlayerControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /*
    Scott: Why use Amount(local) and not just replace with bet(global)?
    */
    private void IncreaseBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncreaseBidButtonActionPerformed
        int Amount = Integer.parseInt(BidAmountBox.getText());
        Amount++;
        bet = Amount;
        BidAmountBox.setText(Integer.toString(Amount));
      
    }//GEN-LAST:event_IncreaseBidButtonActionPerformed

    private void DecreaseBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DecreaseBidButtonActionPerformed
        int Amount = Integer.parseInt(BidAmountBox.getText());
        if(Amount > 1)
        {
            Amount--;
        }
        bet = Amount;
        BidAmountBox.setText(Integer.toString(Amount));
       
    }//GEN-LAST:event_DecreaseBidButtonActionPerformed

    private void SubmitBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SubmitBidButtonActionPerformed
        int Amount = Integer.parseInt(BidAmountBox.getText());
        bet = Amount;
    }//GEN-LAST:event_SubmitBidButtonActionPerformed

    private void HitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HitButtonActionPerformed
        sendMessage("HIT");
    }//GEN-LAST:event_HitButtonActionPerformed

    private void StayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StayButtonActionPerformed
        sendMessage("STAY");
    }//GEN-LAST:event_StayButtonActionPerformed

    private void ConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConnectButtonActionPerformed

        ConnectButton.setVisible(false);
        initConnection();
    }//GEN-LAST:event_ConnectButtonActionPerformed

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
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Client.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Client().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField BidAmountBox;
    private javax.swing.JLabel BidLabel;
    private javax.swing.JLabel CardsTotal;
    private javax.swing.JTextArea ChatBoxArea;
    private javax.swing.JTextField ChatBoxMessageBox;
    private javax.swing.JButton ChatBoxSendButton;
    private javax.swing.JPanel ChatPanel;
    private javax.swing.JButton ConnectButton;
    private javax.swing.JLabel CurrentCashCHANGE;
    private javax.swing.JLabel CurrentCashLabel;
    private javax.swing.JPanel DealerCardsPanel;
    private javax.swing.JButton DecreaseBidButton;
    private javax.swing.JButton HitButton;
    private javax.swing.JButton IncreaseBidButton;
    private javax.swing.JPanel PlayerCardsPanel;
    private javax.swing.JPanel PlayerControlPanel;
    private javax.swing.JButton StayButton;
    private javax.swing.JButton SubmitBidButton;
    private javax.swing.JLabel cardsTotalLbael;
    private javax.swing.JLabel dealerCard1;
    private javax.swing.JLabel dealerCard2;
    private javax.swing.JLabel dealerCard3;
    private javax.swing.JLabel dealerCard4;
    private javax.swing.JLabel dealerCard5;
    private javax.swing.JLabel dealerCard6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel playerCard1;
    private javax.swing.JLabel playerCard2;
    private javax.swing.JLabel playerCard3;
    private javax.swing.JLabel playerCard4;
    private javax.swing.JLabel playerCard5;
    private javax.swing.JLabel playerCard6;
    private javax.swing.JLabel yourCards;
    // End of variables declaration//GEN-END:variables
}
