import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by REraVe on 25.06.2017.
 */
public class NetworkSession {

    private static final String TAG = "NetworkSession";

    private static final int EMPTY_PLAYER_NUMBER    = -1;
    private static final int MAX_PLAYERS_IN_SESSION = 2;

    private static HashMap<Integer, NetworkSession> allSessionsMap = new HashMap<>();

    private int number;
    private HashMap<Integer, InetAddress> playersMap = new HashMap<>();
    private HashMap<Integer, String> messagesMap = new HashMap<>();

    private NetworkSession() {
        this.number = allSessionsMap.size() + 1;

        allSessionsMap.put(this.number, this);

        System.out.println(TAG + ". Created session № " + this.number);
    }

    public static NetworkSession addNewPlayer(InetAddress inetAddress) {
        NetworkSession lastCreatedSession = allSessionsMap.get(allSessionsMap.size());

        if (lastCreatedSession == null
                || lastCreatedSession.playersMap.size() == MAX_PLAYERS_IN_SESSION) {
            NetworkSession newSession = new NetworkSession();
            newSession.addPlayer(inetAddress);

            return newSession;
        }
        else {
            lastCreatedSession.addPlayer(inetAddress);

            return lastCreatedSession;
        }
    }

    public static NetworkSession getNetworkSession(int sessionNumber) {
        return allSessionsMap.get(sessionNumber);
    }

    private void addPlayer(InetAddress inetAddress) {
        int newPlayerNumber = this.playersMap.size() + 1;

        this.playersMap.put(newPlayerNumber, inetAddress);
        this.messagesMap.put(newPlayerNumber, "");

        System.out.println(TAG + ". Created player № " + newPlayerNumber + " for session № " + this.number);
    }

    public void addMessageToOtherPlayers(int playerNumber, String message) {
        for (HashMap.Entry<Integer, String> pair : messagesMap.entrySet()) {
            if (pair.getKey() != playerNumber)
                messagesMap.put(pair.getKey(), message);
        }
    }

    public String getMessageFromOtherPlayers(int playerNumber) {
        String message = messagesMap.get(playerNumber);
        messagesMap.put(playerNumber, "");

        return message;
    }

    public int getNumber() {
        return this.number;
    }

    public int getPlayerNumber(InetAddress inetAddress) {
        int playerNumber = EMPTY_PLAYER_NUMBER;

        for (HashMap.Entry<Integer, InetAddress> pair : playersMap.entrySet()) {
            if (pair.getValue().equals(inetAddress))
                playerNumber = pair.getKey();
        }

        return playerNumber;
    }

    public InetAddress getPlayerInetAddress(int playerNumber) {
        return playersMap.get(playerNumber);
    }
}
