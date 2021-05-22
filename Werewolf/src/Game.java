/**
 * Game class, manages games. like starting a new game, save a game, load a game, etc ...
 * @author Adibov
 * @version 1.0
 */
public class Game {
    private God god;

    public void startNewGame() {
        god = new God();
        god.startNewGame();
    }
}
