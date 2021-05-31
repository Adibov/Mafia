package Controller;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.util.Objects;

/**
 * PlayerController class manages players action. Each player has his own PlayerController
 * @author Adibov
 * @version 1.0
 */
public class PlayerController {
    private Player player;
    private ClientHandler clientHandler;
    private final GameController gameController;

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
            sendMessage("Enter an username:");
            username = getMessage().getBody();
            if (gameController.isUsernameAvailable(username))
                break;
            clearScreen();
            sendMessage("This username has been already taken.");
            getCh();
        }
        player = new Player(username);
        clearScreen();
        sendMessage("You have successfully joined to the game.");
        getCh();
    }

    /**
     * enable chatroom by the given duration
     * @param duration given duration
     */
    public void talk(LocalTime duration) {
        talk(LocalTime.now(), duration);
    }

    /**
     * enable chatroom from starting time by the given duration
     * @param startingTime chatroom starting time
     * @param duration chatroom duration
     */
    public void talk(LocalTime startingTime, LocalTime duration) {
        LocalTime lastTimeRounded = startingTime; // last time which was multiple of quarters
        while (true) {
            long remainingTime = LocalTime.now().toSecondOfDay() - startingTime.toSecondOfDay();
            if (remainingTime >= duration.toSecondOfDay())
                break;
//            if (LocalTime.now().toSecondOfDay() - lastTimeRounded.toSecondOfDay() >=
//                    Setting.getChatroomRemainingTimeAlertCycle().toSecondOfDay()) {
//
//                lastTimeRounded = lastTimeRounded.minusSeconds(Setting.getChatroomRemainingTimeAlertCycle().toSecondOfDay());
//                sendCustomMessage(lastTimeRounded.getMinute() + " minute(s) and " +
//                        lastTimeRounded.getSecond() + " second(s) remaining.", false, false);
//            }

            if (!clientHandler.isStreamEmpty()) {
                Message message = getMessage();
                if (message.getBody().equals("end")) // user wants to end his speaking turn
                    break;
                System.out.println("Message sender: " + message.getSender());
                gameController.sendCustomMessageToAll(message, false, false);
            }
        }
        System.out.println(player + " finished");
    }

    /**
     * clear screen for the player
     */
    private void clearScreen() {
        sendMessage("\n".repeat(50));
    }

    /**
     * implement getCh method from C language. (waits until client press enter)
     */
    private void getCh() {
        sendMessage("Press Enter key to continue...");
        getMessage(); // drop
    }

    /**
     * send the given message to the client
     * @param message given message
     */
    private void sendMessage(Message message) {
        if (clientHandler == null)
            return;
        if (!clientHandler.isSocketAlive())
            gameController.kickPlayer(player);
        clientHandler.sendMessage(message);
    }

    /**
     * send the given text to player
     * @param messageBody given text
     */
    private void sendMessage(String messageBody) {
        sendMessage(new Message(messageBody, God.getInstance()));
    }

    /**
     * waits for the client to enter a message
     * @return entered message
     */
    public synchronized Message getMessage() {
        if (clientHandler == null)
            return null;
        Message receivedMessage = clientHandler.getMessage();
        if (player != null)
            receivedMessage.setSender(player);
        return receivedMessage;
    }

    /**
     * send the given message and run remaining methods
     * @param message given message
     * @param callClearScreen call clearScreen before sending
     * @param callGetCh call getCh method after sending
     */
    public synchronized void sendCustomMessage(Message message, boolean callClearScreen, boolean callGetCh) {
        if (callClearScreen)
            clearScreen();
        sendMessage(message);
        if (callGetCh)
            getCh();
    }

    /**
     * send the given text and run remaining methods
     * @param bodyMessage given text
     * @param callClearScreen call clearScreen before sending
     * @param callGetCh call getCh method after sending
     */
    public void sendCustomMessage(String bodyMessage, boolean callClearScreen, boolean callGetCh) {
        if (callClearScreen)
            clearScreen();
        sendMessage(bodyMessage);
        if (callGetCh)
            getCh();
    }

    /**
     * player getter
     * @return player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * player setter
     * @param player new player value
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /**
     * override equals method
     * @param o given object
     * @return boolean result
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerController)) return false;
        PlayerController that = (PlayerController) o;
        return player.equals(that.getPlayer());
    }
}
