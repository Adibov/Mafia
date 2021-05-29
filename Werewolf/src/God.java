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
    transient private static God god;

    /**
     * class constructor
     */
    public God() {
        super("God");
        god = this;
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
            clearScreenForAll();
            notifyPlayers(new Message("Wait for other players to join, " + remainingPlayers + " players left.", this));
            Player newPlayer = Server.acceptNewPlayer(this);
            players.add(newPlayer);
            alivePlayers.add(newPlayer);
        }
        clearScreenForAll();
        notifyPlayers(new Message("All players have joined the game.", God.getGodObject()));
        getChForAll();
    }

    /**
     * kick the given player from game
     * @param player given player
     */
    public void kickPlayer(Player player) {
        if (alivePlayers.contains(player))
            alivePlayers.remove(player);
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
        for (Player player : alivePlayers)
            Server.sendMessage(message, player);
    }

    /**
     * call clearScreen method for all players
     */
    public void clearScreenForAll() {
        for (Player player : players)
            Server.clearScreen(player);
    }

    /**
     * call getCh method for all players
     */
    public void getChForAll() {
        for (Player player : players)
            Server.getCh(player);
    }

    /**
     * return god object
     * @return god object
     */
    public static God getGodObject() {
        return god;
    }
}
