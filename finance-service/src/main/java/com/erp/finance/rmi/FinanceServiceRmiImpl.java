package com.erp.finance.rmi;

import com.erp.rmi.FinanceServiceRmi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implémentation RMI du service Finance
 * Exporte les fonctionnalités du service Finance en tant que service RMI
 */
@Slf4j
@Service
public class FinanceServiceRmiImpl extends UnicastRemoteObject implements FinanceServiceRmi {

    private static final long serialVersionUID = 1L;

    public FinanceServiceRmiImpl() throws RemoteException {
        super();
        log.info("FinanceServiceRmiImpl initialisé");
    }

    @Override
    public Long createInvoice(Long orderId, Double amount, String status) throws RemoteException {
        try {
            log.debug("RMI: Création d'une facture pour la commande: {}, montant: {}, statut: {}",
                    orderId, amount, status);
            // TODO: Implémenter la logique avec le repository
            return 1L;
        } catch (Exception e) {
            log.error("Erreur lors de la création de la facture", e);
            throw new RemoteException("Erreur lors de la création de la facture", e);
        }
    }

    @Override
    public String getInvoiceStatus(Long invoiceId) throws RemoteException {
        try {
            log.debug("RMI: Récupération du statut de la facture: {}", invoiceId);
            // TODO: Implémenter la logique
            return "DRAFT";
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut de la facture", e);
            throw new RemoteException("Erreur lors de la récupération du statut de la facture", e);
        }
    }

    @Override
    public Boolean updateInvoiceStatus(Long invoiceId, String status) throws RemoteException {
        try {
            log.debug("RMI: Mise à jour du statut de la facture: {} vers: {}", invoiceId, status);
            // TODO: Implémenter la logique
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du statut de la facture", e);
            throw new RemoteException("Erreur lors de la mise à jour du statut de la facture", e);
        }
    }

    @Override
    public Double getInvoiceAmount(Long invoiceId) throws RemoteException {
        try {
            log.debug("RMI: Récupération du montant de la facture: {}", invoiceId);
            // TODO: Implémenter la logique
            return 0.0;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du montant de la facture", e);
            throw new RemoteException("Erreur lors de la récupération du montant de la facture", e);
        }
    }

    @Override
    public Long createPayment(Long invoiceId, Double amount, String paymentMethod) throws RemoteException {
        try {
            log.debug("RMI: Création d'un paiement pour la facture: {}, montant: {}, méthode: {}",
                    invoiceId, amount, paymentMethod);
            // TODO: Implémenter la logique
            return 1L;
        } catch (Exception e) {
            log.error("Erreur lors de la création du paiement", e);
            throw new RemoteException("Erreur lors de la création du paiement", e);
        }
    }

    @Override
    public Double getTotalOutstandingBalance() throws RemoteException {
        try {
            log.debug("RMI: Récupération du solde total des factures impayées");
            // TODO: Implémenter la logique
            return 0.0;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du solde total", e);
            throw new RemoteException("Erreur lors de la récupération du solde total", e);
        }
    }

}
