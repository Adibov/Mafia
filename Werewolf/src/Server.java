import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server class, handles interactions with clients, like send message, receive message, etc
 */
public class Server {
    final static private ExecutorService playerThreads = Executors.newCachedThreadPool();
    static private ServerSocket serverSocket;
    final static HashMap<Player, Socket> playerSocketMap = new HashMap<Player, Socket>();
    static {
        try {
            Server.serverSocket = new ServerSocket(2021);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * connect a new player to server and return it
     * @return new player
     */
    public static Player newPlayer() {
        try(Socket socket = serverSocket.accept()) {
            Server
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void sendMessage(Socket socket) {

    }


}
