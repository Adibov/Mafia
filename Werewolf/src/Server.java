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
    final static HashMap<Player, ClientHandler> playerHandlers = new HashMap<Player, ClientHandler>();
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
            ClientHandler newPlayerClientHandler = new ClientHandler(socket);
            playerThreads.execute(newPlayerClientHandler);
            String username = Server.getUsernameFromClient();

        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * retrieve username from client
     * @param clientHandler clientHandler of the client
     * @return result username
     */
    private static String getUsernameFromClient(ClientHandler clientHandler) {
        clientHandler.sendMessage(new Message("Please enter a username: ", new God(), new Citizen("Unknown")));
        
    }
}
