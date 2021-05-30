package Roles;

import Controller.Player;

/**
 * Citizen class, handles citizen player in the game
 * @author Adibov
 * @version 1.0
 */
public class Citizen extends Player {
    /**
     * class constructor
     * @param player player of the role
     */
    public Citizen(Player player) {
        super(player.getUsername());
    }
}
