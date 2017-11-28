package pl.edu.mimuw.cloudatlas.cloudatlasClient;

import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;
import pl.edu.mimuw.cloudatlas.model.Value;
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

    public static void installQueries(HashMap<String, String[]> queries) throws RemoteException {
        Iterator iterator = queries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> query = (Map.Entry<String, String[]>)iterator.next();
            executor.installQuery(query.getKey(), query.getValue());
        }
    }

    public static String uninstallQuery(String name) throws RemoteException {
        return executor.uninstallQuery(name);
    }

    public static Value requestAttribute(String zmiPath, String attributeName) throws RemoteException {
        return executor.getAttributeValue(zmiPath, attributeName);
    }

    public static ZMI requestZMI(String path) throws RemoteException {
        return executor.getAttributes(path);
    }

    public static HashMap<String, Value> executeQuery(String query) throws RemoteException {
        return executor.execute(query);
    }
}
