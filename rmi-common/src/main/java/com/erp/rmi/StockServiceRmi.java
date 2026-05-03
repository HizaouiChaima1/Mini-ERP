package com.erp.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI pour le service Stock
 * Permet l'accès distant aux fonctionnalités de gestion du stock
 */
public interface StockServiceRmi extends Remote {

    /**
     * Récupère la quantité d'un produit en stock
     * 
     * @param productId ID du produit
     * @return Quantité disponible
     * @throws RemoteException En cas de problème de communication RMI
     */
    Integer getProductQuantity(Long productId) throws RemoteException;

    /**
     * Vérifie si un produit est en stock
     * 
     * @param productId ID du produit
     * @param quantity  Quantité requise
     * @return true si la quantité est disponible, false sinon
     * @throws RemoteException En cas de problème de communication RMI
     */
    Boolean isProductAvailable(Long productId, Integer quantity) throws RemoteException;

    /**
     * Réserve une quantité d'un produit
     * 
     * @param productId ID du produit
     * @param quantity  Quantité à réserver
     * @return true si la réservation est effectuée, false si échec
     * @throws RemoteException En cas de problème de communication RMI
     */
    Boolean reserveProduct(Long productId, Integer quantity) throws RemoteException;

    /**
     * Obtient le nom du produit
     * 
     * @param productId ID du produit
     * @return Nom du produit
     * @throws RemoteException En cas de problème de communication RMI
     */
    String getProductName(Long productId) throws RemoteException;

    /**
     * Obtient le prix du produit
     * 
     * @param productId ID du produit
     * @return Prix du produit
     * @throws RemoteException En cas de problème de communication RMI
     */
    Double getProductPrice(Long productId) throws RemoteException;

}
