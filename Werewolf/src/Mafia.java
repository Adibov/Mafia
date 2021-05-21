/**
 * Mafia class, handles mafia players in the game
 */
public class Mafia extends Player {
    /**
     * class constructor
     * @param username player username
     */
    public Mafia(String username) {
        super(username);
    }

    /**
     * send the given message to the player
     * @param message given message
     */
    @Override
    public void showMessage(Message message) {
        if (message.getMessageType() == MESSAGETYPE.ALL || message.getMessageType() == MESSAGETYPE.MAFIA)
            message.show();
    }
}
