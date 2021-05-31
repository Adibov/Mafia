package Controller;

import java.time.LocalTime;

/**
 * Setting class, saves game settings
 * @author Adibov
 * @version 1.0
 */
public class Setting {
    // gameplay setting
    final static private int numberOfPlayers = 3; // total number of players
    final static private int numberOfMafias = numberOfPlayers / 3; // total number of mafias


    // server setting
    final static private int sleepTime = 700; // in milliseconds
    final static private int clearScreenBlankLines = 50;
    // how much time does a player can talk in introduction day
    final static private LocalTime introductionDayTurnTime = LocalTime.of(0, 0, 30);
    // server alert cycle time
    final static private LocalTime chatroomRemainingTimeAlertCycle = LocalTime.of(0, 0, 15);

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
    public static LocalTime getIntroductionDayTurnTime() {
        return introductionDayTurnTime;
    }

    /**
     * chatroomRemainingTimeAlertCycle getter
     * @return chatroomRemainingTimeAlertCycle
     */
    public static LocalTime getChatroomRemainingTimeAlertCycle() {
        return chatroomRemainingTimeAlertCycle;
    }
}
