package com.erp.finance.service;

import com.erp.rmi.StockServiceRmi;
import com.erp.rmi.VentesServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private final VentesServiceRmi ventesServiceRmi;

    @Autowired(required = false)
    public RmiCommunicationService(@Lazy StockServiceRmi stockServiceRmi, @Lazy VentesServiceRmi ventesServiceRmi) {
        this.stockServiceRmi = stockServiceRmi;
        this.ventesServiceRmi = ventesServiceRmi;
    }

    /**
     * Exemple : Obtenir le prix d'un produit via RMI
     * 
     * @param productId ID du produit
     * @return Prix du produit
     */
    public Double getProductPriceViaRmi(Long productId) {
        try {
            if (stockServiceRmi != null) {
                log.info("Appel RMI: Récupération du prix du produit {}", productId);
                return stockServiceRmi.getProductPrice(productId);
            } else {
                log.warn("StockServiceRmi non disponible");
                return 0.0;
            }
        } catch (RemoteException e) {
            log.error("Erreur lors de l'appel RMI au service Stock", e);
            return 0.0;
        }
    }

    /**
     * Exemple : Obtenir le statut d'une commande via RMI
     * 
     * @param orderId ID de la commande
     * @return Statut de la commande
     */
    public String getOrderStatusViaRmi(Long orderId) {
        try {
            if (ventesServiceRmi != null) {
                log.info("Appel RMI: Récupération du statut de la commande {}", orderId);
                return ventesServiceRmi.getOrderStatus(orderId);
            } else {
                log.warn("VentesServiceRmi non disponible");
                return null;
            }
        } catch (RemoteException e) {
            log.error("Erreur lors de l'appel RMI au service Ventes", e);
            return null;
        }
    }

    /**
     * Exemple : Réserver un produit via RMI
     * 
     * @param productId ID du produit
     * @param quantity  Quantité à réserver
     * @return true si réservation réussie, false sinon
     */
    public Boolean reserveProductViaRmi(Long productId, Integer quantity) {
        try {
            if (stockServiceRmi != null) {
                log.info("Appel RMI: Réservation du produit {} pour quantité {}", productId, quantity);
                return stockServiceRmi.reserveProduct(productId, quantity);
            } else {
                log.warn("StockServiceRmi non disponible");
                return false;
            }
        } catch (RemoteException e) {
            log.error("Erreur lors de l'appel RMI au service Stock", e);
            return false;
        }
    }

}
