package labs.lab2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

// extended task 2
public class HeartbeatClient {

    private static final int SERVER_PORT = HeartbeatServer.SERVER_PORT;
    private static final int BUFFER_SIZE = HeartbeatServer.BUFFER_SIZE;
    private static final long HEARTBEAT_INTERVAL = 1000;

    public static void main(String[] args) {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName("localhost");
            Random random = new Random();
            String clientId = String.valueOf(random.nextInt(1000) + 1);

            while (true) {
                sendHeartbeat(clientSocket, serverAddress, clientId);
                Thread.sleep(HEARTBEAT_INTERVAL);
                sendPing(clientSocket, serverAddress, random);
                receiveResponse(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendHeartbeat(DatagramSocket clientSocket, InetAddress serverAddress, String clientId) throws Exception {
        String heartbeatMessage = "Heartbeat " + clientId + " " + System.currentTimeMillis();
        sendPacket(clientSocket, serverAddress, heartbeatMessage);
    }

    private static void sendPing(DatagramSocket clientSocket, InetAddress serverAddress, Random random) throws Exception {
        int sequenceNumber = random.nextInt(1000) + 1;
        String pingMessage = "Ping " + sequenceNumber + " " + System.currentTimeMillis();
        sendPacket(clientSocket, serverAddress, pingMessage);
    }

    private static void sendPacket(DatagramSocket clientSocket, InetAddress serverAddress, String message) throws Exception {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
        clientSocket.send(sendPacket);
    }

    private static void receiveResponse(DatagramSocket clientSocket) throws Exception {
        byte[] receiveData = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
        long startTime = System.nanoTime();
        long endTime = System.nanoTime();
        double rtt = (endTime - startTime) / 1000000.0; // Calculating RTT in milliseconds
        System.out.println("Response from server: " + response + " time=" + rtt + " milliseconds");
    }
}
