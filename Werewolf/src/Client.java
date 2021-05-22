import java.io.IOException;
import java.net.Socket;

/**
 * Client class, handles player interactions between server
 * @author Adibov
 * @version 1.0
 */
public class Client {
    private Socket socket;

    /**
     * class constructor
     */
    public Client() {
        try {
            socket = new Socket("127.0.0.1", 2021);
        }
        catch (IOException exception) {
            exception.printStackTrace();
            socket = null;
        }
    }
}
