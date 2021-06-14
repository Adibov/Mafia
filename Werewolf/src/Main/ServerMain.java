package Main;

import Controller.Game;
import Utils.ConsoleColor;
import Utils.Setting;

import java.util.Scanner;

/**
 * Main class to start the project and testing
 * @author Adibov
 * @version 1.0
 */
public class ServerMain {
    private Scanner inputScanner = new Scanner(System.in);
    /**
     * main method to make class runnable
     * @param args program args
     */
    public static void main(String[] args) {
        ServerMain server = new ServerMain();
        server.showMainMenu();
    }

    /**
     * show main menu to god and let him choose an option
     */
    @SuppressWarnings("InfiniteRecursion")
    public void showMainMenu() {
        clearScreen();
        System.out.println(ConsoleColor.CYAN_BOLD_BRIGHT + """
                Hello dear God!
                Welcome to server-side :D
                """);
        System.out.println(ConsoleColor.YELLOW + "1) " + ConsoleColor.BLUE_BOLD_BRIGHT + "Start a new game");
        System.out.println(ConsoleColor.YELLOW + "2) " + ConsoleColor.BLUE_BOLD_BRIGHT + "Load last game");
        System.out.println(ConsoleColor.YELLOW + "3) " + ConsoleColor.BLUE_BOLD_BRIGHT + "Change settings\n" + ConsoleColor.RESET);

        int option = getOption(3);
        if (option == 1) {
            clearScreen();
            Game game = new Game();
            game.startNewGame();
        }
        else if (option == 2) {
            System.out.println("Under construction...");
        }
        else {
            changeSettings();
        }
        showMainMenu();
    }

    /**
     * show list of settings and
     */
    public void changeSettings() {
        clearScreen();
        System.out.println(ConsoleColor.YELLOW + "1) " + ConsoleColor.BLUE_BOLD_BRIGHT + "Change number of players");
        System.out.println(ConsoleColor.YELLOW + "2) " + ConsoleColor.BLUE_BOLD_BRIGHT + "Change night action time");
        System.out.println(ConsoleColor.YELLOW + "3) " + ConsoleColor.BLUE_BOLD_BRIGHT + "Change discussion time" + ConsoleColor.RESET);

        int option = getOption(3);
        if (option == 1)
            Setting.setNumberOfPlayers(getIntFromRange(3, 10));
        else if (option == 2) {
            System.out.println(ConsoleColor.CYAN_BOLD_BRIGHT + "Night action time is in seconds." + ConsoleColor.RESET);
            Setting.setNightActionTime(getIntFromRange(15, 45));
        }
        else {
            System.out.println(ConsoleColor.CYAN_BOLD_BRIGHT + "Discussion time is in minutes." + ConsoleColor.RESET);
            Setting.setDiscussionPhaseTime(getIntFromRange(1, 5));
        }
    }

    /**
     * get an option from the available options
     * @param numberOfOptions number of available options
     * @return chosen option
     */
    public int getOption(int numberOfOptions) {
        int option;
        while (true) {
            System.out.println(ConsoleColor.CYAN_BOLD_BRIGHT + "Please choose and option:" + ConsoleColor.RESET);
            option = inputScanner.nextInt(); inputScanner.nextLine(); // drop
            if (option < 1 || option > numberOfOptions) {
                System.out.println(ConsoleColor.RED + "Invalid input" + ConsoleColor.RESET);
                continue;
            }
            break;
        }
        return option;
    }

    /**
     * get an int from the given range
     * @param l start of the range
     * @param r end of the range
     * @return chosen int
     */
    public int getIntFromRange(int l, int r) {
        int option;
        while (true) {
            System.out.println(
                    ConsoleColor.CYAN_BOLD_BRIGHT +
                    "Please enter a number between " + l + " and " + r + "(inclusive):" +
                    ConsoleColor.RESET
            );
            option = inputScanner.nextInt(); inputScanner.nextLine(); // drop
            if (option < l || option > r) {
                System.out.println(ConsoleColor.RED + "Invalid input" + ConsoleColor.RESET);
                continue;
            }
            break;
        }
        return option;
    }

    /**
     * clear terminal screen
     */
    public void clearScreen() {
        System.out.println("\n".repeat(Setting.getClearScreenBlankLines()));
    }
}
