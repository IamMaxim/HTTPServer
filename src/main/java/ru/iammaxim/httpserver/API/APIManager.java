package ru.iammaxim.httpserver.API;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by maxim on 13.09.2016.
 */
public class APIManager {
    private static HashMap<String, MethodBase> methods = new HashMap<>();

    public static void addMethod(String name, MethodBase method) {
        methods.put(name, method);
    }

    public static MethodBase getMethod(String name) {
        return methods.get(name);
    }

    public static String process(String url) {
        int indexOfArgsStart = url.indexOf('?');
        MethodBase method;
        String[] args;
        if (indexOfArgsStart == -1) {
            method = getMethod(url);
            args = new String[0];
        } else {
            method = getMethod(url.substring(0, indexOfArgsStart));
            args = url.substring(indexOfArgsStart + 1).split("&");
        }
        if (method != null) {
            return method.execute(args);
        }
        return new JSONObject().put("error", Error.METHOD_NOT_FOUND).toString();
    }
}
