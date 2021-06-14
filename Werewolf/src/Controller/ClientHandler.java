package Controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
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
     * @param socket client socket
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
     * send the given message via the socket
     * @param message given message
     */
    public synchronized void sendMessage (Message message) {
        if (isSocketClosed())
            return;
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * wait until get a message from the client
     * @return received message
     */
    public synchronized Message getMessage() {
        Message receivedMessage;
        while (true) {
            if (isSocketClosed())
                return null;
            try {
                receivedMessage = (Message) objectInputStream.readObject();
                break;
            }
            catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
            }
        }
        if (receivedMessage != null && receivedMessage.getBody().equals("exit")) {
            try {
                socket.close();
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return receivedMessage;
    }

    /**
     * check if the client is alive
     * @return boolean result
     */
    public boolean isSocketClosed() {
        return socket.isClosed();
    }

    /**
     * check if socket stream is empty
     * @return boolean result
     */
    public boolean isStreamEmpty() {
        try {
            InputStream inputStream = socket.getInputStream();
            return (inputStream.available() == 0);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        return true;
    }

    public void clearStream() {
        while (!isStreamEmpty())
            getMessage(); // drop
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
