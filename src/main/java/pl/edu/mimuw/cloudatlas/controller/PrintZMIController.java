package pl.edu.mimuw.cloudatlas.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PrintZMIController implements HttpHandler {

    // Takes input from the request body
    // Takes as an input zmiPath (example /uw/violet07)
    // Returns the json serialized ZMI
    public void handle(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        ZMI zmi = RequestExecutor.requestZMI(Helpers.convertStreamToString(is));

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String response = gson.toJson(zmi);

        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}