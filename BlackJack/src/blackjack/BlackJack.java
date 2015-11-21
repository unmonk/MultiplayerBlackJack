/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackjack;

import blackjack.Server.DealerForm;
import blackjack.Client.ClientForm;
import javax.swing.JFrame;

/**
 *
 * @author Scott
 */
public class BlackJack {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame dealer = new DealerForm();
        JFrame client1 = new ClientForm();
        JFrame client2 = new ClientForm();
        JFrame client3 = new ClientForm();
    }
    
}
