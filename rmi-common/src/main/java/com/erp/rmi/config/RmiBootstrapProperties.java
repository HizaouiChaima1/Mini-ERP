package com.erp.rmi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Ports / noms d'export RMI et URLs des stubs distants. Surchargés par application.yml dans chaque microservice.
 */
@ConfigurationProperties(prefix = "rmi")
public class RmiBootstrapProperties {

    private boolean enabled = true;

    /** Hostname/IP annoncé dans les stubs (obligatoire en Docker). */
    private String hostname = "localhost";

    private int registryPort = 1099;

    private String serviceName = "StockService";

    private final Clients clients = new Clients();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Clients getClients() {
        return clients;
    }

    public static class Clients {
        /** URL complète pour Naming.lookup(rmi://hôte:port/NomBean) */
        private String stock = "rmi://localhost:1099/StockService";
        private String ventes = "rmi://localhost:1100/VentesService";
        private String finance = "rmi://localhost:1101/FinanceService";

        public String getStock() {
            return stock;
        }

        public void setStock(String stock) {
            this.stock = stock;
        }

        public String getVentes() {
            return ventes;
        }

        public void setVentes(String ventes) {
            this.ventes = ventes;
        }

        public String getFinance() {
            return finance;
        }

        public void setFinance(String finance) {
            this.finance = finance;
        }
    }
}
