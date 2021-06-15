package Roles;

import Controller.Player;

/**
 * Mafia class, handles mafia players in the game
 */
public class Mafia extends Player {
    /**
     * class constructor
     * @param player player
     */
    public Mafia(Player player) {
        super(player.getUsername());
    }
}
