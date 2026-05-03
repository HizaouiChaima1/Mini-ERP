package com.erp.finance.rmi;

import com.erp.finance.dto.FinanceDto;
import com.erp.finance.model.Facture.StatutFacture;
import com.erp.finance.model.Paiement.ModePaiement;
import com.erp.finance.service.FinanceService;
import com.erp.rmi.FinanceServiceRmi;
import com.erp.rmi.VentesServiceRmi;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Locale;

@Slf4j
@Service
@ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
public class FinanceServiceRmiImpl extends UnicastRemoteObject implements FinanceServiceRmi {

    private static final long serialVersionUID = 1L;

    private final FinanceService financeService;
    private final VentesServiceRmi ventesServiceRmi;

    public FinanceServiceRmiImpl(FinanceService financeService, @Lazy VentesServiceRmi ventesServiceRmi)
            throws RemoteException {
        super();
        this.financeService = financeService;
        this.ventesServiceRmi = ventesServiceRmi;
        log.info("FinanceService RMI relié au métier + Ventes distant");
    }

    private StatutFacture parseStatutCreation(String raw) {
        if (raw == null || raw.isBlank()) {
            return StatutFacture.NON_PAYEE;
        }
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "PAID", "PAYEE", "PAYÉE", "PAIDFULLY" -> StatutFacture.PAYEE;
            case "CANCELLED", "ANNULEE", "ANNULÉE", "ANNULLEE" -> StatutFacture.ANNULEE;
            default -> StatutFacture.NON_PAYEE;
        };
    }

    private StatutFacture parseStatutMaj(String raw) throws RemoteException {
        if (raw == null || raw.isBlank()) {
            throw new RemoteException("Statut vide");
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return StatutFacture.valueOf(key);
        } catch (IllegalArgumentException ignored) {
        }
        return switch (key) {
            case "PAID", "PAYÉE", "PAIDFULLY", "COMPLETE" -> StatutFacture.PAYEE;
            case "UNPAID", "IMPAYEE", "UNPAIDINV" -> StatutFacture.NON_PAYEE;
            case "OVERDUE", "RETARD", "DELAYED" -> StatutFacture.EN_RETARD;
            case "CANCELLED" -> StatutFacture.ANNULEE;
            default -> throw new RemoteException("Statut facture inconnu: " + raw);
        };
    }

    private ModePaiement parseMode(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return ModePaiement.VIREMENT;
        }
        return switch (paymentMethod.trim().toUpperCase(Locale.ROOT)) {
            case "CHEQUE", "CHECK" -> ModePaiement.CHEQUE;
            case "CASH", "ESPECES", "ESPÈCES" -> ModePaiement.ESPECES;
            case "CB", "CARD", "CARTE", "CARTE_BANCAIRE" -> ModePaiement.CARTE;
            default -> ModePaiement.VIREMENT;
        };
    }

    @Override
    public Long createInvoice(Long orderId, Double amount, String status) throws RemoteException {
        try {
            String numero = ventesServiceRmi.getCommandeNumero(orderId);
            String client = ventesServiceRmi.getOrderClient(orderId);
            double montantSource = amount != null && amount > 0 ? amount : ventesServiceRmi.getOrderTotal(orderId);
            BigDecimal montantHt = BigDecimal.valueOf(montantSource);
            StatutFacture st = parseStatutCreation(status);
            return financeService.creerOuRecupererFactureLieeCommande(numero, client, montantHt, st);
        } catch (RemoteException e) {
            throw e;
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Finance createInvoice", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getInvoiceStatus(Long invoiceId) throws RemoteException {
        try {
            return financeService.findFactureById(invoiceId).getStatut().name();
        } catch (Exception e) {
            log.error("RMI Finance getInvoiceStatus", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean updateInvoiceStatus(Long invoiceId, String status) throws RemoteException {
        try {
            StatutFacture statut = parseStatutMaj(status);
            financeService.definirStatutFacture(invoiceId, statut);
            return true;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            log.error("RMI Finance updateInvoiceStatus", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Double getInvoiceAmount(Long invoiceId) throws RemoteException {
        try {
            return financeService.findFactureById(invoiceId).getMontantTTC().doubleValue();
        } catch (Exception e) {
            log.error("RMI Finance getInvoiceAmount", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Long createPayment(Long invoiceId, Double amount, String paymentMethod) throws RemoteException {
        try {
            FinanceDto.PaiementRequest req = FinanceDto.PaiementRequest.builder()
                    .montant(BigDecimal.valueOf(amount))
                    .mode(parseMode(paymentMethod))
                    .reference("RMI-" + System.currentTimeMillis())
                    .build();
            FinanceDto.PaiementResponse res = financeService.enregistrerPaiement(invoiceId, req);
            return res.getId();
        } catch (Exception e) {
            log.error("RMI Finance createPayment", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Double getTotalOutstandingBalance() throws RemoteException {
        try {
            return financeService.totalImpayesTtc().doubleValue();
        } catch (Exception e) {
            log.error("RMI Finance getTotalOutstandingBalance", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
