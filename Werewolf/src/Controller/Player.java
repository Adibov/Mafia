package Controller;

import Roles.*;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.Locale;
import java.util.Objects;
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
    private boolean isAwake;
    private int recordCount;

    /**
     * class constructor, for making a player with no username
     */
    public Player() {
        super("");
        isAlive = true;
        isAwake = true;
        recordCount = 0;
    }

    /**
     * class constructor
     * @param username player username
     */
    public Player(String username) {
        super(username);
        isAlive = true;
        isAwake = true;
        recordCount = 0;
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
        while (client.isServerUp()) {
            String message = inputScanner.nextLine();
            client.sendMessage(new Message(message, this));
            if (message.equals("exit")) {
                client.setServerUp(false);
                break;
            }
        }
        client.closeSocket();
    }

    /**
     * connects player to the game
     */
    public void connectToGame() {
        while (true) {
            clearScreen();
            System.out.println("Enter server port, you want to connect:");
//            int port = inputScanner.nextInt(); inputScanner.nextLine(); // drop
            int port = 2021;
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
     * check if the player has the given role
     * @param role given role
     * @return boolean result
     */
    @SuppressWarnings("SpellCheckingInspection")
    public boolean hasRole(String role) {
        role = role.replaceAll(" ", ""); // delete all spaces in the string
        role = role.toLowerCase(); // make it lowercase to make it case insesitive
        //noinspection EnhancedSwitchMigration
        switch (role) {
            case "mafia":
                return this instanceof Mafia;
            case "godfather":
                return this instanceof GodFather;
            case "doctorlecter":
                return this instanceof DoctorLecter;
            case "citizen":
                return this instanceof Citizen;
            case "doctor":
                return this instanceof Doctor;
            case "detector":
                return this instanceof Detector;
            case "sniper":
                return this instanceof Sniper;
            case "mayor":
                return this instanceof Mayor;
            case "psychologist":
                return this instanceof Psychologist;
            case "diehard":
                return this instanceof DieHard;
            default:
                return role.equals("all");
        }
    }

    /**
     * check if the given player is in a same team (Citizen or Mafia)
     * @param player given player
     * @return boolean result
     */
    public boolean isInSameTeam(Player player) {
        return (this instanceof Mafia && player instanceof Mafia) ||
               (this instanceof Citizen && player instanceof Citizen);
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
     * isAwake getter
     * @return isAwake
     */
    public boolean isAwake() {
        return isAwake;
    }

    /**
     * recordCount getter
     * @return recordCount
     */
    public int getRecordCount() {
        return recordCount;
    }

    /**
     * isAwake setter
     * @param awake isAwake new value
     */
    public synchronized void setAwake(boolean awake) {
        isAwake = awake;
    }

    /**
     * isAlive setter
     * @param alive isAlive new value
     */
    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    /**
     * increment record count by one
     */
    public void incrementRecordCount() {
        recordCount++;
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

    /**
     * override hashCode method
     * @return hash result
     */
    @Override
    public int hashCode() {
        return username.hashCode();
    }
}