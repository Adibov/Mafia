import java.io.IOException;
import java.net.Socket;

/**
 * God class, represent god of the game. In other words, it manages game
 * @author Adibov
 * @version 1.0
 */
public class God extends Person {
    /**
     * class constructor
     */
    public God() {
        super("God");
    }

    /**
     * start a new game
     */
    public void startNewGame() {
        try (Socket gameSocket = new Socket("127.0.0.1", 2021)) {

        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
