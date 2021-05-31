package Controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
        while (true) {
            try {
                newMessage = (Message) objectInputStream.readObject();
                player.showMessage(newMessage);
                Thread.sleep(Setting.getSleepTime());
            }
            catch (IOException | ClassNotFoundException | InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * send the given message to the server
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
}
