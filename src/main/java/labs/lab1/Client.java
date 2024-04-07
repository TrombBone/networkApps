package labs.lab1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    // after start - edit configuration and enter server_host server_port file_path
    // example - localhost 80 src\main\java\labs\lab1\test.txt
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Using: java HTTPClient <server_host> <server_port> <file_path>");
            return;
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String filePath = args[2];

        try {
            Socket socket = new Socket(serverHost, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request = "GET " + filePath + " HTTP/1.1\r\nHost: " + serverHost + "\r\n\r\n";
            out.println(request);

            String response;
            while ((response = in.readLine()) != null) {
                System.out.println(response);
            }

            socket.close();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
