package com.erp.ventes.service;

import com.erp.rmi.StockServiceRmi;
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

    private final StockServiceRmi stockServiceRmi;
    private final FinanceServiceRmi financeServiceRmi;

    @Autowired(required = false)
    public RmiCommunicationService(StockServiceRmi stockServiceRmi, FinanceServiceRmi financeServiceRmi) {
        this.stockServiceRmi = stockServiceRmi;
        this.financeServiceRmi = financeServiceRmi;
    }

    /**
     * Exemple : Vérifier la disponibilité d'un produit via RMI
     * 
     * @param productId ID du produit
     * @param quantity  Quantité requise
     * @return true si disponible, false sinon
     */
    public Boolean checkProductAvailabilityViaRmi(Long productId, Integer quantity) {
        try {
            if (stockServiceRmi != null) {
                log.info("Appel RMI: Vérification de la disponibilité du produit {} pour quantité {}",
                        productId, quantity);
                return stockServiceRmi.isProductAvailable(productId, quantity);
            } else {
                log.warn("StockServiceRmi non disponible");
                return false;
            }
        } catch (RemoteException e) {
            log.error("Erreur lors de l'appel RMI au service Stock", e);
            return false;
        }
    }

    /**
     * Exemple : Créer une facture via RMI
     * 
     * @param orderId ID de la commande
     * @param amount  Montant
     * @param status  Statut
     * @return ID de la facture créée
     */
    public Long createInvoiceViaRmi(Long orderId, Double amount, String status) {
        try {
            if (financeServiceRmi != null) {
                log.info("Appel RMI: Création d'une facture pour la commande {}", orderId);
                return financeServiceRmi.createInvoice(orderId, amount, status);
            } else {
                log.warn("FinanceServiceRmi non disponible");
                return null;
            }
        } catch (RemoteException e) {
            log.error("Erreur lors de l'appel RMI au service Finance", e);
            return null;
        }
    }

}
