/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackjack2.Client;
import blackjack2.Shared.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author Scott
 */
public class ClientForm extends javax.swing.JFrame implements Serializable, ActionListener
{
 
    public Player currentPlayer;
    private static String serverIP;
    private Socket socket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    Player.PLAYERMOVES move;

    /**
     * Creates new form ClientForm
     */
    public ClientForm() 
    {
        initComponents();
        initPlayer();
    }
    
    private void initPlayer()
    {
        currentPlayer = new Player("Name", 250);
    }
    
    private void Start()
    {
        try
        {
            connect();
            playGame();
            
        }
        finally
        {
            //closeConnections();
        }
    }
    
    private void playGame()
    {
        SwingWorker<Void, String> playGameWorker = new SwingWorker<Void, String>()
        {
            @Override
            protected Void doInBackground() throws Exception 
            {
                while(true)
                {
                    try
                    {
                        System.out.println("DEBUG: Reached playgame loop");
                        SendMessage message = (SendMessage)inStream.readObject();
                        System.out.println("DEBUG: Got a message" + message.toString());
                        switch(message.message)
                        {
                            case GET_NEXT_MOVE:
                                outStream = new ObjectOutputStream(socket.getOutputStream());
                                Player.PLAYERMOVES nextMove = getMove();
                                switch(nextMove)
                                {
                                    case HIT:
                                        outStream.writeObject(new SendMessage(SendMessage.messageType.HIT, ""));
                                        break;
                                    case STAY:
                                        outStream.writeObject(new SendMessage(SendMessage.messageType.STAY, ""));
                                        break;

                                }
                                break;
                            case DEALTWO:
                                currentPlayer.getCards(((Card[]) message.messageInfo));
                                publish("Got First Two Cards" + currentPlayer + "\n");
                                //ChatBoxArea.append("Got First Two Cards" + currentPlayer + "\n");
                                break;
                            case DEALONE:
                                currentPlayer.getCards(((Card) message.messageInfo));
                                publish("Got a card" + currentPlayer + "\n");
                                //ChatBoxArea.append("Got a card" + currentPlayer + "\n");
                                break;
                            case RESULT:
                                switch(((Results) message.messageInfo))
                                {
                                    case WIN:
                                        publish("You Won" + currentPlayer + "\n");
                                        //ChatBoxArea.append("You Won" + currentPlayer + "\n");
                                        break;
                                    case LOSS:
                                        publish("You Lost" + currentPlayer + "\n");
                                        //ChatBoxArea.append("You Lost" + currentPlayer + "\n");
                                        break;
                                    case DRAW:
                                        publish("You Tied" + currentPlayer + "\n");
                                        //ChatBoxArea.append("You Tied" + currentPlayer + "\n");
                                        break;   
                                }
                            case STAY_OK:
                                currentPlayer.stay = true;
                                break;
                            case ENDGAME:
                                ChatBoxArea.append("Ending Game");
                                closeConnections();
                                break;
                        }
                        outStream.flush();
                        if(socket.isClosed() == false)
                        {
                            inStream = new ObjectInputStream(socket.getInputStream());
                        }




                    }
                    catch(EOFException ex)
                    {
                        ex.printStackTrace();
                        ChatBoxArea.append(ex.toString() + "\n");
                    }
                    catch(IOException ex)
                    {
                        ex.printStackTrace();
                        ChatBoxArea.append(ex.toString() + "\n");
                    }
                    catch(ClassNotFoundException ex)
                    {
                        ex.printStackTrace();
                        ChatBoxArea.append(ex.toString() + "\n");
                    }

                }
            }

            @Override
            protected void process(List<String> chunks) 
            {
                for(String text : chunks)
                {
                    ChatBoxArea.append(text);
                }
            }
 
        };
        playGameWorker.execute();

    }
    
    
    
    private Player.PLAYERMOVES getMove()
    {
        
        SwingWorker<Player.PLAYERMOVES, Void> getMoveWorker = new SwingWorker<Player.PLAYERMOVES, Void>()
        {
            @Override
            protected Player.PLAYERMOVES doInBackground() throws Exception 
            {
                Player.PLAYERMOVES Thismove;
                showOptions();
                Options.setDoneFalse();
                while(Options.getDone() == false)
                {
                    System.out.println("DEBUG: Entered GetMove While Loop");
                    HitButton.addActionListener(new ActionListener() 
                    {
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                            Options.setMoveHit();
                            Options.setDoneTrue();
                        }
                    });
                    StayButton.addActionListener(new ActionListener() 
                    {
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                            Options.setMoveStay();
                            Options.setDoneTrue();
                        }
                    });
                }
                System.out.println("DEBUG Exited GetMove While Loop");
                hideOptions();

                Thismove = Options.getMove();
                move = Thismove;
                return Thismove;
            }
        };
        getMoveWorker.execute();
        return move;
        
    }
    
    
    private void connect()
    {
        ChatBoxArea.append("Connecting to Dealer\n");
        try
        {
            socket = new Socket("localhost", 6900);
            System.out.println("DEBUG: Socket Created");
            outStream = new ObjectOutputStream(socket.getOutputStream());
            inStream = new ObjectInputStream(socket.getInputStream());
            ChatBoxArea.append("Connected to Dealer\n");
            System.out.println("DEBUG: Streams Created");
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            ChatBoxArea.append(ex.toString() +"\n");
        }
    }
    
    
    private void closeConnections()
    {
        try
        {
            if(socket.isClosed() == false)
            {
                outStream.close();
                inStream.close();
                socket.close();
                ChatBoxArea.append("Closed Connections" +"\n");
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            ChatBoxArea.append(ex.toString() + "\n");
        }
    }
    
    
    private void showOptions()
    {
        HitButton.setVisible(true);
        StayButton.setVisible(true);
    }
    private void hideOptions()
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
                .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(DecreaseBidButton)
                    .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                        .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(IncreaseBidButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(BidAmountBox))
                        .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                .addComponent(CardsTotal))
                            .addGroup(PlayerControlPanelLayout.createSequentialGroup()
                                .addGap(267, 267, 267)
                                .addComponent(HitButton, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(StayButton, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(ConnectButton)))))
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
                    .addGroup(PlayerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(StayButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ConnectButton))
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
                .addComponent(ChatBoxMessageBox, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void IncreaseBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IncreaseBidButtonActionPerformed
        int Amount = Integer.parseInt(BidAmountBox.getText());
        Amount++;
        BidAmountBox.setText(Integer.toString(Amount));
    }//GEN-LAST:event_IncreaseBidButtonActionPerformed

    private void DecreaseBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DecreaseBidButtonActionPerformed
        int Amount = Integer.parseInt(BidAmountBox.getText());
        Amount--;
        BidAmountBox.setText(Integer.toString(Amount));
    }//GEN-LAST:event_DecreaseBidButtonActionPerformed

    private void SubmitBidButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SubmitBidButtonActionPerformed
      
    }//GEN-LAST:event_SubmitBidButtonActionPerformed

    private void HitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_HitButtonActionPerformed

    }//GEN-LAST:event_HitButtonActionPerformed

    private void StayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StayButtonActionPerformed

    }//GEN-LAST:event_StayButtonActionPerformed

    private void ConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConnectButtonActionPerformed
        Start();
        ConnectButton.setVisible(false);
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
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ClientForm().setVisible(true);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

