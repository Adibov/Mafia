import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    final private ArrayList<Message> untrackedMessages = new ArrayList<Message>();

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
            String username = Server.getUsernameFromClient(newPlayerClientHandler);
            Player newPlayer = new Player(username);
            playerHandlers.put(newPlayer, newPlayerClientHandler);
            return newPlayer;
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * retrieve username from client
     * @param clientHandler clientHandler of the client
     * @return result username
     */
    private static String getUsernameFromClient(ClientHandler clientHandler) {
        clientHandler.sendMessage(new Message("Please enter an username: ", new God(), new Player("Unknown")));
        String username = null;
        while (username == null) {
            username = clientHandler.getMessageFromClient().toString();
            if (playerHandlers.containsKey(new Player(username))) {
                clientHandler.sendMessage(new Message("This username has been taken, please enter new one:", new God(), new Player("Unknown")));
                username = null;
            }
        }
        return username;
    }

    public void sendMessage(Message message) {
        untrackedMessages.add(message);
    }
}
