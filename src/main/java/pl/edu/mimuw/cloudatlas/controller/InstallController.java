package pl.edu.mimuw.cloudatlas.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pl.edu.mimuw.cloudatlas.Helpers.Helpers;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class InstallController implements HttpHandler {

    // Takes as an input &attributeName: <query1>; <query2>; ... (format provided by the paper)
    // Installs queries on ZMIs and periodically executes them
    public void handle(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        HashMap<String, String[]> queries = new HashMap();
        for (String line: Helpers.convertStreamToString(is).split("\n")) {

            String[] splitString = line.split(":");
            if (splitString.length != 2) {
                String response = "Wrong input";
                t.sendResponseHeaders(400, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            else {
                queries.put(splitString[0],  splitString[1].split(";"));
            }
        }

        RequestExecutor.installQueries(queries);

        String response = "Queries are installed";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}