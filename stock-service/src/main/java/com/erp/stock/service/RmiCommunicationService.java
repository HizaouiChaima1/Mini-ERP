package com.erp.stock.service;

import com.erp.rmi.VentesServiceRmi;
import com.erp.rmi.FinanceServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;

/**
 * Service démontrant l'utilisation des proxies RMI
 * pour communiquer avec les autres services
 */
@Slf4j
@Service
public class RmiCommunicationService {

    private final VentesServiceRmi ventesServiceRmi;
    private final FinanceServiceRmi financeServiceRmi;

    @Autowired(required = false)
    public RmiCommunicationService(VentesServiceRmi ventesServiceRmi, FinanceServiceRmi financeServiceRmi) {
        this.ventesServiceRmi = ventesServiceRmi;
        this.financeServiceRmi = financeServiceRmi;
    }

    /**
     * Exemple : Obtenir le nombre de commandes pour un client via RMI
     * 
     * @param clientName Nom du client
     * @return Nombre de commandes
     */
    public Integer getClientOrderCountViaRmi(String clientName) {
        try {
            if (ventesServiceRmi != null) {
                log.info("Appel RMI: Récupération du nombre de commandes pour {}", clientName);
                return ventesServiceRmi.countOrdersByClient(clientName);
            } else {
                log.warn("VentesServiceRmi non disponible");
                return 0;
            }
        } catch (RemoteException e) {
            log.error("Erreur lors de l'appel RMI au service Ventes", e);
            return 0;
        }
    }

    /**
     * Exemple : Obtenir le solde total des factures impayées via RMI
     * 
     * @return Montant total impayé
     */
    public Double getTotalOutstandingBalanceViaRmi() {
        try {
            if (financeServiceRmi != null) {
                log.info("Appel RMI: Récupération du solde total des factures impayées");
                return financeServiceRmi.getTotalOutstandingBalance();
            } else {
                log.warn("FinanceServiceRmi non disponible");
                return 0.0;
            }
        } catch (RemoteException e) {
            log.error("Erreur lors de l'appel RMI au service Finance", e);
            return 0.0;
        }
    }

}
