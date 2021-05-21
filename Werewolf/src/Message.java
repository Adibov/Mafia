import java.time.LocalDate;

/**
 * Message class, represents messages between persons
 */
public class Message {
    final private String body;
    final private Person sender, receiver;
    final private LocalDate time;

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
        time = LocalDate.now();
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
    public LocalDate getTime() {
        return time;
    }
}
