package labs.lab2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

// main solution
public class UDPPingClient1 {

    private static final int SERVER_PORT = HeartbeatServer.SERVER_PORT;
    static final String SERVER_ADDRESS = "localhost";

    public static void main(String[] args) {
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(1000);

            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);

            for (int sequenceNumber = 1; sequenceNumber <= 10; sequenceNumber++) {
                long startTime = System.nanoTime(); // Засекаем начальное время перед отправкой сообщения
                String message = String.format("Ping %d %.3f", sequenceNumber, startTime / 1_000_000.0);
                byte[] sendData = message.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                clientSocket.send(sendPacket);

                try {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    long endTime = System.nanoTime(); // Засекаем время приема ответа
                    double rtt = (endTime - startTime) / 1_000_000.0; // Вычисляем время RTT в миллисекундах
                    String response = new String(receivePacket.getData()).trim();
                    System.out.printf("Response from %s:%d: %s RTT=%.4f milliseconds%n", receivePacket.getAddress(), receivePacket.getPort(), response, rtt);
                } catch (SocketTimeoutException e) {
                    System.out.println("Request timed out");
                }
            }

            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
