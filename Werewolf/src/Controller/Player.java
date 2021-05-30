package Controller;

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
    private boolean isAlive;

    /**
     * class constructor, for making a player with no username
     */
    public Player() {
        super("");
        isAlive = true;
    }

    /**
     * class constructor
     * @param username player username
     */
    public Player(String username) {
        super(username);
        isAlive = true;
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
        while (true) {
            clearScreen();
            System.out.println("Enter server port, you want to connect:");
            int port = inputScanner.nextInt(); inputScanner.nextLine(); // drop
            try {
                client = new Client(this, port); // connection to the server established
                clearScreen();
                System.out.println("Connection established successfully.");
                getCh();
                break;
            }
            catch (IOException exception) {
                clearScreen();
                System.out.println("No such server with the entered port founded.");
                getCh();
            }
        }
    }

    /**
     * clear terminal screen for user
     */
    public void clearScreen() {
        System.out.println("\n".repeat(Setting.getClearScreenBlankLines()));
    }

    /**
     * waits for the user to press Enter
     */
    public void getCh() {
        System.out.println("Press Enter to continue...");
        inputScanner.nextLine();
    }

    /**
     * send the given message to the player
     * @param message given message
     */
    public void showMessage(Message message) {
        message.show();
    }

    /**
     * isAlive getter
     * @return isAlive
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * isAlive setter
     * @param alive isAlive new value
     */
    public void setAlive(boolean alive) {
        isAlive = alive;
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