import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

/**
 * Player class, implements players behaviours. like waking, sleeping, voting, etc
 * @author Adibov
 * @version 1.0
 */
public class Player extends Person {
    private transient Client client;
    final transient private Scanner inputScanner = new Scanner(System.in);

    /**
     * class constructor, for making a player with no username
     */
    public Player() {
        super("");
    }

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
        Player player = new Player();
        player.joinGame();
    }

    /**
     * join to the game
     */
    public void joinGame() {
        connectToGame();
        client.start();
        while (true) {
            String message = inputScanner.nextLine();
            client.sendMessage(new Message(message, this));
        }
    }

    /**
     * connects player to the game
     */
    public void connectToGame() {
        client = new Client(this); // connection to the server established
        System.out.println("You have joined the game.");
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