package pl.edu.mimuw.cloudatlas.cloudatlasClient;

import com.esotericsoftware.kryo.io.Input;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.controller.*;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import static java.lang.module.ModuleDescriptor.read;

public class Client {

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {

            startHTTPServer();

            String serverRegistry = "localhost";
            if (args.length > 0) {
                serverRegistry = args[0];
            }
            Registry registry = LocateRegistry.getRegistry(serverRegistry);
            RequestExecutor.executor = (QueryExecutor) registry.lookup(QueryExecutor.class.getName());

//            System.out.println("Client started");
//            System.out.println("Use \"install pathToFile\" to install queries from file");
//            System.out.println("Use \"uninstall $attributeName\" to uninstall queries for attributeName");
//            System.out.println("Use \"print_zmi pathToZMI (ex. /uw/violet07)\" to print all ZMI attributes and values");
//            System.out.println("Use \"print_attribute pathToZMI attributeName\" to print value of an attribute");
//            System.out.println("Use \"execute query\" to execute query and print its output");
//
//            Scanner scanner = new Scanner(System.in);
//            scanner.useDelimiter("\\n");
//
//            requestAttribute("/uw/violet07", "num_cores");
//            requestZMI("/uw/violet07");
////            executeQuery("SELECT 2 + 2 AS two_plus_two");
//
//            while(scanner.hasNext()) {
//                parseInput(scanner.next());
//            }
//            scanner.close();

            String[] queries = new String[] {
                    "SELECT to_integer((to_double(3) - 5.6) / 11.0 + to_double(47 * (31 - 15))) AS math WHERE true;",
                    "SELECT 2 + 2 AS two_plus_two;"
            };
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
