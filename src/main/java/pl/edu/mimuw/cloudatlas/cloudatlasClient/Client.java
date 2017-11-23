package pl.edu.mimuw.cloudatlas.cloudatlasClient;

import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Registry registry = LocateRegistry.getRegistry(args[0]);
            QueryExecutor executor = (QueryExecutor) registry.lookup(QueryExecutor.class.getName());
            System.out.println(executor.execute("SELECT 1 AS one"));
        } catch (Exception e) {
            System.err.println("ComputePi exception:");
            e.printStackTrace();
        }
    }
}
