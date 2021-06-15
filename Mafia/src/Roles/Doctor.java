package Roles;

import Controller.Player;

/**
 * Doctor class, handles doctor role in game
 */
public class Doctor extends Citizen {
    private boolean hasHealedHimself;

    /**
     * class constructor
     * @param player player of the role
     */
    public Doctor(Player player) {
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

    /**
     * hasHealedHimself setter
     * @param hasHealedHimself hasHealedHimself new value
     */
    public void setHasHealedHimself(boolean hasHealedHimself) {
        this.hasHealedHimself = hasHealedHimself;
    }
}
