package com.erp.ventes.rmi;

import com.erp.rmi.VentesServiceRmi;
import com.erp.ventes.model.Commande.StatutCommande;
import com.erp.ventes.service.VentesService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Locale;

/**
 * Façade RMI : une seule dépendance vers {@link VentesService} (transactions, repository, Stock Feign).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
public class VentesServiceRmiImpl extends UnicastRemoteObject implements VentesServiceRmi {

    private static final long serialVersionUID = 1L;

    private final VentesService ventesService;

    public VentesServiceRmiImpl(@Lazy VentesService ventesService) throws RemoteException {
        super();
        this.ventesService = ventesService;
        log.info("VentesService RMI -> délégué au bean VentesService");
    }

    @Override
    public Long createOrder(String clientName, Double totalAmount, String status) throws RemoteException {
        try {
            StatutCommande st = StatutCommande.EN_ATTENTE;
            if (status != null && !status.isBlank()) {
                try {
                    st = StatutCommande.valueOf(status.trim().toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException ex) {
                    log.warn("Statut commande RMI inconnu '{}', défaut EN_ATTENTE", status);
                }
            }
            return ventesService.rmiCreerCommandeSansLignes(clientName, totalAmount, st);
        } catch (Exception e) {
            log.error("RMI Ventes createOrder", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getOrderStatus(Long orderId) throws RemoteException {
        try {
            return ventesService.rmiGetOrderStatusName(orderId);
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Ventes getOrderStatus", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Boolean updateOrderStatus(Long orderId, String status) throws RemoteException {
        try {
            if (status == null || status.isBlank()) {
                return false;
            }
            String s = status.trim().toUpperCase(Locale.ROOT);
            switch (s) {
                case "CONFIRMEE", "CONFIRMED" -> ventesService.confirmerCommande(orderId);
                case "LIVREE", "DELIVERED" -> ventesService.livrerCommande(orderId);
                case "ANNULEE", "CANCELLED" -> ventesService.annulerCommande(orderId);
                default ->
                        throw new IllegalArgumentException(
                                "Statut vente inconnu ou non pilotable via RMI: " + status);
            }
            return true;
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Ventes updateOrderStatus", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Double getOrderTotal(Long orderId) throws RemoteException {
        try {
            return ventesService.rmiGetOrderTotalAsDouble(orderId);
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Ventes getOrderTotal", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Integer countOrdersByClient(String clientName) throws RemoteException {
        try {
            return ventesService.rmiCountOrdersByClientExact(clientName);
        } catch (Exception e) {
            log.error("RMI Ventes countOrdersByClient", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getCommandeNumero(Long orderId) throws RemoteException {
        try {
            return ventesService.rmiGetCommandeNumero(orderId);
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Ventes getCommandeNumero", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getOrderClient(Long orderId) throws RemoteException {
        try {
            return ventesService.rmiGetOrderClient(orderId);
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("RMI Ventes getOrderClient", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
