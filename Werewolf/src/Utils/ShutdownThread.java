package Utils;

import Controller.Client;
import Controller.Player;

/**
 * ShutdownThread close output stream when player wants to exit program
 * @author Adibov
 * @version 1.0
 */
public class ShutdownThread extends Thread {
    private Player player;

    /**
     * class constructor
     * @param player player field
     */
    public ShutdownThread(Player player) {
        super();
        this.player = player;
    }

    /**
     * override start method
     */
    @Override
    public synchronized void start() {
        Client client = player.getClient();
        if (client == null)
            return;
        client.sendMessage("exit");
        client.setServerUp(false);
        client.closeSocket();
    }
}
