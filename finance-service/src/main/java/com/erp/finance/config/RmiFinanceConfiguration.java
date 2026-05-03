package com.erp.finance.config;

import com.erp.finance.rmi.FinanceServiceRmiImpl;
import com.erp.rmi.config.RmiBootstrapProperties;
import com.erp.rmi.support.RmiRegistrySupport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.rmi.RemoteException;

/**
 * Publication du registre RMI Finance ({@code application.yml : rmi.*}).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean(FinanceServiceRmiImpl.class)
public class RmiFinanceConfiguration {

    private final FinanceServiceRmiImpl financeServiceRmi;
    private final RmiBootstrapProperties rmiBootstrapProperties;

    @PostConstruct
    public void exportFinanceRmi() throws RemoteException {
        var p = rmiBootstrapProperties;
        log.info("Export RMI Finance : {} sur le port {}", p.getServiceName(), p.getRegistryPort());
        RmiRegistrySupport.exportService(p.getHostname(), p.getServiceName(), p.getRegistryPort(), financeServiceRmi);
        log.info("Lookup : rmi://{}:{}/{}", p.getHostname(), p.getRegistryPort(), p.getServiceName());
    }
}
