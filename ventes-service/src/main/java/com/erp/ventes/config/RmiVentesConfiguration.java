package com.erp.ventes.config;

import com.erp.ventes.rmi.VentesServiceRmiImpl;
import com.erp.rmi.VentesServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiServiceExporter;

import java.rmi.RemoteException;

/**
 * Configuration RMI pour le service Ventes
 * Exporte le service Ventes en tant que service RMI
 */
@Slf4j
@Configuration
public class RmiVentesConfiguration {

    /**
     * Exporte le service Ventes en tant que service RMI
     * Écoute sur le port 1100
     * 
     * @return RmiServiceExporter configuré
     * @throws RemoteException En cas d'erreur RMI
     */
    @Bean
    public RmiServiceExporter ventesServiceRmiExporter() throws RemoteException {
        log.info("Configuration de l'exportation RMI pour le service Ventes");

        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceName("VentesService");
        exporter.setServiceInterface(VentesServiceRmi.class);
        exporter.setService(new VentesServiceRmiImpl());
        exporter.setRegistryPort(1100);
        exporter.setRegistryHost("localhost");

        log.info("Service Ventes RMI exporté sur rmi://localhost:1100/VentesService");
        return exporter;
    }

}
