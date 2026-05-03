package com.erp.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI pour le service Ventes
 * Permet l'accès distant aux fonctionnalités de gestion des ventes
 */
public interface VentesServiceRmi extends Remote {

    /**
     * Crée une nouvelle commande
     * 
     * @param clientName  Nom du client
     * @param totalAmount Montant total
     * @param status      Statut de la commande
     * @return ID de la commande créée
     * @throws RemoteException En cas de problème de communication RMI
     */
    Long createOrder(String clientName, Double totalAmount, String status) throws RemoteException;

    /**
     * Récupère le statut d'une commande
     * 
     * @param orderId ID de la commande
     * @return Statut de la commande
     * @throws RemoteException En cas de problème de communication RMI
     */
    String getOrderStatus(Long orderId) throws RemoteException;

    /**
     * Met à jour le statut d'une commande
     * 
     * @param orderId ID de la commande
     * @param status  Nouveau statut
     * @return true si la mise à jour est effectuée, false sinon
     * @throws RemoteException En cas de problème de communication RMI
     */
    Boolean updateOrderStatus(Long orderId, String status) throws RemoteException;

    /**
     * Récupère le montant total d'une commande
     * 
     * @param orderId ID de la commande
     * @return Montant total
     * @throws RemoteException En cas de problème de communication RMI
     */
    Double getOrderTotal(Long orderId) throws RemoteException;

    /**
     * Compte le nombre de commandes pour un client
     * 
     * @param clientName Nom du client
     * @return Nombre de commandes
     * @throws RemoteException En cas de problème de communication RMI
     */
    Integer countOrdersByClient(String clientName) throws RemoteException;

}
