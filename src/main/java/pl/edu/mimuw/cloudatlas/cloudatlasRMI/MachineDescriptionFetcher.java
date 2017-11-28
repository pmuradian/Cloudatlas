package pl.edu.mimuw.cloudatlas.cloudatlasRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface MachineDescriptionFetcher extends Remote {
    void updateZMIAttributes(String zmiName, Map<String, Object> attributeMap) throws RemoteException;
}
