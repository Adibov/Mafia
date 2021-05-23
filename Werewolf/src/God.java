import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * God class, represent god of the game. In other words, it manages game
 * @author Adibov
 * @version 1.0
 */
public class God extends Person {
    final transient private ArrayList<Player> players = new ArrayList<Player>();
    final transient private ArrayList<Player> alivePlayers = new ArrayList<Player>();
    final transient private static God god = new God();

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
        Server.runServer();
        waitForPlayersToJoin();
    }

    /**
     * wait for all players to join
     */
    public void waitForPlayersToJoin() {
        while (players.size() < Setting.getNumberOfPlayers()) {
            int remainingPlayers = Setting.getNumberOfPlayers() - players.size();
            notifyPlayers(new Message("Wait for other players to join, " + remainingPlayers + " players left.", this));
            Player newPlayer = Server.acceptNewPlayer(this);
            players.add(newPlayer);
            alivePlayers.add(newPlayer);
        }
        System.out.println("Tamam");
    }

    /**
     * check if the given username hasn't been taken
     * @param username new username
     * @return boolean result
     */
    public boolean isAvailableUsername(String username) {
        Player instancePlayer = new Player(username);
        return !players.contains(instancePlayer);
    }

    /**
     * send the given message to the players
     * @param message given message
     */
    public void notifyPlayers(Message message) {
        for (Player player : players)
            Server.sendMessage(message, player);
    }

    public static Person getGodObject() {
        return god;
    }
}
