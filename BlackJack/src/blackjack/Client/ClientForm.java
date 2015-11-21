/*
 * Creative Commons 0
 * The person who associated a work with this deed has dedicated the work to the public domain
 *  by waiving all of his or her rights to the work worldwide under copyright law,
 *  including all related and neighboring rights, to the extent allowed by law.
 */
package blackjack.Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.io.*;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
/**
 *
 * @author Scott
 */
public class ClientForm extends javax.swing.JFrame {
    
    private boolean isBetPressed = false;
    private int GUIBetAmount;
    static boolean playAgain;
    private String decision;
    
    
    private void Connect() throws Exception
    {
        ConnectionConfig con = new ConnectionConfig();
        ChatBoxArea.append("Connecting\n");
        con.connect();
        InputStream input = con.getInputStream();
        OutputStream output = con.getOutputStream();
        Player player = new Player(input, output);
        ChatBoxArea.append("Connected! Welcome to BlackJack.\n");
        playGame(player);
        
    }
    
    public void playGame(Player player)
    {
        
        disableBidButton();
        disableDecisionButtons();
        HitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.sendDecision("HIT");
                disableDecisionButtons();
               
            }
        });
        StayButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                player.sendDecision("STAY");
                disableDecisionButtons();
            }
        });
        while(playAgain)
        {
            ChatBoxArea.append("Waiting for dealer to start.\n");
            if(player.getMessage().equals("START"))
            {
                ChatBoxArea.append("Starting Game.");
                ChatBoxArea.append("Other players placing bets...");
                if(!player.getMessage().equals("BET"))
                {
                    return;
                }
                enableBidButton();
                player.placeBet();
                ChatBoxArea.append("Dealer deals to you");
                disableBidButton();
                player.getCard();
                player.getCard();
                if(!player.getMessage().equals("DECIDE"))
                {
                    return;
                }
                ChatBoxArea.append("Decide what to do!");
                enableDecisionButtons();
            }
            else
            {
                ChatBoxArea.append("Sorry, too many players or game has already started");
            }
            
            int response = JOptionPane.showConfirmDialog(null, "Do you want to play again?", "Round Ended", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.NO_OPTION)
            {
                playAgain = false;
            }
            else if(response == JOptionPane.YES_OPTION)
            {
                player.newGame();
            }
            else if(response == JOptionPane.CLOSED_OPTION)
            {
                playAgain = false;
            }
            
        }
    }

    
    //Player class for a player object
    public class Player 
{
    private int cash = 250;
    private int cardsValue = 0;
    private int cardCount = 0;
    private ArrayList<Card> playerCards;
    private InputStream inStream;
    private OutputStream outStream;
    private ObjectInputStream objectStream;
    private DataOutputStream dataOutStream;
    private DataInputStream dataInStream;
    private int betAmount;
    private boolean endGame = false;
    
    public Player(InputStream inStream, OutputStream outStream)
    {
     playerCards = new ArrayList<Card>();
     this.inStream = inStream;
     this.outStream = outStream;
     dataInStream = new DataInputStream(inStream);
     dataOutStream = new DataOutputStream(outStream);
     setCashLabel(cash);
     setCardTotal(cardsValue);
    }
    
    //Places a bet if the betButton has been pressed
    public void placeBet()
    {
        ChatBoxArea.append("Place a bet!");
        if (isBetPressed)
        {
            ChatBoxArea.append("Please press Submit Bid");
            placeBet();
        }
        else
        {
            betAmount = GUIBetAmount;
            int newMoney = cash - betAmount;
            setCashLabel(newMoney);
            clearBidLabel();
            isBetPressed = false;
        }
        
    }
    
