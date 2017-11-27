package pl.edu.mimuw.cloudatlas.cloudatlasServer;

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
            return Main.executeQueries(Main.root, query);
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String installQuery(String attributeName, String[] queries) throws RemoteException {
        try {
            Main.installQuery(Main.root, attributeName, queries);
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
            if (Main.uninstallQuery(Main.root, attributeName)) {
                System.out.println("Query uninstalled");
                return "Query uninstalled";
            }
            else {
                // TODO: handle error
            }
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] attributeValue(String zmiPath, String attributeName) throws RemoteException {
        try {
            Value value = Main.root.sonForPath(zmiPath).getAttributes().get(attributeName);
            Kryo kryo = new Kryo();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Output output = new Output(stream);
            kryo.writeObject(output, value);
            output.close();
            byte[] buffer = stream.toByteArray();
            return buffer;
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] printAttributes(String zmiPath) throws RemoteException {
        try {
            ZMI zmi = Main.printZMIAttributes(Main.root.sonForPath(zmiPath));
            Kryo kryo = new Kryo();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Output output = new Output(stream);
            kryo.writeObject(output, zmi);
            output.close();
            byte[] buffer = stream.toByteArray();
            return buffer;
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
        return null;
    }
}
