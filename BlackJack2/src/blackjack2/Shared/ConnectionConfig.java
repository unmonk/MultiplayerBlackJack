package blackjack2.Shared;
import java.io.*;
import java.net.Socket;

public class ConnectionConfig implements Serializable
{
    public final Socket socket;
    public Player playerObject;
    public ObjectOutputStream outStream;
    public ObjectInputStream inStream;
    
    public ConnectionConfig(Socket connection)
    {
        socket = connection;
    }
    
}
