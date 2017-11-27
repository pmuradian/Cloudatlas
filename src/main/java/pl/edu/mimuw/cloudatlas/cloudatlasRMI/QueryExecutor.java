package pl.edu.mimuw.cloudatlas.cloudatlasRMI;

import pl.edu.mimuw.cloudatlas.model.Value;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface QueryExecutor extends Remote {
    HashMap<String, Value> execute(String query) throws RemoteException;
    String installQuery(String attributeName, String[] queries) throws RemoteException;
    String uninstallQuery(String attributeName) throws RemoteException;
    Value attributeValue(String zmiPath, String attributeName) throws RemoteException;
    ZMI printAttributes(String zmiPath) throws RemoteException;
}
