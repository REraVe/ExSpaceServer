import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONObject;

/**
 * Created by REraVe on 23.06.2017.
 */
public class ExSpaceServer {

    private static final String TAG = "ExSpaceServer";

    private static final int PORT = 2018;
    private static final String EMPTY_MESSAGE = "-1";

    private static ArrayList<InetAddress> allClientsList = new ArrayList<>();

    public static void main(String args[]) {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);

            System.out.println(TAG + ". Server started.");

            while (true){
                System.out.println(TAG + ". Waiting for connection...");
                System.out.println(TAG + ". Connected now:");

                for (int i = 0; i < allClientsList.size(); i++) {
                    System.out.println(TAG + ". Client № " + (i + 1) + ":" + allClientsList.get(i));
                }

                Socket clientSocket = serverSocket.accept();
                InetAddress clientAddress = clientSocket.getInetAddress();

                System.out.println(TAG + ". Connection accepted from " + clientAddress);

                if (!allClientsList.contains(clientAddress)) {
                    createNewConnection(clientSocket);
                }
                else {
                    processConnection(clientSocket);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(TAG + ". Server stopped.");
    }

    private static void createNewConnection(Socket clientSocket) {
        new Thread(() -> {
            InetAddress clientAddress = clientSocket.getInetAddress();

            System.out.println(TAG + ". Create new connection for:" + clientAddress);

            try (InputStream in   = clientSocket.getInputStream();
                 OutputStream out = clientSocket.getOutputStream()) {

                byte[] bufferData = new byte[32 * 1024];
                int readBytes = in.read(bufferData);
                String receivedMessage = new String(bufferData, 0, readBytes);

                System.out.println(TAG + ". Server < " + clientAddress + " :: " + receivedMessage);

                String answerMessage = EMPTY_MESSAGE;

               if (receivedMessage.equals("createNewNetworkPlayer")) {
                   allClientsList.add(clientAddress);                                          // Добавляем клиента в масив всех клиентов

                   NetworkSession networkSession = NetworkSession.addNewPlayer(clientAddress); // Добавляем клиента в сессию

                   JSONObject jsonObject = new JSONObject();
                   jsonObject.put("networkSessionNumber", networkSession.getNumber());
                   jsonObject.put("networkPlayerNumber",  networkSession.getPlayerNumber(clientAddress));

                   answerMessage = jsonObject.toString();
               }

                out.write(answerMessage.getBytes());
                out.flush();

                System.out.println(TAG + ". Server > " + clientAddress + " :: " + answerMessage);
                System.out.println(TAG + ". Connection close from " + clientAddress);
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println(TAG + ". Receive message error from " + clientAddress);
            }
        }).start();
    }

    private static void processConnection(Socket clientSocket) {
        new Thread(() -> {
            InetAddress clientAddress = clientSocket.getInetAddress();

            try (InputStream in   = clientSocket.getInputStream();
                 OutputStream out = clientSocket.getOutputStream()) {

                byte[] bufferData = new byte[32 * 1024];
                int readBytes = in.read(bufferData);
                String receivedMessage = new String(bufferData, 0, readBytes);

                System.out.println(TAG + ". Server < " + clientAddress + " :: " + receivedMessage);

                String answerMessage = processMessage(receivedMessage);

                if (answerMessage.equals(""))
                    answerMessage = EMPTY_MESSAGE;

                out.write(answerMessage.getBytes());
                out.flush();

                System.out.println(TAG + ". Server > " + clientAddress + " :: " + answerMessage);
                System.out.println(TAG + ". Connection close from " + clientAddress);
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println(TAG + ". Receive message error from " + clientAddress);
            }
        }).start();
    }

    private static String processMessage(String receivedMessage) {
        JSONObject jsonObject = new JSONObject(receivedMessage);
        int networkSessionNumber = jsonObject.getInt("networkSessionNumber");
        int networkPlayerNumber  = jsonObject.getInt("networkPlayerNumber");

        NetworkSession networkSession = NetworkSession.getNetworkSession(networkSessionNumber);
        networkSession.addMessageToOtherPlayers(networkPlayerNumber, receivedMessage);

        return networkSession.getMessageFromOtherPlayers(networkPlayerNumber);
    }

}
