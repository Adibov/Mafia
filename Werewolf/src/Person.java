/**
 * Person class, implements person behaviours
 * @author Adibov
 * @version 1.0
 */
abstract public class Person {
    final private String username;

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
}
