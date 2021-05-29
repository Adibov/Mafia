import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GameController class, controls the game, like accepting new players, sending messages to all players
 */
public class GameController {
    final static private ExecutorService playerThreads = Executors.newCachedThreadPool();
    private ArrayList<Player> players;
    private ServerSocket serverSocket;
    private HashMap<Player, PlayerController> playerControllers;
    private int dayNumber; // how many days have been passed
    private DAYTIME daytime;


    /**
     * class constructor
     */
    public GameController() {
        players = new ArrayList<>();
        playerControllers = new HashMap<>();
        dayNumber = 0;
        daytime = DAYTIME.DAY;
        try {
            serverSocket = new ServerSocket(2021);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * start game from the current state
     */
    public void startGame() {
        if (dayNumber == 0)
            startPreparationDay();

    }

    /**
     * start preparation day. Get username from players and add them to the game
     */
    public void startPreparationDay() {
        while (players.size() < Setting.getNumberOfPlayers()) {
            int remainingPlayer = Setting.getNumberOfPlayers() - players.size();
            clearAllScreens();
            notifyAllPlayers("Waiting for other players to join, " + remainingPlayer + " players left.");
            PlayerController newPlayerController = new PlayerController(this);
            playerThreads.execute(newPlayerController);

            Player newPlayer = null;
            while (newPlayer == null)
                newPlayer = newPlayerController.getPlayer();
            players.add(newPlayer);
            playerControllers.put(newPlayer, newPlayerController);
        }
        clearAllScreens();
        notifyAllPlayers("All players have joined the game.");
        getChAllScreens();
        dayNumber++;
    }

    /**
     * check if the given username hasn't been used
     * @param username given username
     * @return boolean result
     */
    public boolean isUsernameAvailable(String username) {
        for (Player player : players)
            if (player.getUsername().equals(username))
                return false;
        return true;
    }

    /**
     * clear screen for all players
     */
    public void clearAllScreens() {
        for (Player player : players)
            playerControllers.get(player).clearScreen();
    }

    /**
     * call getCh method for all players
     */
    public void getChAllScreens() {
        for (Player player : players)
            playerControllers.get(player).getCh();
    }

    /**
     * send the given message to all players in the game
     * @param messageBody given message's body
     */
    public void notifyAllPlayers(String messageBody) {
        for (Player player : players)
            playerControllers.get(player).sendMessageFromGod(messageBody);
    }

    /**
     * serverSocket getter
     * @return serverSocket
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    /**
     * dayNumber getter
     * @return dayNumber
     */
    public int getDayNumber() {
        return dayNumber;
    }
}
