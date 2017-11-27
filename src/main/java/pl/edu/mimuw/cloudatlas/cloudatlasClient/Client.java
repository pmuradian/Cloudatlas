package pl.edu.mimuw.cloudatlas.cloudatlasClient;

import com.esotericsoftware.kryo.io.Input;
import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {

    private static QueryExecutor executor;
    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String serverRegistry = "localhost";
            if (args.length > 0) {
                serverRegistry = args[0];
            }
            Registry registry = LocateRegistry.getRegistry(serverRegistry);
            executor = (QueryExecutor) registry.lookup(QueryExecutor.class.getName());

            System.out.println("Client started");
            System.out.println("Use \"install pathToFile\" to install queries from file");
            System.out.println("Use \"uninstall $attributeName\" to uninstall queries for attributeName");
            System.out.println("Use \"print_zmi pathToZMI (ex. /uw/violet07)\" to print all ZMI attributes and values");
            System.out.println("Use \"print_attribute pathToZMI attributeName\" to print value of an attribute");
            System.out.println("Use \"execute query\" to execute query and print its output");

            Scanner scanner = new Scanner(System.in);
            scanner.useDelimiter("\\n");

            requestAttribute("/uw/violet07", "num_cores");
            requestZMI("/uw/violet07");
//            executeQuery("SELECT 2 + 2 AS two_plus_two");

            while(scanner.hasNext()) {
                parseInput(scanner.next());
            }
            scanner.close();

            String[] queries = new String[] {
                    "SELECT to_integer((to_double(3) - 5.6) / 11.0 + to_double(47 * (31 - 15))) AS math WHERE true;",
                    "SELECT 2 + 2 AS two_plus_two;"
            };
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }

    private static void parseInput(String input) {
        try {
            String[] args = input.split(" ");
            if (args.length == 0) {
                System.out.println("No operation to be executed");
                return;
            }
            if (args[0].equals("install")) {
                if (args.length > 1) {
                    installQuery(args[1]);
                }
            } else if (args[0].equals("uninstall")) {
                if (args.length > 1) {
                    uninstallQuery(args[1]);
                }
            } else if (args[0].equals("print_zmi")) {
                if (args.length > 1) {
                    requestZMI(args[1]);
                }
            } else if (args[0].equals("print_attribute")) {
                if (args.length > 2) {
                    requestAttribute(args[1], args[2]);
                }

            } else if (args[0].equals("execute")) {

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void installQuery(String filePath) throws RemoteException {
        Iterator iterator = initializeFromFile(filePath).entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> query = (Map.Entry<String, String[]>)iterator.next();
            executor.installQuery(query.getKey(), query.getValue());
        }
    }

    private static void uninstallQuery(String name) throws RemoteException {
        System.out.println(executor.uninstallQuery(name));
    }

    private static void requestAttribute(String zmiPath, String attributeName) throws RemoteException {
        System.out.println(executor.attributeValue(zmiPath, attributeName));
//        Kryo kryo =  new Kryo();
//        Value value = kryo.readObject(new Input(new ByteArrayInputStream(executor.attributeValue(zmiPath, attributeName))), Value.class);
//        System.out.println(value);
    }

    private static void requestZMI(String path) throws RemoteException {
        System.out.println(executor.printAttributes(path));
//        Kryo kryo =  new Kryo();
//        ZMI zmi = kryo.readObject(new Input(new ByteArrayInputStream(executor.printAttributes(path))), ZMI.class);
//        zmi.printAttributes(System.out);
    }

    private static void executeQuery(String query) throws RemoteException {
        System.out.println(executor.execute(query));
    }

    private static HashMap<String, String[]> initializeFromFile(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            HashMap queries = new HashMap();
            for (String line: lines) {

                String[] splitString = line.split(":");
                if (splitString.length != 2) {
                    System.out.println("Input error");
                }
                else {
                    queries.put(splitString[0], splitString[1].split(";"));
                }
            }
            return queries;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
