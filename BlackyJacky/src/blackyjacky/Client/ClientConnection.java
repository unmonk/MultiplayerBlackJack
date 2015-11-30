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
package blackyjacky.Client;

import java.io.*;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author Scott
 */
public class ClientConnection 
{
    private Socket socket = null;
    private InputStream inStream = null;
    private OutputStream outStream = null;
    
    public Socket connect(int port)
    {
        try
        {
            socket = new Socket("localhost", port);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return socket;
        
    }
    
    public InputStream getInputStream()
    {
        
        try
        {
            inStream = socket.getInputStream();
            
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return inStream;
    }
    
    public OutputStream getOutputStream()
    {
        try
        {
            outStream = socket.getOutputStream();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
        return outStream;
                
    }
   
                            
    
}
