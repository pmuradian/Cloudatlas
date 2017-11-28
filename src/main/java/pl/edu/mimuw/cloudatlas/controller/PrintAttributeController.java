package pl.edu.mimuw.cloudatlas.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;
import pl.edu.mimuw.cloudatlas.model.Value;

import java.io.IOException;
import java.io.OutputStream;

public class PrintAttributeController implements HttpHandler {

    // Takes input from the request body
    // Takes as an input zmiPath.attributeName (example /uw/violet07:num_cores)
    // Returns name and value of the attribute
    public void handle(HttpExchange t) throws IOException {
        String requestBody = Helpers.convertStreamToString(t.getRequestBody());
        String[] body = requestBody.split(":");
        if (body.length != 2) {
            String response = "Wrong input";
            t.sendResponseHeaders(400, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }
        Value value = RequestExecutor.requestAttribute(body[0], body[1]);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String response = gson.toJson(value);
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}