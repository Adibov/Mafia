package Controller;

import Roles.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalTime;
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
    static private ExecutorService playerThreads;
    private CopyOnWriteArrayList<Player> players; // list of the players, who has not been kicked out of the game yet
    private ServerSocket serverSocket;
    final private ConcurrentHashMap<Player, PlayerController> playerControllers; // maps each player to his PlayerController
    private int dayNumber; // how many days have been passed
    private DAYTIME daytime; // daytime of the current game
    // last night actions:
    private Player mafiasTarget;
    private Player doctorLecterTarget;
    private Player doctorTarget;
    private Player sniperTarget;
    private Player psychologistTarget;
    private boolean dieHardHasInquire;

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
            playerThreads = Executors.newCachedThreadPool();
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
        while (!isGameFinished()) {
            startRegularDay();
            if (isGameFinished())
                break;
            startRegularNight();
        }
        finishGame();
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
     * DESCRIPTION: god distribute roles, based on the number of players/mafias
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
            if (i == 0) {
                newRole = new GodFather(newRole);
                sendCustomMessageToPlayer("You are God Father of the game.", newRole, true, true);
            }
            else if (i == 1 && Setting.getNumberOfPlayers() > 2) { // make sure sniper exists in game
                newRole = new DoctorLecter(newRole);
                sendCustomMessageToPlayer("You are Doctor Lecter of the game.", newRole, true, true);
            }
            else {
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
     * DESCRIPTION: in this day, players talk in turn and introduce themself to the other players.
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
        sendCustomMessageToAll("Introduction day finished.", true, false, true);
    }

    /**
     * start first night
     * DESCRIPTION: In this night, players wakeup in turn and get to know each other
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
            Player doctor = getPlayerByRole("Doctor");
            doctor.setAwake(true); // to make it visible for mayor
            wakeupGroup("Mayor");
            sendCustomGroupFilteredMessage("Mayor has woke up.", "Mayor", false, false);
            showAlivePlayersToGroup("Mayor", true, false, true, true);
            doctor.setAwake(false);
            sleepGroup("Mayor");
            sendCustomGroupFilteredMessage("Mayor has slept.", "Mayor", false, false);
        }
//        sendCustomMessageToAll("Night has been finished.", true, true);
        System.out.println("First night has been finished.");
        daytime = DAYTIME.DAY;
    }

    /**
     * start a regular day of the game
     * PHASES:
     * 1. wakeup all players
     * 2. report last night events
     * 3. let them discuss about themself/events
     * 4. hold a voting for kicking a player
     */
    public void startRegularDay() {
        wakeupGroup("All", true, false, true);
        reportLastNightEvents();
        showAlivePlayersToGroup("All", false, false, false, true);
        startChatroom();
        holdVoting();
    }

    /**
     * report last night events to players
     * PHASES:
     * 1. check if mafias' target has dead
     * 2. check if sniper's target has dead
     * 3. mute psychologist's target
     * 4. announce status of the game if diehard wants to
     */
    public void reportLastNightEvents() {
        killMafiasTarget();
        killSniperTarget();
        mutePsychologistTarget();
        if (isGameFinished())
            finishGame();
        announceGameStatus();
    }

    /**
     * kill mafias' target if doctor survived wrong person
     */
    public void killMafiasTarget() {
        if (mafiasTarget != null) {
            Player deadPlayer = mafiasTarget;
            if (doctorTarget != null && doctorTarget.equals(mafiasTarget))
                deadPlayer = null;
            if (deadPlayer != null)
                killPlayer(deadPlayer);
        }
    }

    /**
     * kill sniper's target if doctor lecter survived wrong mafia
     */
    public void killSniperTarget() {
        if (sniperTarget != null) {
            Player deadPlayer = sniperTarget;
            if (doctorLecterTarget != null && doctorLecterTarget.equals(sniperTarget))
                deadPlayer = null;
            if (deadPlayer != null) {
                if (deadPlayer.hasRole("Mafia"))
                    killPlayer(deadPlayer);
                else {
                    Player sniper = getPlayerByRole("Sniper");
                    if (sniper != null)
                        killPlayer(sniper);
                }
            }
        }
    }

    /**
     * mute psychologist's target
     */
    public void mutePsychologistTarget() {
        if (psychologistTarget != null) {
            psychologistTarget.setMute(true);
            sendCustomMessageToAll(
                    psychologistTarget+ " is mute, he/she cannot talk during this day.",
                    false, false, true
                    );
        }
    }

    /**
     * announce status of the game if diehard has inquired
     */
    public void announceGameStatus() {
        if (!dieHardHasInquire)
            return;
        int mafiasNumber = 0, citizensNumber = 0;
        for (Player player : players)
            if (player.isAlive() && player.hasRole("Mafia"))
                mafiasNumber++;
            else if (player.isAlive())
                citizensNumber++;

        int deadMafias = Setting.getNumberOfMafias() - mafiasNumber;
        int deadCitizens = Setting.getNumberOfCitizens() - citizensNumber;
        sendCustomMessageToAll("Until now, " + (deadMafias + deadCitizens) + " have been died. " +
                deadCitizens + " of them were citizen and " + deadMafias + " of them were mafia.",
                false, false, true);
    }

    /**
     * start chatroom, so players can talk to each other
     * DESCRIPTION: all players will wakeup and discuss to each other.
     */
    public void startChatroom() {
        sendCustomMessageToAll(
                "Discussion phase started, all players can speak for " +
                        Setting.getDiscussionPhaseTime().getMinute() +
                        " minutes. send 'end' word to finish speaking.",
                false, false, true);
        LocalTime startingTime = LocalTime.now();
        int loopCounter = 0;
        AtomicInteger finishedThread = new AtomicInteger(0);
        for (Player player : players) {
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                continue;
            playerThreads.execute(() -> {
                playerController.talk(startingTime, Setting.getDiscussionPhaseTime());
                finishedThread.incrementAndGet();
            });
            loopCounter++;
        }
        while (finishedThread.get() < loopCounter)
            sleep();
//        sendCustomMessageToAll("Discussion time finished.", true, true, true);
    }

    /**
     * hold a voting for kicking a player
     * PHASES:
     * 1. show alive players to every player
     * 2. let them choose no more than one player
     * 3. ask from mayor to make voting invalid or not
     * 4. report the result of the voting to the players
     */
    public void holdVoting() {
        System.out.println("Start voting.");
        sendCustomMessageToAll(
                "Voting phase started, you can vote for " + Setting.getVotingTime().toSecondOfDay() +
                        " seconds. Send '0' for settle on abstention and 'end' to make your vote finalize.",
                true, false, true);
        // how many players have voted to the target player
        ConcurrentHashMap<Player, Integer> voteCount = new ConcurrentHashMap<>();
        LocalTime startingTime = LocalTime.now();
        int loopCount = 0;
        AtomicInteger finishedThread = new AtomicInteger(0);
        for (Player player : players)
            if (player.isAlive()) {
                playerThreads.execute(() -> {
                    Player votedPlayer = getVote(
                            player,
                            startingTime.plusSeconds(Setting.getVotingTime().toSecondOfDay()),
                            "All",
                            "All",
                            false
                    );
                    if (votedPlayer != null) {
                        if (!voteCount.containsKey(votedPlayer))
                            voteCount.put(votedPlayer, 1);
                        else {
                            int count = voteCount.get(votedPlayer) + 1;
                            voteCount.remove(votedPlayer);
                            voteCount.put(votedPlayer, count);
                        }
                    }
                    finishedThread.incrementAndGet();
                });
                loopCount++;
            }
        while (finishedThread.get() < loopCount) // waits until all players have voted
            sleep();
        if (voteCount.keySet().size() == 0)
            sendCustomMessageToAll("No one will die this turn.", true, true, true);
        else {
            ArrayList<Player> ranking = showStanding(voteCount);
            killPlayerByRanking(ranking, voteCount);
        }
        System.out.println("Finished voting.");
    }

    /**
     * get a vote of the given player
     * @param voterPlayer given player
     * @param finishingTime voting deadline
     * @param candidatesRole only show players with this roles
     * @param receiversRole only show votes to those players with the corresponding role
     * @param showHimself if true, will also show himself in candidate list
     * @return voted player
     */
    public Player getVote(Player voterPlayer, LocalTime finishingTime, String candidatesRole, String receiversRole, boolean showHimself) {
        ArrayList<Player> candidatePlayers = new ArrayList<>();
        for (Player player : players) {
            if ((player.equals(voterPlayer) && !showHimself) || !player.isAlive() || !player.hasRole(candidatesRole))
                continue;
            candidatePlayers.add(player);
        }
        showAlivePlayersToPlayer(voterPlayer, false, showHimself, false, false, true);
        PlayerController playerController = playerControllers.get(voterPlayer);
        if (playerController == null)
            return null;
        int voteIndex = playerController.vote(candidatePlayers.size(), finishingTime);
        if (voteIndex == 0) {
            Message message = new Message("I've settled on abstention.", voterPlayer);
            sendCustomMessageToPlayer("You have settled on abstention.", voterPlayer, false, true, true);
            sendCustomMessageToAll(message, false, false, true);
            return null;
        }

        Player votedPlayer = candidatePlayers.get(voteIndex - 1);
        Message message = new Message("I've voted for " + votedPlayer + ".", voterPlayer);
        sendCustomMessageToPlayer("You have voted for " + votedPlayer + ".", voterPlayer, false, true, true);
        sendCustomMessageToGroup(message, receiversRole, false, false, true);
        return votedPlayer;
    }

    /**
     * show vote standing and return ranking list
     * @param voteCount a hashmap that maps players to the number of votes
     * @return ranking list ordered by voting count in decreasing
     */
    public ArrayList<Player> showStanding(ConcurrentHashMap<Player, Integer> voteCount) {
        ArrayList<Player> ranking = new ArrayList<>();
        for (Player player : voteCount.keySet()) {
            ranking.add(player);
            int index = ranking.size() - 1;
            while (index > 0 && voteCount.get(ranking.get(index - 1)) < voteCount.get(ranking.get(index))) {
                Player tmp = ranking.get(index);
                ranking.set(index, ranking.get(index - 1));
                ranking.set(index - 1, tmp);
            }
        }
        int num = 1;
        StringBuilder result = new StringBuilder("Ranking:\n").append(" ".repeat(15))
                .append("Vote Count").append(" ".repeat(15)).append("Record Count\n");
        for (Player player : ranking) {
            int spaceCount = 12 - player.getUsername().length() + 4;
            result.append("#").append(num).append(" ").append(player).append(" ".repeat(spaceCount)).append(voteCount.get(player))
                    .append(" ".repeat(25)).append(player.getRecordCount()).append("\n");
            num++;
        }
        sendCustomMessageToAll(result.toString(), true, true, true);
        return ranking;
    }

    /**
     * kill players by the given ranking.
     * ATTENTION: among those players with the most voting count, player with the most record count will die. if
     * at least two of those players have the same record count, NO ONE WILL DIE THIS TURN. And record count of those
     * players who reached among the candidates will also increment by one.
     * @param ranking ranking list ordered by voting count
     * @param voteCount maps each player to his vote count
     */
    public void killPlayerByRanking(ArrayList<Player> ranking, ConcurrentHashMap<Player, Integer> voteCount) {
        int index = 1, maximumRecordCount = ranking.get(0).getRecordCount();
        while (index < ranking.size() && voteCount.get(ranking.get(index)).equals(voteCount.get(ranking.get(0)))) {
            int recordCount = ranking.get(index).getRecordCount();
            maximumRecordCount = Math.max(recordCount, maximumRecordCount);
            index++;
        }

        Player resultPlayer = ranking.get(0);
        int maximumCount = 0;
        for (int i = 0; i < index; i++) {
            ranking.get(i).incrementRecordCount();
            if (ranking.get(i).getRecordCount() == maximumRecordCount) {
                resultPlayer = ranking.get(i);
                maximumCount++;
            }
        }
        if (maximumCount > 1) {
            sendCustomMessageToAll("No one will die this turn.", true, true, true);
            return;
        }
        if (resultPlayer == null)
            return;
        killPlayer(resultPlayer);
    }

    /**
     * kill the given player. Indeed, player can choose to leave game (kick out of it) or continue watching it.
     * @param deadPlayer given player
     */
    public void killPlayer(Player deadPlayer) {
        deadPlayer.setAlive(false);
        String message = """
                You have died. Please choose one option:
                1) watch game till the end
                2) leave game""";
        sendCustomMessageToPlayer(message, deadPlayer, true, false, true);
        while (true) {
            int option = Integer.parseInt(getMessageFromPlayer(deadPlayer).getBody());
            if (option < 1 || option > 2) {
                sendCustomMessageToPlayer("Invalid input.", deadPlayer, false, false, true);
                continue;
            }
            if (option == 2) {
                kickPlayer(deadPlayer);
                return;
            }
            break;
        }
        sendCustomMessageToAll(deadPlayer + " has died.", false, true, true);
    }

    /**
     * start a regular day of the game
     * PHASES:
     * 1. sleep all players
     * 2. wakeup mafias and let them kill one person
     * 3. wakeup doctor lecter and let him survive a mafia
     * 4. wakeup citizen's doctor and let him survive a citizen
     * 5. wakeup detector and let him inquire a mafia
     * 5. wakeup sniper and ask him whether he wants to shoot or not
     * 6. wakeup psychologist and let him make someone silent
     * 7. wakeup diehard and ask him whether he wants god to announce status of the game
     */
    public void startRegularNight() {
        sleepGroup("All", true, false, true);
        mafiasTarget = startMafiasTurn();
        doctorLecterTarget = null;
        if (Setting.getNumberOfMafias() > 1) // make sure doctor lecter exists in game
            doctorLecterTarget = startDoctorLecterTurn();
        doctorTarget = startDoctorTurn();
        startDetectorTurn();
        sniperTarget = null;
        if (Setting.getNumberOfPlayers() - Setting.getNumberOfMafias() > 2) // make sure sniper exists in game
            sniperTarget = startSniperTurn();
        psychologistTarget = null;
        if (Setting.getNumberOfPlayers() - Setting.getNumberOfMafias() > 3) // make sure psychologist exits in game
            psychologistTarget = startPsychologistTurn();
        dieHardHasInquire = false;
        if (Setting.getNumberOfPlayers() - Setting.getNumberOfMafias() > 4) // make sure diehard exists in game
            dieHardHasInquire = startDieHardTurn();
    }

    /**
     * start mafias turn and let them choose one player to kill
     * @return target player
     */
    public Player startMafiasTurn() {
        wakeupGroup("Mafia", true, false, true);
        sendCustomGroupFilteredMessage("Mafias are awake now.", "Mafia", false, false, true);
        sendCustomMessageToGroup("Choose a person to kill:", "Mafia", false, false, true);
        // maps each mafia to the player which he voted for
        LocalTime finishingTime = LocalTime.now().plusSeconds(Setting.getNighActionTime().toSecondOfDay());
        ConcurrentHashMap<Player, Player> voteMap = new ConcurrentHashMap<>();
        AtomicInteger finishedThread = new AtomicInteger(0);
        int loopCounter = 0;
        for (Player player : players)
            if (player.hasRole("Mafia")) {
                playerThreads.execute(() -> {
                    Player votedPlayer = getVote(player, finishingTime, "All", "Mafia", false);
                    if (votedPlayer != null)
                        voteMap.put(player, votedPlayer);
                    finishedThread.incrementAndGet();
                });
                loopCounter++;
            }
        while (finishedThread.get() < loopCounter)
            sleep();

        sleepGroup("Mafia", true, false, true);
        sendCustomGroupFilteredMessage("Mafias are asleep now.", "Mafia", false, false, true);
        Player godFather = getPlayerByRole("GodFather"), doctorLecter = getPlayerByRole("DoctorLecter");
        if (godFather != null)
            return voteMap.get(godFather);
        if (doctorLecter != null)
            return voteMap.get(doctorLecter);
        for (Player player : voteMap.values())
            return player;
        return null;
    }

    /**
     * start doctor lectern's turn and let him to choose one mafia to survive
     * @return survived player
     */
    public Player startDoctorLecterTurn() {
        sendCustomGroupFilteredMessage("Doctor Lecter is awake now.", "DoctorLecter", false, false, true);
        DoctorLecter doctorLecter = (DoctorLecter) getPlayerByRole("DoctorLecter");
        if (doctorLecter == null || !doctorLecter.isAlive()) {
            // sleep for a random time to prevent other players from finding out dead roles
            sleepRandomTime(Setting.getNighActionTime().toSecondOfDay() * 1000L);
            sendCustomGroupFilteredMessage("Doctor Lecter is asleep now.", "DoctorLecter", false, false, true);
            return null;
        }
        wakeupGroup("DoctorLecter", true, false, true);

        sendCustomMessageToPlayer("Choose a mafia to survive:", doctorLecter, false, false, true);
        Player survivedPlayer = null;
        LocalTime finishingTime = LocalTime.now().plusSeconds(Setting.getNighActionTime().toSecondOfDay());
        survivedPlayer = getVote(doctorLecter, finishingTime, "Mafia", "Mafia", !doctorLecter.hasHealedHimself());
        sleepGroup("DoctorLecter", true, false, true);
        sendCustomGroupFilteredMessage("Doctor Lecter is asleep now.", "DoctorLecter", false, false, true);
        if (survivedPlayer != null && survivedPlayer.equals(doctorLecter))
            doctorLecter.setHasHealedHimself(true);
        return survivedPlayer;
    }

    /**
     * start doctor's turn and let him choose one citizen to revive.
     * @return survived player
     */
    public Player startDoctorTurn() {
        sendCustomGroupFilteredMessage("Doctor is awake now.", "Doctor", false, false, true);
        Doctor doctor = (Doctor) getPlayerByRole("Doctor");
        if (doctor == null || !doctor.isAlive()) {
            // sleep for a random time to prevent other players from finding out dead roles
            sleepRandomTime(Setting.getNighActionTime().toSecondOfDay() * 1000L);
            sendCustomGroupFilteredMessage("Doctor is asleep now.", "Doctor", false, false, true);
            return null;
        }
        wakeupGroup("Doctor", true, false, true);

        sendCustomMessageToPlayer("Choose a player to survive:", doctor, false, false, true);
        Player survivedPlayer = null;
        LocalTime finishingTime = LocalTime.now().plusSeconds(Setting.getNighActionTime().toSecondOfDay());
        survivedPlayer = getVote(doctor, finishingTime, "All", "None", !doctor.hasHealedHimself());
        sleepGroup("Doctor", true, false, true);
        sendCustomGroupFilteredMessage("Doctor is asleep now.", "Doctor", false, false, true);
        if (survivedPlayer != null && survivedPlayer.equals(doctor))
            doctor.setHasHealedHimself(true);
        return survivedPlayer;
    }

    /**
     * start detector's turn and let him inquire about mafias
     */
    public void startDetectorTurn() {
        Detector detector = (Detector) getPlayerByRole("Detector");
        sendCustomGroupFilteredMessage("Detector is awake.", "Detector", false, false, true);
        if (detector == null || !detector.isAlive()) {
            // sleep for a random time to prevent other players from finding out dead roles
            sleepRandomTime(Setting.getNighActionTime().toSecondOfDay() * 1000L);
            sendCustomGroupFilteredMessage("Detector is asleep.", "Detector", false, false, true);
            return;
        }
        wakeupGroup("Detector", true, false, true);
        sendCustomMessageToPlayer("Choose one player to inquire:", detector, false, false, true);
        LocalTime finishingTime = LocalTime.now().plusSeconds(Setting.getNighActionTime().toSecondOfDay());
        Player inquiredPlayer = getVote(detector, finishingTime, "All", "None", false);
        sendCustomGroupFilteredMessage("Detector is asleep.", "Detector", false, false, true);
        if (inquiredPlayer == null) {
            sleepGroup("Detector", true, false, true);
            return;
        }
        if (inquiredPlayer.hasRole("Mafia") && !inquiredPlayer.hasRole("GodFather"))
            sendCustomMessageToPlayer("Inquire result for " + inquiredPlayer + " is positive.", detector, false, true, true);
        else
            sendCustomMessageToPlayer("Inquire result for " + inquiredPlayer + " is negative.", detector, false, true, true);
        sleepGroup("Detector", true, false, true);
    }

    /**
     * start sniper's turn and ask him whether he wants to shoot or not.
     * @return target player
     */
    public Player startSniperTurn() {
        Sniper sniper = (Sniper) getPlayerByRole("Sniper");
        sendCustomGroupFilteredMessage("Sniper is awake.", "Sniper", false, false, true);
        if (sniper == null || !sniper.isAlive()) {
            // sleep for a random time to prevent other players from finding out dead roles
            sleepRandomTime(Setting.getNighActionTime().toSecondOfDay() * 1000L);
            sendCustomGroupFilteredMessage("Sniper is asleep.", "Sniper", false, false, true);
            return null;
        }
        wakeupGroup("Sniper", true, false, true);
        sendCustomMessageToPlayer("Choose one player to shoot, (enter 0 to cancel shooting):", sniper, false, false, true);
        LocalTime finishingTime = LocalTime.now().plusSeconds(Setting.getNighActionTime().toSecondOfDay());
        Player targetPlayer = getVote(sniper, finishingTime, "All", "None", false);
        sendCustomGroupFilteredMessage("Detector is asleep.", "Sniper", false, false, true);
        sleepGroup("Sniper", true, false, true);
        return targetPlayer;
    }

    /**
     * start psychologist's turn and let him make someone mute
     * @return muted player
     */
    public Player startPsychologistTurn() {
        Psychologist psychologist = (Psychologist) getPlayerByRole("Psychologist");
        sendCustomGroupFilteredMessage("Psychologist is awake.", "Psychologist", false, false, true);
        if (psychologist == null || !psychologist.isAlive()) {
            // sleep for a random time to prevent other players from finding out dead roles
            sleepRandomTime(Setting.getNighActionTime().toSecondOfDay() * 1000L);
            sendCustomGroupFilteredMessage("Psychologist is asleep.", "Psychologist", false, false, true);
            return null;
        }
        wakeupGroup("Psychologist", true, false, true);
        sendCustomMessageToPlayer("Choose one player to mute, (enter 0 to cancel shooting):", psychologist, false, false, true);
        LocalTime finishingTime = LocalTime.now().plusSeconds(Setting.getNighActionTime().toSecondOfDay());
        Player targetPlayer = getVote(psychologist, finishingTime, "All", "None", false);
        sendCustomGroupFilteredMessage("Psychologist is asleep.", "Psychologist", false, false, true);
        sleepGroup("Psychologist", true, false, true);
        return targetPlayer;
    }

    /**
     * start diehard's turn and ask him whether he wants to inquire status of the game or not
     * @return true, if diehard wants to inquire
     */
    public boolean startDieHardTurn() {
        DieHard dieHard = (DieHard) getPlayerByRole("DieHard");
        PlayerController playerController = playerControllers.get(dieHard);
        sendCustomGroupFilteredMessage("DieHard is awake.", "DieHard", false, false, true);
        if (dieHard == null || !dieHard.isAlive() || playerController == null || dieHard.getInquireCount() == Setting.getDieHardInquireCount()) {
            // sleep for a random time to prevent other players from finding out dead roles
            sleepRandomTime(Setting.getNighActionTime().toSecondOfDay() * 1000L);
            sendCustomGroupFilteredMessage("Diehard is asleep.", "Diehard", false, false, true);
            return false;
        }
        wakeupGroup("DieHard", true, false, true);
        LocalTime finishingTime = LocalTime.now().plusSeconds(Setting.getNighActionTime().toSecondOfDay());
        sendCustomMessageToPlayer("Do you want to inquire status of the game? (Enter yes or no)", dieHard, false, false, true);
        return playerController.ask(finishingTime);
    }

    /**
     * finish game and report winners
     */
    public void finishGame() {
        int numberOfMafias = 0, numberOfCitizens = 0;
        for (Player player : players)
            if (player.isAlive()) {
                if (player.hasRole("Mafia"))
                    numberOfMafias++;
                else
                    numberOfCitizens++;
            }
        if (numberOfCitizens == numberOfMafias)
            sendCustomMessageToAll("Congratulations to citizens. They have won the game.", true, false, true);
        else
            sendCustomMessageToAll("Congratulations to mafias. They have won the game.", true, false, true);
        for (Player player : players)
            sendCustomMessageToPlayer("EXIT", player, false, false, true);
        System.exit(0);
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
        for (Player player : players)
            if (player.hasRole(role)) {
                playerThreads.execute(() -> {
                    sendCustomMessageToPlayer(
                            "You have to sleep now, please wait until god inform you.",
                            player,
                            false, false, true
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
     * @param options call clearScreen, call getCh, wait for all message to be sent
     */
    public void wakeupGroup(String role, boolean... options) {
        boolean waitForResponse = false;
        if (options.length > 2)
            waitForResponse = options[2];
        AtomicInteger finishedThread = new AtomicInteger(0);
        int threadCount = 0;
        for (Player player : players)
            if (player.hasRole(role)) {
                PlayerController playerController = playerControllers.get(player);
                if (playerController == null)
                    continue;
                playerThreads.execute(() -> {
                    playerController.wakeup(options);
                    finishedThread.incrementAndGet();
                });
                threadCount++;
            }
        while (waitForResponse && finishedThread.get() < threadCount) // waits until all players wakeup
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
        return !username.equals("God");
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
     * check if game if finished
     * @return boolean result
     */
    public boolean isGameFinished() {
        int numberOfMafias = 0, numberOfCitizens = 0;
        for (Player player : players)
            if (player.isAlive()) {
                if (player.hasRole("Mafia"))
                    numberOfMafias++;
                else
                    numberOfCitizens++;
            }
        return (numberOfCitizens == numberOfMafias) || (numberOfMafias == 0);
    }

    /**
     * kick the given player out of the game.
     * a player will kicked out of the game, when either disconnected or left the game
     * @param player given player
     */
    public void kickPlayer(Player player) {
        if (player == null)
            return;
        // **do not change the order of the following 4 lines, unless program will raise runtime error.**
        sendCustomMessageToPlayer("EXIT", player, false, false, true);
        players.remove(player);
        playerControllers.remove(player);
        sendCustomMessageToAll("\n" + player + " has been kicked out/disconnected from the game.\n", true, true, true);
    }

    /**
     * show alive players to the corresponding player
     * @param targetPlayer target player
     * @param showRoles if true, will also show roles of players in same team
     * @param showHimself if true, will also show himself in the list of players
     * @param options call clearScreen, call getCh
     */
    public void showAlivePlayersToPlayer(Player targetPlayer, boolean showRoles, boolean showHimself, boolean... options) {
        int playerCount = 1;
        StringBuilder message = new StringBuilder();
        message.append("Alive players:\n");
        for (Player player : players) {
            if ((player.equals(targetPlayer) && !showHimself) || !player.isAlive()) // doesn't show himself or dead players
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
                    showAlivePlayersToPlayer(player, showRoles, false, finalCallClearScreen, finalCallGetCh, finalWaitForResponse);
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
        AtomicInteger finishedThread = new AtomicInteger(0);
        int loopCounter = 0;
        for (Player player : players) {
            if (player.hasRole(exceptionRole) || message.getSender().equals(player))
                continue;
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                continue;
            playerThreads.execute(() -> {
                playerController.sendCustomMessage(message, options[0], options[1]);
                finishedThread.incrementAndGet();
            });
            loopCounter++;
        }
        // to make sure that all players have received sent message
        while (options.length > 2 && options[2] && finishedThread.get() < loopCounter)
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
     * send the given message to those players with the given role and call remaining methods.
     * @param message given message
     * @param role players role
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToGroup(Message message, String role, boolean... options) {
        boolean callClearScreen = false, callGetCh = false;
        if (options.length > 0)
            callClearScreen = options[0];
        if (options.length > 1)
            callGetCh = options[1];
        AtomicInteger finishedThread = new AtomicInteger(0);
        int loopCounter = 0;
        for (Player player : players) {
            if (player.equals(message.getSender()) || !player.hasRole(role))
                continue;
            PlayerController playerController = playerControllers.get(player);
            if (playerController == null)
                continue;
            boolean finalCallClearScreen = callClearScreen;
            boolean finalCallGetCh = callGetCh;
            playerThreads.execute(() -> {
                playerController.sendCustomMessage(message, finalCallClearScreen, finalCallGetCh);
                finishedThread.incrementAndGet();
            });
            loopCounter++;
        }
        // to make sure that all players have received sent message
        while (options.length > 2 && options[2] && finishedThread.get() < loopCounter)
            sleep();
    }

    /**
     * send the given text to all players and call remaining methods
     * @param bodyMessage given text
     * @param options call clearScreen, call getCh, wait for client to receive message
     */
    public void sendCustomMessageToGroup(String bodyMessage, String role, boolean... options) {
        sendCustomMessageToGroup(new Message(bodyMessage, God.getInstance(), daytime), role, options);
    }

    /**
     * send the given text to all players
     * @param bodyMessage given text
     * @param option call clearScreen, call getCh, wait for players to receive message
     */
    public void sendCustomMessageToAll(String bodyMessage, boolean... option) {
        sendCustomMessageToGroup(bodyMessage, "All", option);
    }

    /**
     * send the given message to all players
     * @param message given message
     * @param option call clearScreen, call getCh, wait for players to receive message
     */
    public void sendCustomMessageToAll(Message message, boolean... option) {
        sendCustomMessageToGroup(message, "All", option);
    }

    /**
     * get a message from the given player
     * @param player given player
     * @return received message
     */
    public Message getMessageFromPlayer(Player player) {
        PlayerController playerController = playerControllers.get(player);
        if (playerController == null)
            return null;
        return playerController.getMessage();
    }

    /**
     * get player with the given role among those who are alive
     * @param role given role
     * @return corresponding player
     */
    public Player getPlayerByRole(String role) {
        Player resultPlayer = null;
        for (Player player : players)
            if (player.hasRole(role))
                resultPlayer = player;
        return resultPlayer;
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
     * sleep for a random time in milliseconds
     * @param delayTime generate a random time in range [0, delayTime]
     */
    public void sleepRandomTime(long delayTime) {
        Random random = new Random();
        long randomTime = random.nextInt((int) delayTime);
        try {
            Thread.sleep(randomTime);
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
