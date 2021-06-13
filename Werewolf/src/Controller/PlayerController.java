package Controller;

import java.io.IOException;
import java.time.LocalTime;

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
            if (!gameController.isUsernameAvailable(username)) {
                clearScreen();
                sendMessage("This username has been already taken.");
                getCh();
                continue;
            }
            else if (!gameController.isUsernameValid(username)) {
                clearScreen();
                sendMessage("Username must have at least 4 characters.");
                getCh();
                continue;
            }
            break;
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
        clearStream();
        long lastPrintedTime = 0;
        while (true) {
            long remainingTime = duration.toSecondOfDay() - (LocalTime.now().toSecondOfDay() - startingTime.toSecondOfDay());
            if (remainingTime <= 0)
                break;
//            if (remainingTime % 5 == 0 && remainingTime != lastPrintedTime) {
//                System.out.println("Remaining time: " + remainingTime);
//                lastPrintedTime = remainingTime;
//            }

            if (!clientHandler.isStreamEmpty()) {
                Message message = getMessage();
                if (message.getBody().equals("end")) { // user wants to end his speaking
                    sendMessage("- Your speak has ended, please wait until other players' turn ");
                    break;
                }
                if (remainingTime < 90)
                    sendCustomMessage(remainingTime + " second(s) remaining.", false, false);
                if (clientHandler.isSocketClosed())
                    break;
                gameController.sendCustomMessageToAll(message, false, false);
            }
        }
//        System.out.println(player + " finished");
    }

    /**
     * sleep player
     * @param options call clearScreen, call getCh
     */
    public void sleep(boolean... options) {
        boolean callClearScreen = false, callGetCh = false;
        if (options.length > 0)
            callClearScreen = options[0];
        if (options.length > 1)
            callGetCh = options[1];
        player.setAwake(false);
        sendCustomMessage("You are asleep now, please wait until god informs you.", callClearScreen, callGetCh);
    }

    /**
     * wake the player up
     * @param options call clearScreen, call getCh
     */
    public void wakeup(boolean... options) {
        boolean callClearScreen = false, callGetCh = false;
        if (options.length > 0)
            callClearScreen = options[0];
        if (options.length > 1)
            callGetCh = options[1];
        player.setAwake(true);
        sendCustomMessage("You are awake now.", callClearScreen, callGetCh);
    }

    /**
     * get vote among the given number of candidates
     * @param numberOfCandidates number of candidates
     * @param finishingTime deadline time
     * @return final vote
     */
    public int vote(int numberOfCandidates, LocalTime finishingTime) {
        clearStream();
        sendMessage("Enter your vote: (enter 'end' to finalize your vote)");
        int result = 0;
        while (true) {
            if (LocalTime.now().compareTo(finishingTime) >= 0)
                break;
            if (clientHandler.isStreamEmpty())
                continue;
            Message receivedMessage = getMessage();
            String vote = receivedMessage.getBody();
            if (vote.equals("end"))
                break;
            try {
                int tmp = Integer.parseInt(vote);
                if (0 <= tmp && tmp <= numberOfCandidates) {
                    result = tmp;
                    sendMessage("- You have entered " + result + ".");
                }
                else
                    sendMessage("Invalid input.");
            }
            catch (NumberFormatException exception) {
                sendMessage("Invalid input.");
            }
        }
        return result;
    }

    /**
     * get an option from player between yes or no option
     * @param finishingTime choosing deadline
     * @return true, if player enters yes
     */
    public boolean ask(LocalTime finishingTime) {
        clearStream(); // to make sure that server does not send "Invalid input." message first
        while (LocalTime.now().compareTo(finishingTime) < 0) {
            if (clientHandler.isStreamEmpty())
                continue;
            String option = getMessage().getBody().toLowerCase().replaceAll(" ", "");
            if (option.equals("yes"))
                return true;
            else if (option.equals("no"))
                return false;
            sendMessage("Invalid input.");
        }
        return false;
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
        if (clientHandler.isSocketClosed()) {
            gameController.kickPlayer(player);
            return;
        }
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
        if (clientHandler.isSocketClosed()) {
            gameController.kickPlayer(player);
            return null;
        }
        Message receivedMessage = clientHandler.getMessage();
        if (clientHandler.isSocketClosed()) {
            gameController.kickPlayer(player);
            return null;
        }
        if (receivedMessage == null)
            return null;
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
     * clear input stream in clientHandler so old messages will be deleted
     */
    public void clearStream() {
        if (clientHandler == null)
            return;
        clientHandler.clearStream();
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
        //noinspection PatternVariableCanBeUsed
        PlayerController that = (PlayerController) o;
        return player.equals(that.getPlayer());
    }
}
