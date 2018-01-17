package pl.edu.mimuw.cloudatlas.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;
import pl.edu.mimuw.cloudatlas.interpreter.GossipType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class InstallController implements HttpHandler {

    // Takes input from the request body
    // Takes as an input &attributeName: <query1>; <query2>; ... (format provided by the paper)
    // Installs queries on ZMIs and periodically executes them
    public void handle(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        HashMap<String, String[]> queries = new HashMap();

        signQuery(this.getSignerIP() + ":9778/client/sign", Helpers.convertStreamToString(is));

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
                queries.put(splitString[0], splitString[1].split(";"));
            }
        }

        RequestExecutor.installQueries(queries);

        String response = "Queries are installed";
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private String getSignerIP() {
        File iniFile = new File("config.ini");
        Ini ini = null;
        try {
            ini = new Ini(iniFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        IniPreferences prefs = new IniPreferences(ini);
        String ip = prefs.node("ip_addresses").get("query_signer", "localhost");
        return ip;
    }

    public String signQuery(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json");

//            connection.setRequestProperty("Content-Length",
//                    Integer.toString(urlParameters.getBytes().length));
//            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}