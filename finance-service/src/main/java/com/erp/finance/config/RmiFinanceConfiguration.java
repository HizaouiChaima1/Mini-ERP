package com.erp.finance.config;

import com.erp.finance.rmi.FinanceServiceRmiImpl;
import com.erp.rmi.FinanceServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiServiceExporter;

import java.rmi.RemoteException;

/**
 * Configuration RMI pour le service Finance
 * Exporte le service Finance en tant que service RMI
 */
@Slf4j
@Configuration
public class RmiFinanceConfiguration {

    /**
     * Exporte le service Finance en tant que service RMI
     * Écoute sur le port 1101
     * 
     * @return RmiServiceExporter configuré
     * @throws RemoteException En cas d'erreur RMI
     */
    @Bean
    public RmiServiceExporter financeServiceRmiExporter() throws RemoteException {
        log.info("Configuration de l'exportation RMI pour le service Finance");

        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceName("FinanceService");
        exporter.setServiceInterface(FinanceServiceRmi.class);
        exporter.setService(new FinanceServiceRmiImpl());
        exporter.setRegistryPort(1101);
        exporter.setRegistryHost("localhost");

        log.info("Service Finance RMI exporté sur rmi://localhost:1101/FinanceService");
        return exporter;
    }

}
