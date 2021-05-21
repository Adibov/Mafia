import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * God class, represent god of the game. In other words, it manages game
 * @author Adibov
 * @version 1.0
 */
public class God extends Person {
    final private ArrayList<Player> players = new ArrayList<Player>();
    final private ArrayList<Player> alivePlayers = new ArrayList<Player>();

    /**
     * class constructor
     */
    public God() {
        super("God");
    }

    /**
     * start a new game
     */
    public void startNewGame() {
        waitForPlayersToJoin();
    }

    /**
     * wait for all players to join
     */
    public void waitForPlayersToJoin() {
        while (players.size() < Setting.getNumberOfPlayers()) {
            int remainingPlayers = Setting.getNumberOfPlayers() - players.size();
            notifyPlayers("Wait for other players to join, " + remainingPlayers + " players left.");
        }
    }

    /**
     * send the given message to the players
     * @param message given message
     */
    public void notifyPlayers(String message) {
        for (Player player : players)
            player.showMessage(new Message(message, this, player));
    }
}
