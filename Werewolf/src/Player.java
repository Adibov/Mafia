/**
 * Player class, implements players behaviours. like waking, sleeping, voting, etc
 * @author Adibov
 * @version 1.0
 */
abstract public class Player extends Person {
    /**
     * class constructor
     * @param username player username
     */
    public Player(String username) {
        super(username);
    }

    /**
     * main method, to make class runnable
     * @param args program args
     */
    public static void main(String[] args) {

    }

    /**
     * send the given message to the player
     * @param message given message
     */
    public void showMessage(Message message) {
        message.show();
    }


}