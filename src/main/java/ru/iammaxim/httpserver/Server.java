package ru.iammaxim.httpserver;

import ru.iammaxim.httpserver.API.APIManager;

import javax.net.ssl.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.KeyStore;

/**
 * Created by maxim on 12.09.2016.
 */
public class Server extends Thread {
    public static final int PORT = 8080;

    public ServerSocket serverSocket;

    public Server() throws IOException {
        this(PORT);
    }

    public Server(int port) throws IOException {
        super();
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();
                new ClientProcessor(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientProcessor extends Thread {
        private Socket s;
        private InputStream is;
        private OutputStream os;
        private String url;

        public ClientProcessor(Socket socket) {
            super();
            s = socket;
        }

        @Override
        public void run() {
            try {
                is = s.getInputStream();
                os = s.getOutputStream();
                String response = "no response";
                readInputHeaders();
                if (url.contains("api/")) {
                    response = APIManager.process(url.substring(5));
                }
                if (url.equals("/favicon.ico")) {
                    writeByteHeader();
                    writeImage("favicon.ico");
                    os.close();
                } else {
                    writeHTMLheader();
                    writeResponse("<html><body><h1>" + response + "</h1></body></html>");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    s.close();
                } catch (Throwable t) {
                }
            }
        }

        private void writeHTMLheader() throws IOException {
            writeResponse("HTTP/1.1 200 OK\r\n" +
                    "ru.iammaxim.httpserver.Server: HTTPServer\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + "$CONTENT_LENGTH" + "\r\n" +
                    "Connection: close\r\n\r\n");
        }

        private void writeByteHeader() throws IOException {
            writeResponse("HTTP/1.1 200 OK\r\n" +
                    "ru.iammaxim.httpserver.Server: HTTPServer\r\n" +
                    "Content-Type: image\r\n" +
                    "Content-Length: " + "$CONTENT_LENGTH" + "\r\n" +
                    "Connection: close\r\n\r\n");
        }

        private void writeImage(String filepath) throws IOException {
            File file = new File("favicon.ico");
            Files.copy(file.toPath(), os);
        }

        private void writeResponse(String s) throws IOException {
            writeResponse(s.getBytes());
        }

        private void writeResponse(byte[] data) throws IOException {
            os.write(data);
            os.flush();
        }

        private void readInputHeaders() throws Exception {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String s = br.readLine();
                if (s.startsWith("GET")) {
                    url = s.substring(s.indexOf(' ') + 1, s.lastIndexOf(' '));
                }
                if (s.trim().length() == 0) {
                    break;
                }
            }
        }
    }
}
