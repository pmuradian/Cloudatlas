package pl.edu.mimuw.cloudatlas.cloudatlasServer;

import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;
import pl.edu.mimuw.cloudatlas.interpreter.Main;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server implements QueryExecutor {

    public Server() {
        super();
    }

    @Override
    public String execute(String query) throws RemoteException {
        try {
            Main.executeQueries(Main.root, query);
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
        return "asdf";
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            System.out.println("Starting Server");
            QueryExecutor engine = new Server();
            QueryExecutor stub = (QueryExecutor) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry();
            String name = QueryExecutor.class.getName();
            registry.rebind(name, stub);
            System.out.println("Server bound");
        } catch (Exception e) {
            System.err.println("ComputeEngine exception:");
            e.printStackTrace();
        }
    }

    public String adf() {
        return "asdf";
    }
}
