package pl.edu.mimuw.cloudatlas.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pl.edu.mimuw.cloudatlas.Helpers.Helpers;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class UninstallController implements HttpHandler {

    // Takes as an input &attributeName (example &num_cores)
    // Removes queries installed by that attribute from ZMIs
    public void handle(HttpExchange t) throws IOException {
        RequestExecutor.uninstallQuery(Helpers.convertStreamToString(t.getRequestBody()));
        String response = "Query uninstalled";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}