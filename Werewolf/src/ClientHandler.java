import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * ClientHandler class, handles server interactions with client
 */
public class ClientHandler implements Runnable {
    final private Socket socket;
    final private ConcurrentLinkedDeque<Message> messages = new ConcurrentLinkedDeque<>();

    /**
     * class constructor
     * @param socket server socket
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
            socket.setSoTimeout(Setting.getSocketTimeOut());
            while (true) {
                Message newMessage = (Message) objectInputStream.readObject();
                messages.add(newMessage);
                Thread.sleep(Setting.getServerRefreshTime());
            }
        }
        catch (IOException | ClassNotFoundException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * get last unseen sent message
     * @return result message
     */
    public Message getLastUnseenMessage() {
        if (messages.size() == 0)
            return null;
        Message result = messages.getFirst();
        messages.removeFirst();
        return result;
    }

    /**
     * return getLastUnseenMessage if not null, waits for an input otherwise
     * @return result message
     */
    public Message getMessageFromClient() {
        Message result = null;
        while (result == null)
            result = getLastUnseenMessage();
        return result;
    }

    /**
     * send the given message via the socket
     * @param message given message
     */
    public void sendMessage(Message message) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            objectOutputStream.writeObject(message);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
