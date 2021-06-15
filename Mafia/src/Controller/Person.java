package Controller;

import java.io.Serializable;

/**
 * Person class, implements person behaviours
 * @author Adibov
 * @version 1.0
 */
abstract public class Person implements Serializable {
    final protected String username;
    private String preferredColor;

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

    /**
     * preferredColor getter
     * @return preferredColor
     */
    public String getPreferredColor() {
        return preferredColor;
    }

    /**
     * preferredColor setter
     * @param preferredColor preferredColor's new value
     */
    public void setPreferredColor(String preferredColor) {
        this.preferredColor = preferredColor;
    }
}
