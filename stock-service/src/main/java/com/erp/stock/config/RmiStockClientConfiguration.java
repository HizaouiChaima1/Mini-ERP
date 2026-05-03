package com.erp.stock.config;

import com.erp.rmi.FinanceServiceRmi;
import com.erp.rmi.VentesServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

/**
 * Configuration des clients RMI pour le service Stock
 * Permet au service Stock d'accéder aux autres services en RMI
 */
@Slf4j
@Configuration
public class RmiStockClientConfiguration {

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
