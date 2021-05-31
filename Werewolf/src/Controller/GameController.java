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
    final private CopyOnWriteArrayList<Player> players; // list of the players, who has not been kicked out of the game yet
    private ServerSocket serverSocket;
    final private ConcurrentHashMap<Player, PlayerController> playerControllers; // maps each player to his PlayerController
    private int dayNumber; // how many days have been passed
    final private DAYTIME daytime; // daytime of the current game

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
        if (dayNumber == 1) {
            startIntroductionDay();

        }
    }

    /**
     * start preparation day. Get username from players and add them to the game
     */
    public void startPreparationDay() {
        startWaitingLobby();
        distributeRoles();
        sendCustomMessageToAll("Press Enter whenever you're ready to start game.", true, true);
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
                newPlayerController.setPlayer(newPlayer);
                players.add(newPlayer);
                playerControllers.put(newPlayer, newPlayerController);

                numberOfPlayers.incrementAndGet();
                int remainingPlayer = Setting.getNumberOfPlayers() - numberOfPlayers.get();
                if (remainingPlayer > 0)
                    sendCustomMessageToAll("Waiting for other players to join, " + remainingPlayer + " player(s) left.", true, false);
            });
            try {
                //noinspection BusyWait
                Thread.sleep(Setting.getSleepTime());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            whileLoopCount++;
        }
        while (players.size() < Setting.getNumberOfPlayers()) { // waits until all players have joined to the game
            try {
                //noinspection BusyWait
                Thread.sleep(Setting.getSleepTime());
            }
            catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
        sendCustomMessageToAll("All players have joined the game.", true, true);
        System.out.println("All players have joined the game.");
        sleep();
    }

    /**
     * distribute roles randomly
     */
    public void distributeRoles() {
        System.out.println("Starting to distribute roles.");
        sendCustomMessageToAll("God is distributing roles, please wait...", true, true);
        randomShuffle(players);

        final int numberOfMafias = Setting.getNumberOfMafias();
        for (int i = 0; i < numberOfMafias; i++) {
            Player player = players.get(i);
            //noinspection EnhancedSwitchMigration
            switch (i) {
                case 0:
                    player = new GodFather(player);
                    sendCustomMessageToPlayer("You are God Father of the game.", player, true, true);
                    break;
                case 1:
                    player = new DoctorLecter(player);
                    sendCustomMessageToPlayer("You are Doctor Lecter of the game.", player, true, true);
                    break;
                default:
                    player = new Mafia(player);
                    sendCustomMessageToPlayer("You are Mafia of the game.", player, true, true);
            }
        }
        for (int i = numberOfMafias; i < players.size(); i++) {
            int citizenIndex = i - numberOfMafias;
            Player player = players.get(i);
            //noinspection EnhancedSwitchMigration
            switch (citizenIndex) {
                case 0:
                    player = new Doctor(player);
                    sendCustomMessageToPlayer("You are Doctor of the game.", player, true, true);
                    break;
                case 1:
                    player = new Detector(player);
                    sendCustomMessageToPlayer("You are Detector of the game.", player, true, true);
                    break;
                case 2:
                    player = new Sniper(player);
                    sendCustomMessageToPlayer("You are Sniper of the game.", player, true, true);
                    break;
                case 3:
                    player = new Mayor(player);
                    sendCustomMessageToPlayer("You are Mayor of the game.", player, true, true);
                    break;
                case 4:
                    player = new Psychologist(player);
                    sendCustomMessageToPlayer("You are Psychologist of the game.", player, true, true);
                    break;
                case 5:
                    player = new DieHard(player);
                    sendCustomMessageToPlayer("You are Die Hard of the game.", player, true, true);
                    break;
                default:
                    player = new Citizen(player);
                    sendCustomMessageToPlayer("You are Citizen of the game.", player, true, true);
            }
        }
        sendCustomMessageToAll("Roles distribution has been finished.", true, true);
        System.out.println("Roles distribution has been finished.");
    }

    /**
     * shuffle the given list randomly
     * @param list given list
     */
    private void randomShuffle(CopyOnWriteArrayList<Player> list) {
        Random random = new Random();
        for (int i = 1; i < list.size(); i++) {
            int index = random.nextInt(i + 1);
            Player tmp = list.get(index);
            list.set(index, list.get(i));
            list.set(i, tmp);
        }
    }

    /**
     * start introduction day.
     */
    public void startIntroductionDay() {
        sendCustomMessageToAll("Introduction day started. Each player can speak for "
                    + Setting.getIntroductionDayTurnTime().toString() + " seconds in his turn.",
                    true, false);
        for (Player player : players) {
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                continue;
            sendCustomMessageToPlayer("It's your turn now, enter 'stop' to finish your turn.", player, false, false);
            playerController.talk(Setting.getIntroductionDayTurnTime());
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

    /**
     * kick the given player out of the game.
     * a player will kicked out of the game, when either disconnected or left the game
     * @param player given player
     */
    public void kickPlayer(Player player) {
        players.remove(player);
        playerControllers.remove(player);
        sendCustomMessageToAll("\n" + player + " has been kicked out/disconnected from game.\n", false, false);
    }

    /**
     * send the given message to the corresponding player and call remaining methods
     * @param message given message
     * @param player receiver player
     * @param callClearScreen call clearScreen before sending
     * @param callGetCh call getCH after sending
     */
    public void sendCustomMessageToPlayer(Message message, Player player, boolean callClearScreen, boolean callGetCh) {
        PlayerController playerController = playerControllers.get(player);
        if (playerController == null)
            return;
        playerThreads.execute(() -> {
            playerController.sendCustomMessage(message, callClearScreen, callGetCh);
        });
        sleep();
    }

    /**
     * send the given text to the corresponding player and call remaining methods
     * @param bodyMessage given text
     * @param player receiver player
     * @param callClearScreen call clearScreen before sending
     * @param callGetCh call getCH after sending
     */
    public void sendCustomMessageToPlayer(String bodyMessage, Player player, boolean callClearScreen, boolean callGetCh) {
        sendCustomMessageToPlayer(new Message(bodyMessage, God.getInstance(), daytime), player, callClearScreen, callGetCh);
    }

    /**
     * send the given message to all players and call remaining methods.
     * ATTENTION: this method calls sendMessage for every player in a new thread and waits until all messages have been
     * sent, so IT IS GUARANTEED THAT THIS THREAD WILL STOP UNTIL ALL PLAYERS HAVE RECEIVED MESSAGE
     * @param message given message
     * @param callClearScreen call clearScreen before sending
     * @param callGetCh call getCH after sending
     */
    public void sendCustomMessageToAll(Message message, boolean callClearScreen, boolean callGetCh) {
        AtomicInteger sentMessage = new AtomicInteger(0);
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.equals(message.getSender()))
                continue;
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                return;
            playerThreads.execute(() -> {
                playerController.sendCustomMessage(message, callClearScreen, callGetCh);
                sentMessage.incrementAndGet();
            });
        }
        while (sentMessage.get() < players.size())
            sleep();
    }

    /**
     * send the given text to all players and call remaining methods
     * @param bodyMessage given text
     * @param callClearScreen call clearScreen before sending
     * @param callGetCh call getCH after sending
     */
    public void sendCustomMessageToAll(String bodyMessage, boolean callClearScreen, boolean callGetCh) {
        sendCustomMessageToAll(new Message(bodyMessage, God.getInstance(), daytime), callClearScreen, callGetCh);
    }

    /**
     * sleep server for a short time
     */
    public void sleep() {
        try {
            Thread.sleep(Setting.getSleepTime());
        }
        catch (InterruptedException exception) {
            exception.printStackTrace();
        }
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
