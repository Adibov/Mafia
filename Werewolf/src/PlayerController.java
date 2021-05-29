/**
 * PlayerController class manages players action. Each player has his own PlayerController
 * @author Adibov
 * @version 1.0
 */
public class PlayerController implements Runnable {
    private Player player;
    private ClientHandler clientHandler;
    private GameController gameController;

    /**
     * class constructor
     * @param gameController gameController of the game
     */
    public PlayerController(GameController gameController) {
        this.gameController = gameController;
        clientHandler = new ClientHandler(); // connection to the client established
    }

    /**
     * Override run method to make this class Runnable
     */
    @Override
    public void run() {
        if (player == null)
            player = registerPlayer();
    }

    /**
     * register the client to the game and return the new player's object
     * @return new player object
     */
    public Player registerPlayer() {
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
        clearScreen();
        sendMessageFromGod("You have successfully joined to the game.");
        getCh();
        return new Player(username);
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
        sendMessageFromGod("Press any key to continue...");
        getMessage(); // drop
    }

    public void sendMessageFromGod(String body) {
        sendMessage(new Message(body, God.getInstance()));
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
        if (clientHandler == null)
            return null;
        return clientHandler.getMessage();
    }

    /**
     * player getter
     * @return player
     */
    public Player getPlayer() {
        return player;
    }
}
