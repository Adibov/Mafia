package Main;

import Controller.Player;
import Utils.ShutdownThread;

/**
 * PlayerMain class, each player have to run this class to join game
 * @author Adibov
 * @version 1.0
 */
public class PlayerMain {
    /**
     * main method, to make class runnable
     * @param args program args
     */
    public static void main(String[] args) {
        Player player = new Player();
        Runtime.getRuntime().addShutdownHook(new ShutdownThread(player));
        player.joinGame();
    }
}
