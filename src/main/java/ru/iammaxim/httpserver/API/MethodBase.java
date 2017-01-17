package ru.iammaxim.httpserver.API;

import java.util.HashMap;

/**
 * Created by maxim on 14.09.2016.
 */
public abstract class MethodBase {
    public abstract String execute(String[] args);

    private HashMap<String, String> getArgs(String[] keysAndValues) {
        HashMap<String, String> args = new HashMap<>();
        for (String arg : keysAndValues) {
            String[] keyAndValue = arg.split("=");
            args.put(keyAndValue[0], keyAndValue[1]);
        }
        return args;
    }
}
