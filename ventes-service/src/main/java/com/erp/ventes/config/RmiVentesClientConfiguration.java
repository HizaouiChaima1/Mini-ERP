package com.erp.ventes.config;

import com.erp.rmi.config.RmiBootstrapProperties;
import com.erp.rmi.FinanceServiceRmi;
import com.erp.rmi.StockServiceRmi;
import com.erp.rmi.support.RmiServiceProxyFactoryBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Proxies RMI sortants depuis ventes-service ({@code rmi.clients.*}).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RmiVentesClientConfiguration {

    private final RmiBootstrapProperties rmiBootstrapProperties;

    @Bean
    @Lazy
    @ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
    public RmiServiceProxyFactoryBean<StockServiceRmi> stockServiceRmiProxy() {
        String url = rmiBootstrapProperties.getClients().getStock();
        log.info("Proxy RMI Stock -> {}", url);
        return new RmiServiceProxyFactoryBean<>(StockServiceRmi.class, url);
    }

    @Bean
    @Lazy
    @ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
    public RmiServiceProxyFactoryBean<FinanceServiceRmi> financeServiceRmiProxy() {
        String url = rmiBootstrapProperties.getClients().getFinance();
        log.info("Proxy RMI Finance -> {}", url);
        return new RmiServiceProxyFactoryBean<>(FinanceServiceRmi.class, url);
    }
}
