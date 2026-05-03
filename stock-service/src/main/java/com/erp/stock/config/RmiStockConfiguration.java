package com.erp.stock.config;

import com.erp.rmi.config.RmiBootstrapProperties;
import com.erp.rmi.support.RmiRegistrySupport;
import com.erp.stock.rmi.StockServiceRmiImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.rmi.RemoteException;

/**
 * Publication du registre RMI Stock (voir {@code application.yml : rmi.*}).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(StockServiceRmiImpl.class)
public class RmiStockConfiguration {

    private final StockServiceRmiImpl stockServiceRmi;
    private final RmiBootstrapProperties rmiBootstrapProperties;

    @PostConstruct
    public void exportStockRmi() throws RemoteException {
        var p = rmiBootstrapProperties;
        log.info("Export RMI Stock : {} sur le port {}", p.getServiceName(), p.getRegistryPort());
        RmiRegistrySupport.exportService(p.getHostname(), p.getServiceName(), p.getRegistryPort(), stockServiceRmi);
        log.info("Clients distants peuvent faire lookup : rmi://{}:{}/{}", p.getHostname(), p.getRegistryPort(),
                p.getServiceName());
    }
}
