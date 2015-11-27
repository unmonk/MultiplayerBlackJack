/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackjack2;

import blackjack2.Client.ClientForm;
import blackjack2.Dealer.DealerForm;

/**
 *
 * @author Scott
 */
public class BlackJack2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        new ClientForm().setVisible(true);    // TODO code application logic here
        new DealerForm().setVisible(true);
    }
    
}
