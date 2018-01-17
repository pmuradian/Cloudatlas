package pl.edu.mimuw.cloudatlas.cloudatlasClient;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.util.HashMap;

public class CloudatlasAgent implements QueryExecutor {
    @Override
    public HashMap<String, Value> execute(String query) throws RemoteException {
        try {
            return Main.executeQueries(Main.getNode(), query);
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String installQuery(String attributeName, String[] queries) throws RemoteException {
        try {
            Main.installQuery(Main.getNode(), attributeName, queries);
            System.out.println("Query installed");
            return "Query installed";
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String uninstallQuery(String attributeName) throws RemoteException {
        try {
            return Main.uninstallQuery(Main.getNode(), attributeName);
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Value getAttributeValue(String zmiPath, String attributeName) throws RemoteException {
        try {
            return Main.root.sonForPath(zmiPath).getAttributes().get(attributeName);
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ZMI getAttributes(String zmiPath) throws RemoteException {
        try {
            return Main.printZMIAttributes(Main.root.sonForPath(zmiPath));
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
        return null;
    }
}
