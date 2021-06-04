package Controller;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

enum DAYTIME implements Serializable {
    DAY,
    NIGHT
}

/**
 * Message class, represents messages between persons
 */
public class Message implements Serializable {
    final private String body;
    private Person sender;
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

    public Message(String body, Person sender, DAYTIME daytime) {
        this.body = body;
        this.sender = sender;
        this.time = LocalTime.now();
        this.messageTime = daytime;
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
     * show message in the stdout
     */
    public void show() {
        if (!sender.getUsername().equals("God"))
            System.out.println(sender + ":\n" + body);
        else
            System.out.println(body);
    }
}
