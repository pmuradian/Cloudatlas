package pl.edu.mimuw.cloudatlas.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class UninstallController implements HttpHandler {

    // Takes input from the request body
    // Takes as an input &attributeName (example &num_cores)
    // Removes queries installed by that attribute from ZMIs
    public void handle(HttpExchange t) throws IOException {
        RequestExecutor.uninstallQuery(Helpers.convertStreamToString(t.getRequestBody()));
        String response = "Query uninstalled";

        t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
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

    private String signQuery(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/json");

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
            StringBuilder response = new StringBuilder();
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