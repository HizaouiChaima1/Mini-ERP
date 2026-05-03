package com.erp.ventes.config;

import com.erp.rmi.config.RmiBootstrapProperties;
import com.erp.rmi.support.RmiRegistrySupport;
import com.erp.ventes.rmi.VentesServiceRmiImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.rmi.RemoteException;

/**
 * Publication du registre RMI Ventes ({@code application.yml : rmi.*}).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(VentesServiceRmiImpl.class)
public class RmiVentesConfiguration {

    private final VentesServiceRmiImpl ventesServiceRmi;
    private final RmiBootstrapProperties rmiBootstrapProperties;

    @PostConstruct
    public void exportVentesRmi() throws RemoteException {
        var p = rmiBootstrapProperties;
        log.info("Export RMI Ventes : {} sur le port {}", p.getServiceName(), p.getRegistryPort());
        RmiRegistrySupport.exportService(p.getHostname(), p.getServiceName(), p.getRegistryPort(), ventesServiceRmi);
        log.info("Lookup : rmi://{}:{}/{}", p.getHostname(), p.getRegistryPort(), p.getServiceName());
    }
}
