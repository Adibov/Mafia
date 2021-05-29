import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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
            
            PlayerController newPlayerController = new PlayerController(this);
            Player newPlayer = newPlayerController.getPlayer();
            players.add(newPlayer);
            playerControllers.put(newPlayer, newPlayerController);

        }
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
}
