import java.time.LocalDate;
import java.time.LocalTime;

enum MESSAGETYPE {
    ALL,
    MAFIA
}

/**
 * Message class, represents messages between persons
 */
public class Message {
    final private String body;
    final private Person sender, receiver;
    final private LocalTime time;
    final private MESSAGETYPE messageType;

    /**
     * class constructor
     * @param body message body
     * @param sender message sender
     * @param receiver message receiver
     */
    public Message(String body, Person sender, Person receiver) {
        this.body = body;
        this.sender = sender;
        this.receiver = receiver;
        time = LocalTime.now();
        messageType = MESSAGETYPE.ALL;
    }

    public Message(String body, Person sender, Person receiver, MESSAGETYPE messageType) {
        this.body = body;
        this.sender = sender;
        this.receiver = receiver;
        this.time = LocalTime.now();
        this.messageType = messageType;
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
     * receiver getter
     * @return receiver
     */
    public Person getReceiver() {
        return receiver;
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
    public MESSAGETYPE getMessageType() {
        return messageType;
    }

    /**
     * show message in the stdout
     */
    public void show() {
        System.out.println("A message from " + sender + ":\n" + body);
    }
}
