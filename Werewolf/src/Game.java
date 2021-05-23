/**
 * Game class, manages games. like starting a new game, save a game, load a game, etc ...
 * @author Adibov
 * @version 1.0
 */
public class Game {
    private God god;
    private DAYTIME daytime = DAYTIME.DAY;

    public void startNewGame() {
        god = new God();
        god.startNewGame();
    }

    /**
     * dayTime setter
     * @param daytime new dayTime value
     */
    public void setDaytime(DAYTIME daytime) {
        this.daytime = daytime;
    }
}
