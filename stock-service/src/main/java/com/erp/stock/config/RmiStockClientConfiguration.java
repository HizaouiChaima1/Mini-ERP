package com.erp.stock.config;

import com.erp.rmi.config.RmiBootstrapProperties;
import com.erp.rmi.FinanceServiceRmi;
import com.erp.rmi.VentesServiceRmi;
import com.erp.rmi.support.RmiServiceProxyFactoryBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Proxies RMI sortants depuis stock-service ({@code rmi.clients.*}).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RmiStockClientConfiguration {

    private final RmiBootstrapProperties rmiBootstrapProperties;

    @Bean
    @Lazy
    @ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
    public RmiServiceProxyFactoryBean<VentesServiceRmi> ventesServiceRmiProxy() {
        String url = rmiBootstrapProperties.getClients().getVentes();
        log.info("Proxy RMI Ventes -> {}", url);
        return new RmiServiceProxyFactoryBean<>(VentesServiceRmi.class, url);
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
