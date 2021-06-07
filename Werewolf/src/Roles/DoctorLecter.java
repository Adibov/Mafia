package Roles;

import Controller.Player;

/**
 * DoctorLecter class, represents doctor lecter role in game
 * @author Adibov
 * @version 1.0
 */
public class DoctorLecter extends Mafia {
    private boolean hasHealedHimself;

    /**
     * class constructor
     * @param player player of the role
     */
    public DoctorLecter(Player player) {
        super(player);
        hasHealedHimself = false;
    }

    /**
     * hasHealedHimSelf getter
     * @return hasHealedHimSelf
     */
    public boolean hasHealedHimself() {
        return hasHealedHimself;
    }
}
