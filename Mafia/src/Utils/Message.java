package Utils;

import Controller.Person;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Message class, represents messages between persons
 */
public class Message implements Serializable {
    private String body;
    private Person sender;
    private String senderColor;
    private String bodyColor;
    final private LocalTime time;
    final private DAYTIME messageTime;

    /**
     * class constructor
     * @param body message body
     * @param sender message sender
     */
    public Message(String body, Person sender) {
        this.body = body;
        this.sender = sender;
        time = LocalTime.now();
        messageTime = DAYTIME.DAY;
    }

    /**
     * class constructor
     * @param body message body
     * @param sender message sender
     * @param daytime message time
     */
    public Message(String body, Person sender, DAYTIME daytime) {
        this.body = body;
        this.sender = sender;
        this.time = LocalTime.now();
        this.messageTime = daytime;
    }

    /**
     * class constructor
     * @param body message body
     * @param sender message sender
     * @param bodyColor message's body color
     */
    public Message(String body, Person sender, String bodyColor) {
        this.body = body;
        this.sender = sender;
        this.bodyColor = bodyColor;
        time = LocalTime.now();
        messageTime = DAYTIME.DAY;
    }

    /**
     * body getter
     * @return body
     */
    public String getBody() {
        return body;
    }

    /**
     * sender getter
     * @return sender
     */
    public Person getSender() {
        return sender;
    }

    /**
     * time getter
     * @return time
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * messageType getter
     * @return messageType
     */
    public DAYTIME getMessageType() {
        return messageTime;
    }

    /**
     * sender setter
     * @param sender sender new value
     */
    public void setSender(Person sender) {
        this.sender = sender;
    }

    /**
     * append the given text to the end of the body
     * @param text given text
     */
    public void appendBody(String text) {
        body += text;
    }

    /**
     * senderColor setter
     * @param senderColor senderColor's new value
     */
    public void setSenderColor(String senderColor) {
        this.senderColor = senderColor;
    }

    /**
     * textColor setter
     * @param bodyColor textColor's new value
     */
    public void setBodyColor(String bodyColor) {
        this.bodyColor = bodyColor;
    }

    /**
     * show message in the stdout
     */
    public void show() {
        String senderColor = ConsoleColor.YELLOW, bodyColor = ConsoleColor.BLUE_BOLD_BRIGHT;
        if (this.senderColor != null)
            senderColor = this.senderColor;
        if (this.bodyColor != null)
            bodyColor = this.bodyColor;
        if (body.equals("Press Enter key to continue..."))
            bodyColor = ConsoleColor.RED;

        if (sender != null && !sender.getUsername().equals("God"))
            System.out.println(senderColor + sender + ":\n" + bodyColor + body + ConsoleColor.RESET);
        else
            System.out.println(bodyColor + body + ConsoleColor.RESET);
    }
}
