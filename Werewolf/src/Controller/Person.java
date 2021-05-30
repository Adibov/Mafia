package Controller;

import java.io.Serializable;

/**
 * Person class, implements person behaviours
 * @author Adibov
 * @version 1.0
 */
abstract public class Person implements Serializable {
    final protected String username;

    /**
     * class constructor
     * @param username person username
     */
    public Person(String username) {
        this.username = username;
    }

    /**
     * username getter
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * override toString method
     * @return String result
     */
    @Override
    public String toString() {
        return username;
    }
}
