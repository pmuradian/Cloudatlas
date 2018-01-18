package pl.edu.mimuw.cloudatlas.modules.security;

import com.sun.net.httpserver.HttpServer;
import pl.edu.mimuw.cloudatlas.controller.*;

import java.io.IOException;
import java.net.InetSocketAddress;

public class QuerySigner {

    public static void main(String[] args) {
        try {
            startQuerySigner();
            System.out.println("Query signer up and running");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startQuerySigner() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9778), 0);
        server.createContext("/client/sign", new Signer());
        server.setExecutor(null);
        server.start();
    }
}
