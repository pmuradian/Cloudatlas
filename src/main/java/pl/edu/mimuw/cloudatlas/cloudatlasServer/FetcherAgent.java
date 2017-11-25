package pl.edu.mimuw.cloudatlas.cloudatlasServer;

import pl.edu.mimuw.cloudatlas.cloudatlasRMI.MachineDescriptionFetcher;
import pl.edu.mimuw.cloudatlas.interpreter.Main;

import java.rmi.RemoteException;
import java.util.Map;

public class FetcherAgent implements MachineDescriptionFetcher {
    @Override
    public void updateZMIAttributes(String zmiName, Map<String, Object> attributeMap) throws RemoteException {
        try {
            Main.updateZMIAttributes(zmiName, attributeMap);
        } catch (Exception e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }
}
