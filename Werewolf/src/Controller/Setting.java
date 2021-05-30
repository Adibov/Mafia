package Controller;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Setting class, saves game settings
 */
public class Setting {
    final static private int numberOfPlayers = 3;
    final static private int numberOfMafias = numberOfPlayers / 3;
    final static private LocalTime dayLength = LocalTime.of(0, 5, 0);
    final static private LocalTime votingTime = LocalTime.of(0, 0, 30);
    final static private int serverRefreshTime = 700; // in milliseconds
    final static private int socketTimeOut = 60 * 30; // in seconds
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
     * dayLength getter
     * @return dayLength
     */
    public static LocalTime getDayLength() {
        return dayLength;
    }

    /**
     * votingTime getter
     * @return votingTime
     */
    public static LocalTime getVotingTime() {
        return votingTime;
    }

    /**
     * serverRefreshTime getter
     * @return serverRefreshTime
     */
    public static int getServerRefreshTime() {
        return serverRefreshTime;
    }

    /**
     * socketTimeOut getter
     * @return socketTimeOut
     */
    public static int getSocketTimeOut() {
        return socketTimeOut;
    }

    /**
     * clearScreenBlankLines getter
     * @return clearScreenBlankLines
     */
    public static int getClearScreenBlankLines() {
        return clearScreenBlankLines;
    }
}
