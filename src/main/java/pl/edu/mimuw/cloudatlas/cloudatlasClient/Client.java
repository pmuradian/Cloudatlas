package pl.edu.mimuw.cloudatlas.cloudatlasClient;

import com.sun.net.httpserver.HttpServer;
import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;
import pl.edu.mimuw.cloudatlas.controller.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String serverRegistry = "localhost";
            if (args.length > 0) {
                serverRegistry = args[0];
            }

            startHTTPServer();
            Registry registry = LocateRegistry.getRegistry(serverRegistry);
            RequestExecutor.executor = (QueryExecutor) registry.lookup(QueryExecutor.class.getName());

            System.out.println("Client is running");
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }

    private static void startHTTPServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/client", new InstallController());
        server.createContext("/client/uninstall", new UninstallController());
        server.createContext("/client/install", new InstallController());
        server.createContext("/client/printZMI", new PrintZMIController());
        server.createContext("/client/printAttribute", new PrintAttributeController());
        server.createContext("/client/execute", new QueryExecutorController());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
