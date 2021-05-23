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
            System.out.println("Server started successfully.");
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
                clearScreen(newClientHandler);
                newClientHandler.sendMessage(new Message("Enter an username:", God.getGodObject()));
                username = newClientHandler.getMessage().getBody();
                if (god.isAvailableUsername(username))
                    break;
                clearScreen(newClientHandler);
                newClientHandler.sendMessage(new Message("This username has been taken.", God.getGodObject()));
                getCh(newClientHandler);
            }
            newPlayer = new Player(username);
            playerHandlers.put(newPlayer, newClientHandler);
            playerThreads.execute(newClientHandler);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        clearScreen(newPlayer);
        sendMessage(new Message("You have joined to the game.", God.getGodObject()), newPlayer);
        getCh(newPlayer);
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
     * clear terminal screen for the given player
     * @param player given player
     */
    public static void clearScreen(Player player) {
        sendMessage(new Message("\n".repeat(50), God.getGodObject()), player);
    }

    /**
     * clear terminal screen for the given clientHandler
     * @param clientHandler given clientHandler
     */
    private static void clearScreen(ClientHandler clientHandler) {
        clientHandler.sendMessage(new Message("\n".repeat(50), God.getGodObject()));
    }

    /**
     * wait for the client to press enter
     * @param player client player
     */
    public static void getCh(Player player) {
        ClientHandler clientHandler = playerHandlers.get(player);
        getCh(clientHandler);
    }

    /**
     * wait for the client to press enter
     * @param clientHandler given clientHandler
     */
    private static void getCh(ClientHandler clientHandler) {
        clientHandler.setPaused(false);
        try {
            Thread.sleep(Setting.getServerRefreshTime());
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        clientHandler.sendMessage(new Message("Press enter to continue...", God.getGodObject()));
        clientHandler.getMessage(); // drop
        clientHandler.setPaused(true);
    }

    /**
     * add the given message to the untracked messages
     * @param message given message
     */
    public static void addMessage(Message message) {
        untrackedMessages.add(message);
    }
}
