package pl.edu.mimuw.cloudatlas.cloudatlasServer;

import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;
import pl.edu.mimuw.cloudatlas.interpreter.Main;

import java.rmi.RemoteException;

public class CloudatlasAgent implements QueryExecutor {
    @Override
    public String execute(String query) throws RemoteException {
        try {
            Main.executeQueries(Main.root, query);
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
        return "Query calculated";
    }
}
