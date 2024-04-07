package labs.lab5;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ICMPPing {
    public static void main(String[] args) {
        String targetHost = "mail.ru";
        int timeoutMillis = 2000;
        int numOfPackets = 4;

        try {
            InetAddress remoteAddress = InetAddress.getByName(targetHost);
            ArrayList<Long> responseTimes = new ArrayList<>();
            int lostCount = 0;

            System.out.println("NETWORK CHECKER for " + targetHost + ":");

            for (int i = 0; i < numOfPackets; i++) {
                long startTime = System.nanoTime();

                try {
                    if (checkNetwork(remoteAddress, timeoutMillis)) {
                        long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                        responseTimes.add(responseTime);
                        System.out.println("Received response from " + remoteAddress + ": time=" + responseTime + "ms");
                    } else {
                        System.out.println("Request timed out");
                        lostCount++;
                    }
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }

                try {
                    // Wait 1 second before send
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (!responseTimes.isEmpty()) {
                long minResponseTime = responseTimes.stream().min(Long::compareTo).orElse(0L);
                long maxResponseTime = responseTimes.stream().max(Long::compareTo).orElse(0L);
                long sumResponseTime = responseTimes.stream().mapToLong(Long::longValue).sum();
                long avgResponseTime = sumResponseTime / responseTimes.size();
                long lossPercentage = lostCount / numOfPackets * 100L;

                System.out.println("\n--- " + targetHost + " network statistics ---");
                System.out.println(numOfPackets + " packets sent, " + (numOfPackets - lostCount) + " received, " +
                        lossPercentage + "% packet loss");
                System.out.println("Response time min/avg/max = " + minResponseTime + "/" + avgResponseTime + "/" + maxResponseTime + " ms");
            }

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean checkNetwork(InetAddress address, int timeout) throws IOException {
        return address.isReachable(timeout);
    }
}