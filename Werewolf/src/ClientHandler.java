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
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    /**
     * class constructor
     * @param socket server socket
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
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
        try {
            socket.setSoTimeout(Setting.getSocketTimeOut());
            while (true) {
                Message newMessage = (Message) objectInputStream.readObject();
                Server.addMessage(newMessage);
                Thread.sleep(Setting.getServerRefreshTime());
            }
        }
        catch (IOException | ClassNotFoundException | InterruptedException exception) {
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
     * get message from the client, MAKE SURE TO RUN IT BEFORE THE run METHOD SO THEY DON'T INTERRUPT EACH OTHER
     * @return received message
     */
    public Message getMessage() {
        Message receivedMessage = null;
        try {
            receivedMessage = (Message) objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return receivedMessage;
    }
}
