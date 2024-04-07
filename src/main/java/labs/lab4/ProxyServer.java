package labs.lab4;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

public class ProxyServer {

    private static final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    // after start - edit configuration and enter port number in args
    public static void main(String[] args) {
        int port;
        port = Integer.parseInt(args[0]);
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            System.out.println("Failed to start server on port" + port);
            System.exit(1);
        }
        server.createContext("/", new ProxyHandler(port));
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port " + port);
    }

    static class ProxyHandler implements HttpHandler {
        private final int port;

        public ProxyHandler(int port) {
            this.port = port;
        }

        public void handle(HttpExchange t) {
            String method = t.getRequestMethod();

            try {
                String url;
                if (t.getRequestHeaders().get("Referer") != null) {
                    url = t.getRequestHeaders().get("Referer").get(0)
                            .replace("localhost:" + port + "/", "") + t.getRequestURI().toString();
                } else {
                    url = "http://" + t.getRequestURI().toString().substring(1);
                }

                if (cache.containsKey(url)) {
                    System.out.println("Cache hit for URL: " + url);
                    serveFromCache(t, cache.get(url));
                } else {
                    System.out.println("Cache miss for URL: " + url);
                    serveFromRemoteServer(t, method, url);
                }
            } catch (Exception e) {
                System.out.println("Failed load resource " + t.getRequestURI());
            }
        }

        private void serveFromCache(HttpExchange t, byte[] cachedResponse) throws IOException {
            t.sendResponseHeaders(200, cachedResponse.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(cachedResponse);
            }
        }

        private void serveFromRemoteServer(HttpExchange t, String method, String url) throws IOException {
            HttpClient httpclient = HttpClients.createDefault();
            HttpResponse response = null;

            try {
                System.out.println("Request URL: " + url);

                if ("GET".equalsIgnoreCase(method)) {
                    HttpGet httpGet = new HttpGet(url);
                    response = httpclient.execute(httpGet);
                } else if ("POST".equalsIgnoreCase(method)) {
                    HttpPost httpPost = new HttpPost(url);
                    InputStream requestBody = t.getRequestBody();
                    httpPost.setEntity(new InputStreamEntity(requestBody));
                    response = httpclient.execute(httpPost);
                }

                if (response != null) {
                    byte[] responseBody = EntityUtils.toByteArray(response.getEntity());
                    cache.put(url, responseBody);

                    Map<String, List<String>> headers = new HashMap<>();
                    Arrays.stream(response.getAllHeaders()).forEach(header ->
                            headers.put(header.getName(), Collections.singletonList(header.getValue())));

                    t.getResponseHeaders().putAll(headers);
                    t.sendResponseHeaders(response.getStatusLine().getStatusCode(), responseBody.length);

                    try (OutputStream os = t.getResponseBody()) {
                        os.write(responseBody);
                    }
                }
            } catch (Exception e) {
                String errorPage = "<html><body><h1>Error 404: Not Found</h1></body></html>";
                t.sendResponseHeaders(404, errorPage.getBytes().length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(errorPage.getBytes());
                }
            } finally {
                EntityUtils.consumeQuietly(response != null ? response.getEntity() : null);
            }
        }
    }
}
