package pl.edu.mimuw.cloudatlas.cloudatlasRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QueryExecutor extends Remote {
    String execute(String query) throws RemoteException;
}
