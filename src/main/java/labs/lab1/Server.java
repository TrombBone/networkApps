package labs.lab1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 80;
    private static final int THREAD_POOL_SIZE = 4;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Ready to serve...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted");

                Runnable worker = new RequestHandler(clientSocket);
                executor.execute(worker);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private record RequestHandler(Socket clientSocket) implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String request = in.readLine();
                String filename = request.split(" ")[1];

                try (FileInputStream fileInputStream = new FileInputStream(filename)) {
                    out.println("HTTP/1.1 200 OK\r\n\r\n");
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        clientSocket.getOutputStream().write(buffer, 0, bytesRead);
                    }
                } catch (FileNotFoundException e) {
                    out.println("HTTP/1.1 404 Not Found\r\n\r\nFile Not Found");
                }

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
