import java.io.IOException;
import java.net.Socket;

/**
 * Player class, implements players behaviours. like waking, sleeping, voting, etc
 * @author Adibov
 * @version 1.0
 */
public class Player extends Person {
    private Client client;

    /**
     * class constructor
     * @param username player username
     */
    public Player(String username) {
        super(username);
        client = new Client();
    }

    /**
     * main method, to make class runnable
     * @param args program args
     */
    public static void main(String[] args) {
        Player player = new Player("mammad");
        player.joinGame();
    }

    /**
     * join to the game
     */
    public void joinGame() {

    }

    /**
     * send the given message to the player
     * @param message given message
     */
    public void showMessage(Message message) {
        message.show();
    }

    /**
     * override equals method
     * @param obj given object
     * @return boolean result
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Player))
            return false;
        return this.getUsername().equals(((Player) obj).getUsername());
    }
}