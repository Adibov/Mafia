package Utils;

import java.time.LocalTime;

/**
 * Setting class, saves game settings
 * @author Adibov
 * @version 1.0
 */
public class Setting {
    //////////// gameplay setting \\\\\\\\\\\\\\\\\
    // total number of players
    static private int numberOfPlayers = 10;
    // total number of mafias
    static private int numberOfMafias = (numberOfPlayers + 1) / 3;
    // how much time does a player can talk in an introduction phase
    final static private LocalTime introductionTurnTime = LocalTime.of(0, 0, 30);
    // how much time does a player can make his turn in night
    static private LocalTime nightActionTime = LocalTime.of(0, 0, 30);
    // how much time does the server freeze game when someone is dead
    final static private LocalTime randomNightActionTime = LocalTime.of(0, 0, 5);
    // how much time do players can talk in a regular discussion phase
    static private LocalTime discussionPhaseTime = LocalTime.of(0, 5, 0);
    // how much time does a player have to vote in voting phase
    final static private LocalTime votingTime = LocalTime.of(0, 2, 0);
    // how many times does diehard can inquire game status
    final static private int dieHardInquireCount = 2;
    // how much time does a player can watch history for each turn
    final static private LocalTime historyReviewTime = LocalTime.of(0, 0, 30);

    // TODO set settings to default

    ///////////////// server setting \\\\\\\\\\\\\\\\\\\\
    // sleep time in milliseconds
    final static private int sleepTime = 50;
    // how many blank lines to printed in clearScreen method
    final static private int clearScreenBlankLines = 50;

    /**
     * numberOfPlayers getter
     * @return numberOfPlayers
     */
    public static int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * numberOfMafias getter
     * @return numberOfMafias
     */
    public static int getNumberOfMafias() {
        return numberOfMafias;
    }

    /**
     * return number of citizens
     * @return citizens' count
     */
    public static int getNumberOfCitizens() {
        return Setting.getNumberOfPlayers() - Setting.getNumberOfMafias();
    }

    /**
     * serverRefreshTime getter
     * @return serverRefreshTime
     */
    public static int getSleepTime() {
        return sleepTime;
    }

    /**
     * clearScreenBlankLines getter
     * @return clearScreenBlankLines
     */
    public static int getClearScreenBlankLines() {
        return clearScreenBlankLines;
    }

    /**
     * introductionDayTurnTime getter
     * @return introductionDayTurnTime
     */
    public static LocalTime getIntroductionTurnTime() {
        return introductionTurnTime;
    }

    /**
     * nightActionTime getter
     * @return nightActionTime
     */
    public static LocalTime getNightActionTime() {
        return nightActionTime;
    }

    /**
     * randomNightActionTime getter
     * @return randomNightActionTime
     */
    public static LocalTime getRandomNightActionTime() {
        return randomNightActionTime;
    }

    /**
     * regularDayTurnTime getter
     * @return regularDayTurnTime
     */
    public static LocalTime getDiscussionPhaseTime() {
        return discussionPhaseTime;
    }

    /**
     * votingTime getter
     * @return votingTime
     */
    public static LocalTime getVotingTime() {
        return votingTime;
    }

    /**
     * get dieHardInquireCount
     * @return dieHardInquireCount
     */
    public static int getDieHardInquireCount() {
        return dieHardInquireCount;
    }

    /**
     * historyReviewTime getter
     * @return historyReviewTime
     */
    public static LocalTime getHistoryReviewTime() {
        return historyReviewTime;
    }

    /**
     * numberOfPlayers setter
     * @param numberOfPlayers numberOfPlayers
     */
    public static void setNumberOfPlayers(int numberOfPlayers) {
        Setting.numberOfPlayers = numberOfPlayers;
        Setting.numberOfMafias = (numberOfPlayers + 1) / 3;
    }

    /**
     * numberOfPlayers setter
     * @param nightActionTime numberOfPlayers in seconds
     */
    public static void setNightActionTime(int nightActionTime) {
        Setting.nightActionTime = LocalTime.of(0, 0, nightActionTime);
    }

    /**
     * discussionPhaseTime setter
     * @param discussionPhaseTime discussionPhaseTime in minutes
     */
    public static void setDiscussionPhaseTime(int discussionPhaseTime) {
        Setting.discussionPhaseTime = LocalTime.of(0, discussionPhaseTime, 0);
    }
}