    public void getCard()
    {
       try
       {
           //get a card, and add to playerCards
           objectStream = new ObjectInputStream(inStream);
           Card card = (Card) objectStream.readObject();
           cardsValue = cardsValue + card.getValue();
           ChatBoxArea.append("Recieved: " + card.getCardName());
           setCardTotal(cardsValue);
           playerCards.add(card);
           cardCount++;
           
           //Set Correct Image
           String whichImage = "Images/" + card.getCardName() + ".png";
           ImageIcon cardImage = new ImageIcon(whichImage);
           switch(cardCount)
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
               
           }
           
       } //print errors
       catch(IOException | ClassNotFoundException ex)
       {
           ex.printStackTrace();
           ChatBoxArea.append(ex.toString());
       }
    }
    //Helper to get a new card / hit button
    public void Hit()
    {
        ChatBoxArea.append("You chose to HIT.");
        getCard();
    }
    //Helper for stand button
    public void Stay()
    {
        ChatBoxArea.append("You chose to STAY.");
        endGame();
    }
    //stand results from dealer, add money if you won, reset stats
    public void endGame()
    {
        endGame = true;
        try
        {
            String result = dataInStream.readUTF();
            int numOfPlayers = inStream.read();
            ChatBoxArea.append(result);
            if(result.equals("WINNER"))
            {
                cash = cash + betAmount * numOfPlayers;
                setCashLabel(cash);
            }
            ChatBoxArea.append("New Balance: $" + getCash() );
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            ChatBoxArea.append(ex.toString());
        }
        
        cardsValue = 0;
        setCardTotal(cardsValue);
        playerCards = new ArrayList<>();
        betAmount = 0;
        clearBidLabel();
    }
    //helper to change the cash label
    private void setCashLabel(int cash)
    {
        CurrentCashCHANGE.setText(Integer.toString(cash));
    }
    //helper to get the cash amount
    public int getCash()
    {
     return Integer.parseInt(CurrentCashCHANGE.getText());
    }
    //helper to set the card label
    private void setCardTotal(int cardsValue)
    {
        CardsTotal.setText(Integer.toString(cardsValue));
    }
    //helper to empty the bid box
    public void clearBidLabel()
    {
        BidAmountBox.setText("000");
    }
    //helper to get message from server
    public String getMessage()
    {
        String message = "";
        try
        {
            message = dataInStream.readUTF();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            ChatBoxArea.append(ex.toString());
        }
        return message;
    }
    
    public void sendDecision(String decision)
    {
        while(!endGame)
        {
            try
            {
                dataOutStream.writeUTF(decision);
                dataOutStream.flush();
                switch(decision)
                {
                    case "HIT":
                        Hit();
                        break;
                    case "STAY":
                        endGame();
                        break;
                            
                }
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
                ChatBoxArea.append(ex.toString());
            }
         
            
        }
    }
    //new game sets endgame to false
    public void newGame()
    {
        endGame = false;
        ClearValues();
    }
    
}

    /**
     * Creates new form ClientForm
     */
    public ClientForm() {
        initComponents();
        ClearValues();
        try 
        {
            Connect();
        } 
        catch (Exception ex) 
        {
            ChatBoxArea.append(ex.toString());
        }
    }
    
    private void ClearValues()
    {
        BidAmountBox.setText("000");
        CardsTotal.setText("0");
    }
    
    private void enableBidButton()
    {
        SubmitBidButton.setVisible(true);
    }
    private void disableBidButton()
    {
        SubmitBidButton.setVisible(false);
    }
    private void enableDecisionButtons()
    {
       StayButton.setVisible(true);
       HitButton.setVisible(true);
    }
    private void disableDecisionButtons()
    {
        StayButton.setVisible(false);
        HitButton.setVisible(false);
    }
    
    
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
            java.util.logging.Logger.getLogger(ClientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new ClientForm().setVisible(true);
        });
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ChatPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ChatBoxArea = new javax.swing.JTextArea();
        ChatBoxMessageBox = new javax.swing.JTextField();
        ChatBoxSendButton = new javax.swing.JButton();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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
                .addComponent(ChatBoxMessageBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ChatBoxSendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ChatPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
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

        PlayerControlPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        BidAmountBox.setText("000");

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

        BidLabel.setText("Bid");

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

        javax.swing.GroupLayout PlayerControlPanelLayout = new javax.swing.GroupLayout(PlayerControlPanel);
        PlayerControlPanel.setLayout(PlayerControlPanelLayout);
        PlayerControlPanelLayout.setHorizontalGroup(
            PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                .addContainerGap()
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
                                .addComponent(StayButton, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                                        .addComponent(BidLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(SubmitBidButton)
                                        .addGap(100, 100, 100))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PlayerControlPanelLayout.createSequentialGroup()
                                        .addComponent(CurrentCashLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(CurrentCashCHANGE, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(78, 78, 78)
                                .addComponent(cardsTotalLbael)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(CardsTotal)))))
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
                    .addComponent(StayButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(HitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
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
                .addContainerGap(33, Short.MAX_VALUE))
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(DealerCardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(PlayerCardsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(ChatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    //Increased bid amount by 1
    private void IncreaseBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncreaseBidButtonActionPerformed
        int Amount = Integer.parseInt(BidAmountBox.getText());
        Amount++;
        BidAmountBox.setText(Integer.toString(Amount));
    }//GEN-LAST:event_IncreaseBidButtonActionPerformed

    //Decreased bid amount by 1
    private void DecreaseBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DecreaseBidButtonActionPerformed
        int Amount = Integer.parseInt(BidAmountBox.getText());
        Amount--;
        BidAmountBox.setText(Integer.toString(Amount));
    }//GEN-LAST:event_DecreaseBidButtonActionPerformed

    //Submit bid, sets isBetPressed to True for Player to access values
    private void SubmitBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SubmitBidButtonActionPerformed
        int bidBox = Integer.parseInt(BidAmountBox.getText());
        int maxBid = Integer.parseInt(BidLabel.getText());
        
        if(bidBox > maxBid || bidBox < 1)
        {
            ChatBoxArea.append("You cant make that bet!");
            isBetPressed=false;
        }
        else
        {
            GUIBetAmount = bidBox;
            isBetPressed=true;
        }
    }//GEN-LAST:event_SubmitBidButtonActionPerformed

    private void HitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HitButtonActionPerformed
        
    }//GEN-LAST:event_HitButtonActionPerformed

    private void StayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StayButtonActionPerformed

    }//GEN-LAST:event_StayButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField BidAmountBox;
    private javax.swing.JLabel BidLabel;
    private javax.swing.JLabel CardsTotal;
    private javax.swing.JTextArea ChatBoxArea;
    private javax.swing.JTextField ChatBoxMessageBox;
    private javax.swing.JButton ChatBoxSendButton;
    private javax.swing.JPanel ChatPanel;
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


