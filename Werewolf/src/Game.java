/**
 * Game class saves game state and handle saving/loading the game.
 * @author Adibov
 * @version 1.0
 */
public class Game {
    private GameController gameController;

    /**
     * class constructor
     */
    public Game() {

    }

    /**
     * start a new game
     */
    public void startNewGame() {
        gameController = new GameController();
        gameController.startGame();
    }
}
