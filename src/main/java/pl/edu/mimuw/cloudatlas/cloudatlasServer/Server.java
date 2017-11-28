package pl.edu.mimuw.cloudatlas.cloudatlasServer;

import pl.edu.mimuw.cloudatlas.cloudatlasClient.CloudatlasAgent;
import pl.edu.mimuw.cloudatlas.cloudatlasRMI.MachineDescriptionFetcher;
import pl.edu.mimuw.cloudatlas.cloudatlasRMI.QueryExecutor;
import pl.edu.mimuw.cloudatlas.fetcher.FetcherAgent;
import pl.edu.mimuw.cloudatlas.interpreter.Main;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    public Server() {
        super();
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            Main.initializeHierarchy();

            System.out.println("Starting Server");

            // Fetcher listener
            MachineDescriptionFetcher fetcher = new FetcherAgent();
            MachineDescriptionFetcher machineStub = (MachineDescriptionFetcher) UnicastRemoteObject.exportObject(fetcher, 0);
            Registry machineRegistry = LocateRegistry.getRegistry();
            machineRegistry.rebind(MachineDescriptionFetcher.class.getName(), machineStub);
            System.out.println("Fetcher bound");

            // Cloudatlas agent
            QueryExecutor agent = new CloudatlasAgent();
            QueryExecutor stub = (QueryExecutor) UnicastRemoteObject.exportObject(agent, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(QueryExecutor.class.getName(), stub);
            System.out.println("Agent bound");
        } catch (Exception e) {
            System.err.println("QueryExecutor exception:");
            e.printStackTrace();
        }
    }
}
