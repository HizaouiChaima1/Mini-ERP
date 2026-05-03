package com.erp.finance.config;

import com.erp.rmi.StockServiceRmi;
import com.erp.rmi.VentesServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * Configuration des clients RMI pour le service Finance
 * Permet au service Finance d'accéder aux autres services en RMI
 */
@Slf4j
@Configuration
public class RmiFinanceClientConfiguration {

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
     * Proxy RMI pour accéder au service Ventes
     * 
     * @return Proxy du service Ventes
     */
    @Bean
    @ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
    public RmiProxyFactoryBean ventesServiceRmiProxy() {
        log.info("Configuration du proxy RMI pour le service Ventes");

        RmiProxyFactoryBean rmiProxy = new RmiProxyFactoryBean();
        rmiProxy.setServiceUrl("rmi://ventes-service:1100/VentesService");
        rmiProxy.setServiceInterface(VentesServiceRmi.class);

        return rmiProxy;
    }

}
