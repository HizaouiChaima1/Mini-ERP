package com.erp.ventes.config;

import com.erp.rmi.StockServiceRmi;
import com.erp.rmi.FinanceServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * Configuration des clients RMI pour le service Ventes
 * Permet au service Ventes d'accéder aux autres services en RMI
 */
@Slf4j
@Configuration
public class RmiVentesClientConfiguration {

    /**
     * Proxy RMI pour accéder au service Stock
     * 
     * @return Proxy du service Stock
     */
    @Bean
    @ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
    public RmiProxyFactoryBean stockServiceRmiProxy() {
        log.info("Configuration du proxy RMI pour le service Stock");

        RmiProxyFactoryBean rmiProxy = new RmiProxyFactoryBean();
        rmiProxy.setServiceUrl("rmi://stock-service:1099/StockService");
        rmiProxy.setServiceInterface(StockServiceRmi.class);

        return rmiProxy;
    }

    /**
     * Proxy RMI pour accéder au service Finance
     * 
     * @return Proxy du service Finance
     */
    @Bean
    @ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
    public RmiProxyFactoryBean financeServiceRmiProxy() {
        log.info("Configuration du proxy RMI pour le service Finance");

        RmiProxyFactoryBean rmiProxy = new RmiProxyFactoryBean();
        rmiProxy.setServiceUrl("rmi://finance-service:1101/FinanceService");
        rmiProxy.setServiceInterface(FinanceServiceRmi.class);

        return rmiProxy;
    }

}
