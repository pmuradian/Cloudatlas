package pl.edu.mimuw.cloudatlas.cloudatlasClient;

import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RequestExecutor {
    public static QueryExecutor executor;

    public static void parseInput(String input) {
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

    public static void installQuery(String filePath) throws RemoteException {
        Iterator iterator = initializeFromFile(filePath).entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> query = (Map.Entry<String, String[]>)iterator.next();
            executor.installQuery(query.getKey(), query.getValue());
        }
    }

    public static void uninstallQuery(String name) throws RemoteException {
        System.out.println(executor.uninstallQuery(name));
    }

    public static void requestAttribute(String zmiPath, String attributeName) throws RemoteException {
        System.out.println(executor.attributeValue(zmiPath, attributeName));
    }

    public static ZMI requestZMI(String path) throws RemoteException {
        return executor.printAttributes(path);
    }

    public static void executeQuery(String query) throws RemoteException {
        System.out.println(executor.execute(query));
    }

    public static HashMap<String, String[]> initializeFromFile(String path) {
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
