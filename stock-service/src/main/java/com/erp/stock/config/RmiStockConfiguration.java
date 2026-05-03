package com.erp.stock.config;

import com.erp.stock.rmi.StockServiceRmiImpl;
import com.erp.rmi.StockServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiServiceExporter;

import java.rmi.RemoteException;

/**
 * Configuration RMI pour le service Stock
 * Exporte le service Stock en tant que service RMI
 */
@Slf4j
@Configuration
public class RmiStockConfiguration {

    /**
     * Exporte le service Stock en tant que service RMI
     * Écoute sur le port 1099 par défaut
     * 
     * @return RmiServiceExporter configuré
     * @throws RemoteException En cas d'erreur RMI
     */
    @Bean
    public RmiServiceExporter stockServiceRmiExporter() throws RemoteException {
        log.info("Configuration de l'exportation RMI pour le service Stock");

        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceName("StockService");
        exporter.setServiceInterface(StockServiceRmi.class);
        exporter.setService(new StockServiceRmiImpl());
        exporter.setRegistryPort(1099);
        exporter.setRegistryHost("localhost");

        log.info("Service Stock RMI exporté sur rmi://localhost:1099/StockService");
        return exporter;
    }

}
