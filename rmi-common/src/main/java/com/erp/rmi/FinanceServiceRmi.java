package com.erp.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface RMI pour le service Finance
 * Permet l'accès distant aux fonctionnalités de gestion financière
 */
public interface FinanceServiceRmi extends Remote {

    /**
     * Crée une nouvelle facture
     * 
     * @param orderId ID de la commande associée
     * @param amount  Montant de la facture
     * @param status  Statut de la facture
     * @return ID de la facture créée
     * @throws RemoteException En cas de problème de communication RMI
     */
    Long createInvoice(Long orderId, Double amount, String status) throws RemoteException;

    /**
     * Récupère le statut d'une facture
     * 
     * @param invoiceId ID de la facture
     * @return Statut de la facture
     * @throws RemoteException En cas de problème de communication RMI
     */
    String getInvoiceStatus(Long invoiceId) throws RemoteException;

    /**
     * Met à jour le statut d'une facture
     * 
     * @param invoiceId ID de la facture
     * @param status    Nouveau statut
     * @return true si la mise à jour est effectuée, false sinon
     * @throws RemoteException En cas de problème de communication RMI
     */
    Boolean updateInvoiceStatus(Long invoiceId, String status) throws RemoteException;

    /**
     * Récupère le montant d'une facture
     * 
     * @param invoiceId ID de la facture
     * @return Montant de la facture
     * @throws RemoteException En cas de problème de communication RMI
     */
    Double getInvoiceAmount(Long invoiceId) throws RemoteException;

    /**
     * Crée un paiement pour une facture
     * 
     * @param invoiceId     ID de la facture
     * @param amount        Montant payé
     * @param paymentMethod Méthode de paiement
     * @return ID du paiement créé
     * @throws RemoteException En cas de problème de communication RMI
     */
    Long createPayment(Long invoiceId, Double amount, String paymentMethod) throws RemoteException;

    /**
     * Obtient le solde total des factures impayées
     * 
     * @return Montant total impayé
     * @throws RemoteException En cas de problème de communication RMI
     */
    Double getTotalOutstandingBalance() throws RemoteException;

}
