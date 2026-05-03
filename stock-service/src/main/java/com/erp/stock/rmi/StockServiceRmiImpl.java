package com.erp.stock.rmi;

import com.erp.rmi.StockServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implémentation RMI du service Stock
 * Exporte les fonctionnalités du service Stock en tant que service RMI
 */
@Slf4j
@Service
public class StockServiceRmiImpl extends UnicastRemoteObject implements StockServiceRmi {

    private static final long serialVersionUID = 1L;

    public StockServiceRmiImpl() throws RemoteException {
        super();
        log.info("StockServiceRmiImpl initialisé");
    }

    @Override
    public Integer getProductQuantity(Long productId) throws RemoteException {
        try {
            log.debug("RMI: Récupération de la quantité pour le produit: {}", productId);
            // TODO: Implémenter la logique avec le repository
            // return
            // stockRepository.findById(productId).map(Product::getQuantity).orElse(0);
            return 0;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la quantité", e);
            throw new RemoteException("Erreur lors de la récupération de la quantité", e);
        }
    }

    @Override
    public Boolean isProductAvailable(Long productId, Integer quantity) throws RemoteException {
        try {
            log.debug("RMI: Vérification de disponibilité du produit: {} pour quantité: {}", productId, quantity);
            // TODO: Implémenter la logique
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la vérification de disponibilité", e);
            throw new RemoteException("Erreur lors de la vérification de disponibilité", e);
        }
    }

    @Override
    public Boolean reserveProduct(Long productId, Integer quantity) throws RemoteException {
        try {
            log.debug("RMI: Réservation du produit: {} pour quantité: {}", productId, quantity);
            // TODO: Implémenter la logique de réservation
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la réservation du produit", e);
            throw new RemoteException("Erreur lors de la réservation du produit", e);
        }
    }

    @Override
    public String getProductName(Long productId) throws RemoteException {
        try {
            log.debug("RMI: Récupération du nom du produit: {}", productId);
            // TODO: Implémenter la logique
            return "Produit";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du nom du produit", e);
            throw new RemoteException("Erreur lors de la récupération du nom du produit", e);
        }
    }

    @Override
    public Double getProductPrice(Long productId) throws RemoteException {
        try {
            log.debug("RMI: Récupération du prix du produit: {}", productId);
            // TODO: Implémenter la logique
            return 0.0;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du prix du produit", e);
            throw new RemoteException("Erreur lors de la récupération du prix du produit", e);
        }
    }

}
