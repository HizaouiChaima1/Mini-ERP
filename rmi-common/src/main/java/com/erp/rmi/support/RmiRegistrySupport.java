package com.erp.rmi.support;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Enregistre un objet distant sur un port (remplace l'ancien {@code RmiServiceExporter} retiré de Spring 6).
 */
public final class RmiRegistrySupport {

    private RmiRegistrySupport() {
    }

    /**
     * Crée le registre local si besoin, puis {@code rebind} du service.
     */
    /**
     * @param advertisedHostname valeur de {@code java.rmi.server.hostname} pour que les clients distants résolvent le bon hôte (Docker/K8s)
     */
    public static void exportService(String advertisedHostname, String serviceName, int registryPort,
            Remote implementation) throws RemoteException {
        if (advertisedHostname != null && !advertisedHostname.isBlank()) {
            System.setProperty("java.rmi.server.hostname", advertisedHostname.trim());
        }
        try {
            LocateRegistry.createRegistry(registryPort);
        } catch (RemoteException e) {
            // registre déjà présent dans cette JVM
        }
        Registry registry = LocateRegistry.getRegistry(registryPort);
        registry.rebind(serviceName, implementation);
    }
}
