package pl.edu.mimuw.cloudatlas.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.ini4j.Ini;
import org.ini4j.IniPreferences;
import pl.edu.mimuw.cloudatlas.helpers.Helpers;
import pl.edu.mimuw.cloudatlas.cloudatlasClient.RequestExecutor;
import pl.edu.mimuw.cloudatlas.interpreter.GossipType;
import pl.edu.mimuw.cloudatlas.modules.security.SHA1;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class InstallController implements HttpHandler {

    private final static String ENCRYPTION_ALGORITHM = "RSA";
    private final static int NUM_KEY_BITS = 1024;

    // Takes input from the request body
    // Takes as an input &attributeName: <query1>; <query2>; ... (format provided by the paper)
    // Installs queries on ZMIs and periodically executes them
    public void handle(HttpExchange t) throws IOException {
        InputStream is = t.getRequestBody();
        HashMap<String, String[]> queries = new HashMap();

        String json = signQuery("http://" + this.getSignerIP() + ":9778/client/sign", Helpers.convertStreamToString(is));

        Type type = new TypeToken<HashMap<String, Object>>(){}.getType();
        Gson gson = new Gson();
        HashMap<String, Object> res = gson.fromJson(json, type);
        PublicKey key = Helpers.stringToPublicKey((String)res.get("key"));

        String response = "Queries are installed";

        if (!checkSignature(key, Helpers.arrayToBytes((ArrayList<Double>) res.get("signature")), Helpers.convertStreamToString(is))) {
            response = "Query signer SHA1 mismatch: Query installation denied";
        } else {
            for (String line: Helpers.convertStreamToString(is).split("\n")) {

                String[] splitString = line.split(":");
                if (splitString.length != 2) {
                    response = "Wrong query syntax";
                    System.out.println(response);
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
        }
        System.out.println(response);

        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private boolean checkSignature(PublicKey key, byte[] signature, String originalString) {
        try {
            Cipher verifyCipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            verifyCipher.init(Cipher.DECRYPT_MODE, key);
            String decryptedBytes = new String(verifyCipher.doFinal(signature));
            String shaValue = new String(SHA1.calculate(originalString.getBytes()));

            //check calculated and received SHA1 values
            if (decryptedBytes.equals(shaValue)) {
                return true;
            }
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return false;
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