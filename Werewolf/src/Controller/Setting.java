package Controller;

import java.time.LocalTime;

/**
 * Setting class, saves game settings
 * @author Adibov
 * @version 1.0
 */
public class Setting {
    //////////// gameplay setting \\\\\\\\\\\\\\\\\
    // total number of players
    final static private int numberOfPlayers = 3;
    // total number of mafias
    final static private int numberOfMafias = numberOfPlayers / 3;
    // how much time does a player can talk in an introduction phase
    final static private LocalTime introductionTurnTime = LocalTime.of(0, 0, 2);
    // how much time does a player can make his turn in night
    final static private LocalTime nighActionTime = LocalTime.of(0, 0, 30);

    ///////////////// server setting \\\\\\\\\\\\\\\\\\\\
    // sleep time in milliseconds
    final static private int sleepTime = 200;
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
    public static LocalTime getNighActionTime() {
        return nighActionTime;
    }
}
