package Controller;

import Roles.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameController class, controls the game, like accepting new players, sending messages to all players
 * @author Adibov
 * @version 1.0
 */
public class GameController {
    final static private ExecutorService playerThreads = Executors.newCachedThreadPool();
    private CopyOnWriteArrayList<Player> players; // list of the players, who has not been kicked out of the game yet
    private ServerSocket serverSocket;
    final private ConcurrentHashMap<Player, PlayerController> playerControllers; // maps each player to his PlayerController
    private int dayNumber; // how many days have been passed
    private DAYTIME daytime; // daytime of the current game

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
            sleepPlayers();
            wakeupPerson("Mafia");
            showAlivePlayersToGroup("Mafia", true, true, false);

        }
    }

    /**
     * start preparation day. Get username from players and add them to the game
     */
    public void startPreparationDay() {
        startWaitingLobby();
        distributeRoles();
        sendCustomMessageToAll("Press Enter whenever you're ready to start game.", true, true, true);
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
            sleep();
            whileLoopCount++;
        }
        while (players.size() < Setting.getNumberOfPlayers()) // waits until all players have joined to the game
            sleep();
//        sendCustomMessageToAll("All players have joined the game.", true, true);
        System.out.println("All players have joined the game.");
    }

    /**
     * distribute roles randomly
     */
    public void distributeRoles() {
        System.out.println("Starting to distribute roles.");
//        sendCustomMessageToAll("God is distributing roles, please wait...", true, true);
        randomShuffle(players);

        CopyOnWriteArrayList<Player> newPlayers = new CopyOnWriteArrayList<>();
        final int numberOfMafias = Setting.getNumberOfMafias(), numberOfPlayers = players.size();
        for (int i = 0; i < numberOfMafias; i++) {
            Player player = players.get(i), newRole = player;
            PlayerController playerController = playerControllers.get(player);
            //noinspection EnhancedSwitchMigration
            switch (i) {
                case 0:
                    newRole = new GodFather(newRole);
                    sendCustomMessageToPlayer("You are God Father of the game.", newRole, true, true);
                    break;
                case 1:
                    newRole = new DoctorLecter(newRole);
                    sendCustomMessageToPlayer("You are Doctor Lecter of the game.", newRole, true, true);
                    break;
                default:
                    newRole = new Mafia(newRole);
                    sendCustomMessageToPlayer("You are Mafia of the game.", newRole, true, true);
            }
            playerControllers.remove(player);
            newPlayers.add(newRole);
            playerControllers.put(newRole, playerController);
        }
        for (int i = numberOfMafias; i < numberOfPlayers; i++) {
            int citizenIndex = i - numberOfMafias;
            // to stop thread for the last receiver to make sure that all players have received message
            boolean isLastPlayer = (i == (numberOfPlayers - 1));
            Player player = players.get(i), newRole = player;
            PlayerController playerController = playerControllers.get(player);
            //noinspection EnhancedSwitchMigration
            switch (citizenIndex) {
                case 0:
                    newRole = new Doctor(newRole);
                    sendCustomMessageToPlayer("You are Doctor of the game.", newRole, true, true, isLastPlayer);
                    break;
                case 1:
                    newRole = new Detector(newRole);
                    sendCustomMessageToPlayer("You are Detector of the game.", newRole, true, true, isLastPlayer);
                    break;
                case 2:
                    newRole = new Sniper(newRole);
                    sendCustomMessageToPlayer("You are Sniper of the game.", newRole, true, true, isLastPlayer);
                    break;
                case 3:
                    newRole = new Mayor(newRole);
                    sendCustomMessageToPlayer("You are Mayor of the game.", newRole, true, true, isLastPlayer);
                    break;
                case 4:
                    newRole = new Psychologist(newRole);
                    sendCustomMessageToPlayer("You are Psychologist of the game.", newRole, true, true, isLastPlayer);
                    break;
                case 5:
                    newRole = new DieHard(newRole);
                    sendCustomMessageToPlayer("You are Die Hard of the game.", newRole, true, true, isLastPlayer);
                    break;
                default:
                    newRole = new Citizen(newRole);
                    sendCustomMessageToPlayer("You are Citizen of the game.", newRole, true, true, isLastPlayer);
            }
            playerControllers.remove(player);
            newPlayers.add(newRole);
            playerControllers.put(newRole, playerController);
        }
        players = newPlayers;
        randomShuffle(players); // so that players can't discover other players role by their talking turn :))
//        sendCustomMessageToAll("Roles distribution has been finished.", true, true);
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
                    + Setting.getIntroductionTurnTime().toString() + " seconds in his turn.",
                    true, false);
        for (Player player : players) {
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                continue;
            sendCustomMessageToPlayer("It's your turn now, enter 'stop' to finish your turn.", player, false, false);
            playerController.talk(Setting.getIntroductionTurnTime());
            sendCustomMessageToPlayer("Your turn has been finished.", player, false, false);
        }
        sendCustomMessageToAll("Introduction day finished.", true, false);
    }

    /**
     * make all players asleep
     */
    public void sleepPlayers() {
        sendCustomMessageToAll(
                "It's night now, all players have to sleep. Please wait until night ends.",
                false,
                false
        );
        for (Player player : players)
            player.setAwake(false);
        daytime = DAYTIME.NIGHT;
    }

    /**
     * wake a group of players
     * @param role role of the corresponding group
     */
    public void wakeupPerson(String role) {
        AtomicInteger finishedThread = new AtomicInteger(0);
        int threadCount = 0;
        for (Player player : players) {
            if (player.checkRole(role)) {
                PlayerController playerController = playerControllers.get(player);
                if (playerController == null)
                    continue;
                playerThreads.execute(() -> {
                    playerController.wakeup();
                    finishedThread.incrementAndGet();
                });
                threadCount++;
            }
        }
        while (finishedThread.get() < threadCount) // waits until all players wakeup
            sleep();
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
        if (player == null)
            return;
        players.remove(player);
        playerControllers.remove(player);
        sendCustomMessageToAll("\n" + player + " has been kicked out/disconnected from game.\n", false, false);
    }

    /**
     * show alive players to the corresponding player
     * @param targetPlayer target player
     * @param showRoles if true, will also show roles of players in same team
     * @param callClearScreen if true, calls clear screen method after finishing
     * @param callGetCh if true, calls getCh method after finishing
     */
    public void showAlivePlayersToPlayer(Player targetPlayer,
                                         boolean showRoles,
                                         boolean callClearScreen,
                                         boolean callGetCh) {
        int playerCount = 1;
        for (Player player : players) {
            if (player.equals(targetPlayer))
                continue;
            String message = playerCount + ") " + player;
            if (showRoles && targetPlayer.isInSameTeam(player))
                message += "(" + player.getClass() + ")";
            sendCustomMessageToPlayer(message, player, callClearScreen, callGetCh);
            playerCount++;
        }
    }

    /**
     * show alive players to a given group of players
     * @param role target players group
     * @param showRoles if true, will also show roles of players
     * @param callClearScreen if true, calls clear screen method after finishing
     * @param callGetCh if true, calls getCh method after finishing
     */
    public void showAlivePlayersToGroup(String role, boolean showRoles, boolean callClearScreen, boolean callGetCh) {
        AtomicInteger finishedThread = new AtomicInteger(0);
        int loopCounter = 0;
        for (Player player : players)
            if (player.checkRole(role)) {
                playerThreads.execute(() -> {
                    showAlivePlayersToPlayer(player, showRoles, callClearScreen, callGetCh);
                    finishedThread.incrementAndGet();
                });
                loopCounter++;
            }
        while (finishedThread.get() < loopCounter) // wait until all players have received send
            sleep();
    }

    /**
     * send the given message to the corresponding player and call remaining methods
     * @param message given message
     * @param player receiver player
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToPlayer(Message message, Player player, boolean... options) {
        PlayerController playerController = playerControllers.get(player);
        if (playerController == null)
            return;
        AtomicBoolean threadFinished = new AtomicBoolean(false); // define atomic to make it usable in lambda
        playerThreads.execute(() -> {
            playerController.sendCustomMessage(message, options[0], options[1]);
            threadFinished.set(true);
        });
        while (options.length > 2 && options[2] && !threadFinished.get()) // wait until player received message
            sleep();
    }

    /**
     * send the given text to the corresponding player and call remaining methods
     * @param bodyMessage given text
     * @param player receiver player
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToPlayer(String bodyMessage, Player player, boolean... options) {
        sendCustomMessageToPlayer(new Message(bodyMessage, God.getInstance(), daytime), player, options);
    }

    /**
     * send the given message to players with the corresponding role and call remaining methods.
     * @param message given message
     * @param role corresponding role
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToGroup(Message message, String role, boolean... options) {
        AtomicInteger sentMessage = new AtomicInteger(0);
        for (Player player : players) {
            if (!player.checkRole(role))
                continue;
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                return;
            playerThreads.execute(() -> {
                playerController.sendCustomMessage(message, options[0], options[1]);
                sentMessage.incrementAndGet();

            });
        }
        while (options.length > 2 &&
               options[2] &&
               sentMessage.get() < players.size()) // to make sure that all players have received sent message
            sleep();
    }

    /**
     * send the given text to players with the corresponding role and call remaining methods.
     * @param bodyMessage given text
     * @param role corresponding role
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToGroup(String bodyMessage, String role, boolean... options) {
        sendCustomMessageToGroup(new Message(bodyMessage, God.getInstance()), role, options);
    }

    /**
     * send the given message to all players and call remaining methods.
     * @param message given message
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToAll(Message message, boolean... options) {
        AtomicInteger sentMessage = new AtomicInteger(0);
        for (Player player : players) {
            if (player.equals(message.getSender()) || !player.isAwake()) {
                sentMessage.incrementAndGet();
                continue;
            }
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                return;
            playerThreads.execute(() -> {
                playerController.sendCustomMessage(message, options[0], options[1]);
                sentMessage.incrementAndGet();
            });
        }
        while (options.length > 2 &&
                options[2] &&
                sentMessage.get() < players.size()) // to make sure that all players have received sent message
            sleep();
    }

    /**
     * send the given text to all players and call remaining methods
     * @param bodyMessage given text
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToAll(String bodyMessage, boolean... options) {
        sendCustomMessageToAll(new Message(bodyMessage, God.getInstance(), daytime), options);
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
     * sleep server for a given time
     * @param delayTime given time in milli seconds
     */
    public void sleep(long delayTime) {
        try {
            Thread.sleep(delayTime);
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
