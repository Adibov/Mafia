package Controller;

import Roles.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameController class, controls the game, like accepting new players, sending messages to all players
 * @author Adibov
 * @version 1.0
 */
public class GameController {
    final static private ExecutorService playerThreads = Executors.newCachedThreadPool();
    private CopyOnWriteArrayList<Player> players;
    private ServerSocket serverSocket;
    private ConcurrentHashMap<Player, PlayerController> playerControllers;
    private int dayNumber; // how many days have been passed
    private DAYTIME daytime;


    /**
     * class constructor
     */
    public GameController() {
        players = new CopyOnWriteArrayList<>();
        playerControllers = new ConcurrentHashMap<>();
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
        int whileLoopCount = 0;
        AtomicInteger numberOfPlayers = new AtomicInteger(0); // define Atomic to make it usable in the following lambda function
        while (whileLoopCount < Setting.getNumberOfPlayers()) {
            PlayerController newPlayerController = new PlayerController(this);
            playerThreads.execute(() -> {
                newPlayerController.registerPlayer();
                Player newPlayer = null;
                //noinspection IdempotentLoopBody
                while (newPlayer == null)
                    newPlayer = newPlayerController.getPlayer();
                players.add(newPlayer);
                playerControllers.put(newPlayer, newPlayerController);
                clearAllScreens();

                numberOfPlayers.incrementAndGet();
                int remainingPlayer = Setting.getNumberOfPlayers() - numberOfPlayers.get();
                if (remainingPlayer > 0) {
                    notifyAllPlayers("Waiting for other players to join, " + remainingPlayer + " player(s) left.");
                }
            });
            try {
                //noinspection BusyWait
                Thread.sleep(Setting.getServerRefreshTime());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            whileLoopCount++;
        }
        while (players.size() < Setting.getNumberOfPlayers()) { // waits until all players have joined to the game
            try {
                //noinspection BusyWait
                Thread.sleep(Setting.getServerRefreshTime());
            }
            catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        clearAllScreens();
        notifyAllPlayers("All players have joined the game.");
        getChAllScreens();
        System.out.println("All players have joined the game.");
    }

    /**
     * distribute roles randomly
     */
    public void distributeRoles() {
        System.out.println("Starting to distribute roles.");
        clearAllScreens();
        notifyAllPlayers("God is distributing roles, please wait...");
        randomShuffle(players);

        final int numberOfMafias = Setting.getNumberOfMafias();
        for (int i = 0; i < numberOfMafias; i++) {
            Player player = players.get(i);
            clearPlayerScreen(player);
            //noinspection EnhancedSwitchMigration
            switch (i) {
                case 0:
                    player = new GodFather(player);
                    sendTextAndGetCh("You are God Father of the game.", player);
                    break;
                case 1:
                    player = new DoctorLecter(player);
                    sendTextAndGetCh("You are Doctor Lecter of the game.", player);
                    break;
                default:
                    player = new Mafia(player);
                    sendTextAndGetCh("You are Mafia of the game.", player);
            }
        }
        for (int i = numberOfMafias; i < players.size(); i++) {
            int citizenIndex = numberOfMafias - i;
            Player player = players.get(i);
            clearPlayerScreen(player);
            switch (citizenIndex) {
                case 0:
                    player = new Doctor(player);
                    sendTextAndGetCh("You are Doctor of the game.", player);
                    break;
                case 1:
                    player = new Detector(player);
                    sendTextAndGetCh("You are Detector of the game.", player);
                    break;
                case 2:
                    player = new Sniper(player);
                    sendTextAndGetCh("You are Sniper of the game.", player);
                    break;
                case 3:
                    player = new Mayor(player);
                    sendTextAndGetCh("You are Mayor of the game.", player);
                    break;
                case 4:
                    player = new Psychologist(player);
                    sendTextAndGetCh("You are Psychologist of the game.", player);
                    break;
                case 5:
                    player = new DieHard(player);
                    sendTextAndGetCh("You are Die Hard of the game.", player);
                    break;
                default:
                    player = new Citizen(player);
                    sendTextAndGetCh("You are Citizen of the game.", player);
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
     * clear screen of the given player
     * @param player given player
     */
    public void clearPlayerScreen(Player player) {
        playerThreads.execute(() -> playerControllers.get(player).clearScreen());
    }

    /**
     * clear screen for all players
     */
    public void clearAllScreens() {
        for (Player player : players)
            clearPlayerScreen(player);
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
     * send the given message to the corresponding player
     * @param message given message
     * @param player receiver player
     */
    public void sendMessageToPlayer(Message message, Player player) {
        playerThreads.execute(() -> playerControllers.get(player).sendMessage(message));
    }

    /**
     * send the given text to the corresponding player
     * @param messageBody message body
     * @param player receiver player
     */
    public void sendTextToPlayer(String messageBody, Player player) {
        sendMessageToPlayer(new Message(messageBody, God.getInstance()), player);
    }

    /**
     * send the given text to the corresponding player and call getCh method for the same player
     * @param messageBody given text
     * @param player corresponding player
     */
    public void sendTextAndGetCh(String messageBody, Player player) {
        PlayerController playerController = playerControllers.get(player);
        System.out.println("Player text: " + player + " , " + players + " , " + playerController + " , " + player.equals(players.get(0)));
        playerThreads.execute(() -> {
            playerController.sendMessageFromGod(messageBody);
            playerController.getCh();
        });
    }

    /**
     * send the given message to all players in the game
     * @param messageBody given message's body
     */
    public void notifyAllPlayers(String messageBody) {
        for (Player player : players)
            sendTextToPlayer(messageBody, player);
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
