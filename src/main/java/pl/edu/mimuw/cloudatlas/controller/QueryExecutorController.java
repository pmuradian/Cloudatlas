package pl.edu.mimuw.cloudatlas.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class QueryExecutorController implements HttpHandler {

    // Takes input from the request body
    // Takes as an input a query (example SELECT 2 + 2 AS two_plus_two)
    // Returns result of the query performed on all but singleton ZMIs
    public void handle(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        HashMap result = RequestExecutor.executeQuery(Helpers.convertStreamToString(is));
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String response = gson.toJson(result);

        t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}