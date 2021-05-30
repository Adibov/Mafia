package Controller;

import javax.swing.text.Style;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

/**
 * GameController class, controls the game, like accepting new players, sending messages to all players
 */
public class GameController {
    final static private ExecutorService playerThreads = Executors.newCachedThreadPool();
    private CopyOnWriteArrayList<Player> players;
    private ServerSocket serverSocket;
    private HashMap<Player, PlayerController> playerControllers;
    private int dayNumber; // how many days have been passed
    private DAYTIME daytime;


    /**
     * class constructor
     */
    public GameController() {
        players = new CopyOnWriteArrayList<>();
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
        if (dayNumber == 1)
            startIntroductionDay();
    }

    /**
     * start preparation day. Get username from players and add them to the game
     */
    public void startPreparationDay() {
        startWaitingLobby();
        distributeRoles();
        dayNumber++;
    }

    /**
     * waits until all player join to the game
     */
    public void startWaitingLobby() {
        while (players.size() < Setting.getNumberOfPlayers()) {
            System.out.println("hey:" + players);
            PlayerController newPlayerController = new PlayerController(this);
            playerThreads.execute(() -> {
                        Player newPlayer = null;
                        newPlayerController.registerPlayer();
                        //noinspection IdempotentLoopBody
                        while (newPlayer == null)
                            newPlayer = newPlayerController.getPlayer();
                        players.add(newPlayer);
                        playerControllers.put(newPlayer, newPlayerController);
                        clearAllScreens();
                        int remainingPlayer = Setting.getNumberOfPlayers() - players.size();
                        if (remainingPlayer > 0)
                            notifyAllPlayers("Waiting for other players to join, " + remainingPlayer + " players left.");
                    }
            );
            try {
                //noinspection BusyWait
                Thread.sleep(Setting.getServerRefreshTime());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        clearAllScreens();
        notifyAllPlayers("All players have joined the game.");
        getChAllScreens();
    }

    /**
     * distribute roles randomly
     */
    public void distributeRoles() {
        clearAllScreens();
        notifyAllPlayers("God is distributing roles, please wait...");
        randomShuffle(players);
        for (int i = 0; i < Setting.getNumberOfMafias(); i++) {
            Player player = players.get(i);
            switch (i) {
                case 0:
                    break;
                case 1:
                    break;
                default:
                    ;
            }
        }

    }

    /**
     * shuffle the given list randomly
     * @param list given list
     */
    private void randomShuffle(CopyOnWriteArrayList<Player> list) {
        Random random = new Random();
        for (int i = 1; i < list.size(); i++) {
            int indx = random.nextInt(i + 1);
            Player tmp = list.get(indx);
            list.set(indx, list.get(i));
            list.set(i, tmp);
        }
    }

    /**
     * start introduction day.
     */
    public void startIntroductionDay() {

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
     * call getCh method for the given player
     * @param player given player
     */
    public void getCh(Player player) {
        playerThreads.execute(() -> playerControllers.get(player).getCh());
    }

    /**
     * call getCh method for all players
     */
    public void getChAllScreens() {
        for (Player player : players)
            getCh(player);
    }

    /**
     * send the given message to all players in the game
     * @param messageBody given message's body
     */
    public void notifyAllPlayers(String messageBody) {
        for (Player player : players)
            playerThreads.execute(() -> playerControllers.get(player).sendMessageFromGod(messageBody));
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
