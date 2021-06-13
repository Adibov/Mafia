package Controller;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client class, handles player interactions between server
 * @author Adibov
 * @version 1.0
 */
public class Client extends Thread {
    private Player player;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private AtomicBoolean isServerUp; // defined atomic so threads don't conflict

    /**
     * class constructor
     * @param player client's player
     * @param port server port
     */
    public Client(Player player, int port) throws IOException {
        this.player = player;
        socket = new Socket("localhost", port);
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException exception) {
            exception.printStackTrace();
            socket = null;
        }
        isServerUp = new AtomicBoolean(true);
    }

    /**
     * thread run method
     */
    public void run() {
        runInputEngine();
    }

    /**
     * always get input from the server and print it to the output
     */
    private void runInputEngine() {
        Message newMessage;
        while (isServerUp.get()) {
            try {
                if (socket.getInputStream().available() > 0) {
                    newMessage = (Message) objectInputStream.readObject();
                    if (newMessage.getBody().equals("EXIT") && newMessage.getSender().getUsername().equals("God")) {
                        isServerUp.set(false);
                        System.out.println("Press Enter to continue...");
                        break;
                    }
                    player.showMessage(newMessage);
                }
                //noinspection BusyWait
                Thread.sleep(2L * Setting.getSleepTime());
            }
            catch (IOException | ClassNotFoundException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * close socket
     */
    public void closeSocket() {
        if (socket.isClosed())
            return;
        try {
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * isServerUp getter
     * @return boolean result
     */
    public boolean isServerUp() {
        return isServerUp.get();
    }

    /**
     * serverUp setter
     * @param serverUp serverUp new value
     */
    public void setServerUp(boolean serverUp) {
        isServerUp.set(serverUp);
    }

    /**
     * send the given message to the server
     * @param message given message
     */
    public void sendMessage(Message message) {
        if (!isServerUp.get() || socket.isClosed())
            return;
        try {
            objectOutputStream.writeObject(message);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * send the given text to the server
     * @param message given text
     */
    public void sendMessage(String message) {
        sendMessage(new Message(message, player));
    }
}
