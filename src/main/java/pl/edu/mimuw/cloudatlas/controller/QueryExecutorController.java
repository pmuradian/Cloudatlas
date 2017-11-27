package pl.edu.mimuw.cloudatlas.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class QueryExecutorController implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        byte[] body = new byte[100];
        is.read(body);

        String response = new String(body, StandardCharsets.UTF_8);
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
        RequestExecutor.executeQuery("");
    }
}