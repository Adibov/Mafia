package Roles;

import Controller.Player;

/**
 * Mayor class, represent mayor role in game
 * @author Adibov
 * @version 1.0
 */
public class Mayor extends Citizen {
    private boolean hasCanceledVoting;

    /**
     * class constructor
     * @param player player of the role
     */
    public Mayor(Player player) {
        super(player);
        hasCanceledVoting = false;
    }

    /**
     * hasCanceledVoting getter
     * @return hasCanceledVoting
     */
    public boolean hasCanceledVoting() {
        return hasCanceledVoting;
    }

    /**
     * hasCanceledVoting setter
     * @param hasCanceledVoting new value
     */
    public void setHasCanceledVoting(boolean hasCanceledVoting) {
        this.hasCanceledVoting = hasCanceledVoting;
    }
}
