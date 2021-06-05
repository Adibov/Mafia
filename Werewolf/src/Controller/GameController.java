package Controller;

import Roles.*;

import java.io.IOException;
import java.net.ServerSocket;
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
            startFirstNight();

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
            playerController.setPlayer(newRole);
        }
        for (int i = numberOfMafias; i < numberOfPlayers; i++) {
            int citizenIndex = i - numberOfMafias;
            Player player = players.get(i), newRole = player;
            PlayerController playerController = playerControllers.get(player);
            //noinspection EnhancedSwitchMigration
            switch (citizenIndex) {
                case 0:
                    newRole = new Doctor(newRole);
                    sendCustomMessageToPlayer("You are Doctor of the game.", newRole, true, true);
                    break;
                case 1:
                    newRole = new Detector(newRole);
                    sendCustomMessageToPlayer("You are Detector of the game.", newRole, true, true);
                    break;
                case 2:
                    newRole = new Sniper(newRole);
                    sendCustomMessageToPlayer("You are Sniper of the game.", newRole, true, true);
                    break;
                case 3:
                    newRole = new Mayor(newRole);
                    sendCustomMessageToPlayer("You are Mayor of the game.", newRole, true, true);
                    break;
                case 4:
                    newRole = new Psychologist(newRole);
                    sendCustomMessageToPlayer("You are Psychologist of the game.", newRole, true, true);
                    break;
                case 5:
                    newRole = new DieHard(newRole);
                    sendCustomMessageToPlayer("You are Die Hard of the game.", newRole, true, true);
                    break;
                default:
                    newRole = new Citizen(newRole);
                    sendCustomMessageToPlayer("You are Citizen of the game.", newRole, true, true);
            }
            playerControllers.remove(player);
            newPlayers.add(newRole);
            playerControllers.put(newRole, playerController);
            playerController.setPlayer(newRole);
            sleep();
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
     * start first night
     * DESCRIPTION: In this night, players wakeup in turn and
     */
    public void startFirstNight() {
        sleepPlayers();
        // mafias introduction
        if (Setting.getNumberOfMafias() > 1) { // make sure there exists at least two mafias in game
            wakeupGroup("Mafia");
            sendCustomGroupFilteredMessage("Mafias have woke up.", "Mafia", false, false);
            showAlivePlayersToGroup("Mafia", true, false, true, true);
            sleepGroup("Mafia");
            sendCustomGroupFilteredMessage("Mafias have slept.", "Mafia", false, false);
        }

        // doctor and mayor introduction
        if (Setting.getNumberOfPlayers() - Setting.getNumberOfMafias() > 3) { // make sure doctor and mayor are in the game
            wakeupGroup("Doctor");
            sendCustomGroupFilteredMessage("Doctor has woke up.", "Doctor", false, false);
            wakeupGroup("Mayor");
            sendCustomGroupFilteredMessage("Mayor has woke up.", "Mayor", false, false);
            AtomicInteger finishedThread = new AtomicInteger(0);
            playerThreads.execute(() -> {
                showAlivePlayersToGroup("Doctor", true, false, true, true);
                finishedThread.incrementAndGet();
            });
            playerThreads.execute(() -> {
                showAlivePlayersToGroup("Mayor", true, false, true, true);
                finishedThread.incrementAndGet();
            });
            while (finishedThread.get() < 2)
                sleep();
            sleepGroup("Doctor");
            sendCustomGroupFilteredMessage("Doctor has slept.", "Doctor", false, false);
            sleepGroup("Mayor");
            sendCustomGroupFilteredMessage("Mayor has slept.", "Mayor", false, false);
        }
        sendCustomMessageToAll("Night has been finished.");
        System.out.println("First night has been finished.");
    }

    /**
     * make all players asleep
     */
    public void sleepPlayers() {
        sendCustomMessageToAll(
                "You have to sleep now, please wait until god inform you.",
                false,
                false
        );
        for (Player player : players)
            player.setAwake(false);
        daytime = DAYTIME.NIGHT;
    }

    /**
     * sleep players with the given role
     * @param role given role
     * @param options waits until all players sleep
     */
    public void sleepGroup(String role, boolean... options) {
        AtomicInteger finishedThread = new AtomicInteger(0);
        int loopCounter = 0;
        for (Player player : players) {
            if (player.hasRole(role))
                playerThreads.execute(() -> {
                    sendCustomMessageToPlayer(
                            "You have to sleep now, please wait until god inform you.",
                            player,
                            false, false
                    );
                    finishedThread.incrementAndGet();
                });
            loopCounter++;
        }
        while (options.length > 0 && options[0] && finishedThread.get() < loopCounter)
            sleep();
    }

    /**
     * wake a group of players
     * @param role role of the corresponding group
     */
    public void wakeupGroup(String role) {
        AtomicInteger finishedThread = new AtomicInteger(0);
        int threadCount = 0;
        for (Player player : players) {
            if (player.hasRole(role)) {
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
     * check if the given username can register to the game
     * @param username given username
     * @return boolean result
     */
    public boolean isUsernameValid(String username) {
        return username.length() > 3;
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
     * @param options call clearScreen, call getCh
     */
    public void showAlivePlayersToPlayer(Player targetPlayer, boolean showRoles, boolean... options) {
        int playerCount = 1;
        StringBuilder message = new StringBuilder();
        for (Player player : players) {
            if (player.equals(targetPlayer)) // doesn't show himself
                continue;
            message.append(playerCount).append(") ").append(player);
            if (showRoles && targetPlayer.isInSameTeam(player) && player.isAwake()) {
                String role = player.getClass().toString().split("\\.")[1];
                message.append(" (").append(role).append(")");
            }
            message.append("\n");
            playerCount++;
        }
        sendCustomMessageToPlayer(message.toString(), targetPlayer, options);
    }

    /**
     * show alive players to a given group of players
     * @param role target players group
     * @param showRoles if true, will also show roles of players
     * @param options call clearScreen, call getCh, wait for all message to be sent
     */
    public void showAlivePlayersToGroup(String role, boolean showRoles, boolean... options) {
        boolean callClearScreen = false, callGetCh = false, waitForResponse = false;
        if (options.length > 0)
            callClearScreen = options[0];
        if (options.length > 1)
            callGetCh = options[1];
        if (options.length > 2)
            waitForResponse = options[2];
        AtomicInteger finishedThread = new AtomicInteger(0);
        int loopCounter = 0;
        for (Player player : players)
            if (player.hasRole(role)) {
                boolean finalCallClearScreen = callClearScreen;
                boolean finalCallGetCh = callGetCh;
                boolean finalWaitForResponse = waitForResponse;
                playerThreads.execute(() -> {
                    showAlivePlayersToPlayer(player, showRoles, finalCallClearScreen, finalCallGetCh, finalWaitForResponse);
                    finishedThread.incrementAndGet();
                });
                loopCounter++;
            }
        while (waitForResponse && finishedThread.get() < loopCounter) // wait until all players have received message
            sleep();
    }

    /**
     * send the given message to the corresponding player and call remaining methods
     * @param message given message
     * @param player receiver player
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToPlayer(Message message, Player player, boolean... options) {
        boolean callClearScreen = false, callGetCh = false;
        if (options.length > 0)
            callClearScreen = options[0];
        if (options.length > 1)
            callGetCh = options[1];
        PlayerController playerController = playerControllers.get(player);
        if (playerController == null)
            return;
        AtomicBoolean threadFinished = new AtomicBoolean(false); // define atomic to make it usable in lambda
        boolean finalCallClearScreen = callClearScreen;
        boolean finalCallGetCh = callGetCh;
        playerThreads.execute(() -> {
            playerController.sendCustomMessage(message, finalCallClearScreen, finalCallGetCh);
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
     * send the given message to all players, EXCEPT players with the corresponding role and call remaining methods.
     * @param message given message
     * @param exceptionRole ignored role
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomGroupFilteredMessage(Message message, String exceptionRole, boolean... options) {
        AtomicInteger sentMessage = new AtomicInteger(0);
        for (Player player : players) {
            if (player.hasRole(exceptionRole))
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
     * send the given text to all players, EXCEPT players with the corresponding role and call remaining methods.
     * @param bodyMessage given text
     * @param exceptionRole ignored role
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomGroupFilteredMessage(String bodyMessage, String exceptionRole, boolean... options) {
        sendCustomGroupFilteredMessage(new Message(bodyMessage, God.getInstance()), exceptionRole, options);
    }

    /**
     * send the given message to all players and call remaining methods.
     * @param message given message
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToAll(Message message, boolean... options) {
        boolean callClearScreen = false, callGetCh = false;
        if (options.length > 0)
            callClearScreen = options[0];
        if (options.length > 1)
            callGetCh = options[1];
        AtomicInteger sentMessage = new AtomicInteger(0);
        for (Player player : players) {
            if (player.equals(message.getSender())) {
                sentMessage.incrementAndGet();
                continue;
            }
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                return;
            boolean finalCallClearScreen = callClearScreen;
            boolean finalCallGetCh = callGetCh;
            playerThreads.execute(() -> {
                playerController.sendCustomMessage(message, finalCallClearScreen, finalCallGetCh);
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
