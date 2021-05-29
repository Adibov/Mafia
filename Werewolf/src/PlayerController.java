import java.io.IOException;
import java.net.Socket;

/**
 * PlayerController class manages players action. Each player has his own PlayerController
 * @author Adibov
 * @version 1.0
 */
public class PlayerController {
    private Player player;
    private ClientHandler clientHandler;
    private GameController gameController;

    /**
     * class constructor
     * @param gameController gameController of the game
     */
    public PlayerController(GameController gameController) {
        this.gameController = gameController;
        try {
            clientHandler = new ClientHandler(gameController.getServerSocket().accept()); // connection to the client established
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * register the client to the game and return the new player's object
     */
    public void registerPlayer() {
        String username = "";
        while (true) {
            clearScreen();
            sendMessageFromGod("Enter an username:");
            username = getMessage().getBody();
            if (gameController.isUsernameAvailable(username))
                break;
            clearScreen();
            sendMessageFromGod("This username has been already taken.");
            getCh();
        }
        player = new Player(username);
        clearScreen();
        sendMessageFromGod("You have successfully joined to the game.");
        getCh();
    }

    /**
     * clear screen for the player
     */
    public void clearScreen() {
        sendMessageFromGod("\n".repeat(50));
    }

    /**
     * implement getCh method from C language. (waits until client press enter)
     */
    public void getCh() {
        sendMessageFromGod("Press Enter key to continue...");
        getMessage(); // drop
    }

    public void sendMessageFromGod(String messageBody) {
        sendMessage(new Message(messageBody, God.getInstance()));
    }

    /**
     * send the given message to the client
     * @param message given message
     */
    public void sendMessage(Message message) {
        if (clientHandler == null)
            return;
        clientHandler.sendMessage(message);
    }

    /**
     * waits for the client to enter a message
     * @return entered message
     */
    public Message getMessage() {
        synchronized(this) {
            if (clientHandler == null)
                return null;
            return clientHandler.getMessage();
        }
    }

    /**
     * player getter
     * @return player
     */
    public Player getPlayer() {
        return player;
    }
}
