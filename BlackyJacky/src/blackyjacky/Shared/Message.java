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
package blackyjacky.Shared;

import java.io.Serializable;

/**
 *
 * @author Scott
 */
public class Message implements Serializable
{
    static final int LOGIN = 1;
    static final int HIT = 2;
    static final int STAY = 3;
    static final int WIN = 4;
    static final int LOSE = 5;
    static final int TIE = 6;
    
    private int type;
    private String message;
    
    public void Message(int type, String message)
    {
        this.type = type;
        this.message = message;
    }
    
    public int getType()
    {
        return type;
    }
    
    public String getMessage()
    {
        return message;
    }
}
