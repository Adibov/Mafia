import java.util.ArrayList;

/**
 * God class, represent god of the game. By using singleton design patter, returns a single God object
 * @author Adibov
 * @version 1.0
 */
public class God extends Person {
    private static God instance = null;

    /**
     * private class constructor, so no one can make more than one instance
     */
    private God() {
        super("God");
    }

    /**
     * return god object
     * @return god object
     */
    public static God getInstance() {
        if (instance == null)
            instance = new God();
        return instance;
    }
}
