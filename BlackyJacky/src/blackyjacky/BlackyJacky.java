/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackyjacky;

import blackyjacky.Client.Client;
import blackyjacky.Server.Server;

/**
 *
 * @author Scott
 */
public class BlackyJacky 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        new Server().setVisible(true);
        new Client().setVisible(true);
    }
    
}
