package labs.lab2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

// extended task 1
public class UDPPingClient2 {
    public static void main(String[] args) {
        DatagramSocket clientSocket = null;
        try {
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(1000);

            InetAddress serverAddress = InetAddress.getByName(UDPPingClient1.SERVER_ADDRESS);

            long[] rttTimes = new long[10];
            int lostPackets = 0;

            for (int sequenceNumber = 1; sequenceNumber <= 10; sequenceNumber++) {
                long startTime = System.nanoTime(); // Засекаем начальное время перед отправкой сообщения
                String message = String.format("Ping %d %.3f", sequenceNumber, System.nanoTime() / 1_000_000.0);
                byte[] sendData = message.getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, HeartbeatServer.SERVER_PORT);
                clientSocket.send(sendPacket);

                try {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    long endTime = System.nanoTime(); // Засекаем время приема ответа
                    double rtt = (endTime - startTime) / 1_000_000.0; // Вычисляем время RTT в миллисекундах
                    String response = new String(receivePacket.getData()).trim();
                    System.out.printf("Response from %s:%d: %s RTT=%.4f milliseconds%n", receivePacket.getAddress(), receivePacket.getPort(), response, rtt);
                    rttTimes[sequenceNumber - 1] = (long) rtt;
                } catch (java.net.SocketTimeoutException e) {
                    lostPackets++;
                    System.out.println("Request timed out");
                }
            }

            double totalRtt = 0;
            for (long rtt : rttTimes) {
                totalRtt += rtt;
            }
            double packetLoss = (double) lostPackets / 10 * 100;
            System.out.printf("--- Ping statistics ---%n10 packets transmitted, %d packets received, %.1f%% packet loss%n", 10 - lostPackets, packetLoss);
            System.out.printf("Min/Max/Avg RTT = %.6f/%.6f/%.6f seconds%n", min(rttTimes) / 1_000_000.0, max(rttTimes) / 1_000_000.0, totalRtt / (10 - lostPackets) / 1_000_000.0);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (clientSocket != null) {
                clientSocket.close();
            }
        }
    }

    private static long min(long[] array) {
        long min = Long.MAX_VALUE;
        for (long num : array) {
            if (num < min) {
                min = num;
            }
        }
        return min;
    }

    private static long max(long[] array) {
        long max = Long.MIN_VALUE;
        for (long num : array) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }
}
