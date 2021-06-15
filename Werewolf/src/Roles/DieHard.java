package Roles;

import Controller.Player;

import java.time.LocalTime;

/**
 * DieHard class, represent die hard role in game
 * @author Adibov
 * @version 1.0
 */
public class DieHard extends Citizen {
    private boolean isInvulnerable;
    private int inquireCount;

    /**
     * class constructor
     * @param player player of the role
     */
    public DieHard(Player player) {
        super(player);
        isInvulnerable = true;
        inquireCount = 0;
    }

    /**
     * isInvulnerable getter
     * @return isInvulnerable
     */
    public boolean isInvulnerable() {
        return isInvulnerable;
    }

    /**
     * inquireCount getter
     * @return inquireCount
     */
    public int getInquireCount() {
        return inquireCount;
    }

    /**
     * isInvulnerable setter
     * @param invulnerable invulnerable new value
     */
    public void setInvulnerable(boolean invulnerable) {
        isInvulnerable = invulnerable;
    }

    /**
     * inquireCount setter
     */
    public void incrementInquireCount() {
        inquireCount++;
    }
}
