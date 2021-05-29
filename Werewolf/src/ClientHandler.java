import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * ClientHandler class, handles server interactions with client
 */
public class ClientHandler {
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    /**
     * class constructor
     */
    public ClientHandler() {
        try {
            socket = new Socket("127.0.0.1", 2021);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * send the given message via the socket
     * @param message given message
     */
    public void sendMessage(Message message) {
        try {
            objectOutputStream.writeObject(message);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * wait until get a message from the client
     * @return received message
     */
    public Message getMessage() {
        Message receivedMessage = null;
        while (true) {
            try {
                receivedMessage = (Message) objectInputStream.readObject();
                break;
            }
            catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
            }
        }
        return receivedMessage;
    }

    /**
     * override equals method
     * @param o given object
     * @return boolean result
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientHandler that)) return false;
        return Objects.equals(socket, that.socket);
    }

    /**
     * override hashCode method
     * @return hash result
     */
    @Override
    public int hashCode() {
        return Objects.hash(socket);
    }
}
