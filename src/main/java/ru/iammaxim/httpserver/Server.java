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

    public SSLServerSocket serverSocket;

    public Server() throws IOException {
        this(PORT);
    }

    private SSLContext createSSLContext() {
        try {
            try {
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(null, null);

                keyStore.store(new FileOutputStream("ssl.jks"), ("password").toCharArray());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            KeyStore keyStore = KeyStore.getInstance("JKS");
            InputStream is = new FileInputStream("ssl.jks");
            keyStore.load(is, "password".toCharArray());
            is.close();
            //"SunX509"
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "password".toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(km, tm, null);
            return sslContext;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Server(int port) throws IOException {
        super();
        SSLContext sslContext = createSSLContext();
        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
        serverSocket.setEnabledProtocols(new String[] {"TLSv1", "TLSv1.1", "TLSv1.2", "SSLv3"});
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                new ClientProcessor(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class ClientProcessor extends Thread {
        private SSLSocket s;
        private InputStream is;
        private OutputStream os;
        private String url;
//        private int contentLength = 0;

        public ClientProcessor(SSLSocket socket) {
            super();
            s = socket;
        }

        @Override
        public void run() {
            try {
                s.setEnabledCipherSuites(s.getSupportedCipherSuites());
                s.startHandshake();
                SSLSession sslSession = s.getSession();

                System.out.println("SSLSession :");
                System.out.println("\tProtocol : " + sslSession.getProtocol());
                System.out.println("\tCipher suite : " + sslSession.getCipherSuite());

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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
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

        private void readInputHeaders() throws Throwable {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String s = br.readLine();
                if (s.startsWith("GET")) {
                    url = s.substring(s.indexOf(' ') + 1, s.lastIndexOf(' '));
                }
                if (s == null || s.trim().length() == 0) {
                    break;
                }
            }
        }
    }
}
