package ru.iammaxim.httpserver;

import ru.iammaxim.httpserver.API.APIManager;
import ru.iammaxim.httpserver.API.MethodBase;

import java.io.IOException;

/**
 * Created by maxim on 12.09.2016.
 */
public class Main {
    public static Main instance;
    public Server server;

    private void init() {
        try {
            APIManager.addMethod("debug.echo", new MethodBase() {
                @Override
                public String execute(String[] args) {
                    StringBuilder sb = new StringBuilder();
                    for (String arg : args) {
                        sb.append(arg).append('\n');
                    }
                    return sb.toString();
                }
            });

            server = new Server();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        instance = new Main();
        instance.init();
    }
}
