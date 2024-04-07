package labs.lab2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class HeartbeatServer {

    static final int SERVER_PORT = 10000;
    static final int BUFFER_SIZE = 1024;
    private static final long INACTIVE_TIMEOUT = 5000;

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(SERVER_PORT)) {
            byte[] receiveData = new byte[BUFFER_SIZE];
            Map<String, Long> clients = new HashMap<>();

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                processHeartbeat(message, clients);
                sendResponse(serverSocket, receivePacket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processHeartbeat(String message, Map<String, Long> clients) {
        String[] messageData = message.split(" ");
        if (messageData.length == 3 && messageData[0].equals("Heartbeat")) {
            String clientId = messageData[1];
            clients.put(clientId, System.currentTimeMillis());
            System.out.println("Heartbeat received from client " + clientId);

            clients.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > INACTIVE_TIMEOUT);
            clients.forEach((key, value) -> {
                if (System.currentTimeMillis() - value > INACTIVE_TIMEOUT) {
                    System.out.println("Client " + key + " is inactive. Application may have stopped.");
                }
            });
        }
    }

    private static void sendResponse(DatagramSocket serverSocket, DatagramPacket receivePacket) throws IOException {
        InetAddress clientAddress = receivePacket.getAddress();
        int clientPort = receivePacket.getPort();
        serverSocket.send(new DatagramPacket(receivePacket.getData(), receivePacket.getLength(), clientAddress, clientPort));
    }
}
