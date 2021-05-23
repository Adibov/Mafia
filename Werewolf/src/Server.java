import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server class, handles interactions with clients, like send message, receive message, etc
 */
public class Server {
    final static private ExecutorService playerThreads = Executors.newCachedThreadPool();
    static private ServerSocket serverSocket;
    final static HashMap<Player, ClientHandler> playerHandlers = new HashMap<>();
    final static private ConcurrentLinkedQueue<Message> untrackedMessages = new ConcurrentLinkedQueue<>();

    /**
     * run server
     */
    public static void runServer() {
        try {
            serverSocket = new ServerSocket(2021);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * accept a new client and choose username for him
     * @param god god of the game
     * @return new player
     */
    public static Player acceptNewPlayer(God god) {
        Player newPlayer = null;
        try {
            ClientHandler newClientHandler = new ClientHandler(serverSocket.accept());
            String username = "";
            while (true) {
                newClientHandler.sendMessage(new Message("Enter an username:", God.getGodObject()));
                username = newClientHandler.getMessage().getBody();
                if (god.isAvailableUsername(username))
                    break;
                newClientHandler.sendMessage(new Message("This username has been taken.", God.getGodObject()));
            }
            newPlayer = new Player(username);
            playerHandlers.put(newPlayer, newClientHandler);
            playerThreads.execute(newClientHandler);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        return newPlayer;
    }

    /**
     * send the given message to the corresponding player
     * @param message given message
     * @param player receiver player
     */
    public static void sendMessage(Message message, Player player) {
        ClientHandler clientHandler = playerHandlers.get(player);
        clientHandler.sendMessage(message);
    }

    /**
     * add the given message to the untracked messages
     * @param message given message
     */
    public static void addMessage(Message message) {
        untrackedMessages.add(message);
    }
}
