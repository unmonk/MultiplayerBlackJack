/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blackestjack;
import blackestjack.Client.Client;
import blackestjack.Server.Server;
/**
 *
 * @author Scott
 * @author Jessica
 * CS 412 Project
 * 2 December 2015
 */
public class BlackestJack 
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        new Server().setVisible(true);
        new Client().setVisible(true);
        // TODO code application logic here
    }
    
}
