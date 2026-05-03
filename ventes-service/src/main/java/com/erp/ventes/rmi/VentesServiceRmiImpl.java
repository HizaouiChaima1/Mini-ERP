package com.erp.ventes.rmi;

import com.erp.rmi.VentesServiceRmi;
import com.erp.ventes.model.Commande;
import com.erp.ventes.model.Commande.StatutCommande;
import com.erp.ventes.repository.CommandeRepository;
import com.erp.ventes.service.VentesService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

@Slf4j
@Service
@ConditionalOnProperty(name = "rmi.enabled", havingValue = "true", matchIfMissing = true)
public class VentesServiceRmiImpl extends UnicastRemoteObject implements VentesServiceRmi {

    private static final long serialVersionUID = 1L;

    private final CommandeRepository commandeRepository;
    private final VentesService ventesService;

    public VentesServiceRmiImpl(CommandeRepository commandeRepository, VentesService ventesService)
            throws RemoteException {
        super();
        this.commandeRepository = commandeRepository;
        this.ventesService = ventesService;
        log.info("VentesService RMI relié au métier Ventes");
    }

    private Commande getOrThrow(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande introuvable: " + id));
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
            String numero = "CMD-RMI-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
            BigDecimal montant = totalAmount == null ? BigDecimal.ZERO : BigDecimal.valueOf(totalAmount);
            Commande commande = Commande.builder()
                    .numero(numero)
                    .client(clientName)
                    .statut(st)
                    .montantTotal(montant)
                    .lignes(new ArrayList<>())
                    .build();
            Commande saved = commandeRepository.save(commande);
            return saved.getId();
        } catch (Exception e) {
            log.error("RMI Ventes createOrder", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getOrderStatus(Long orderId) throws RemoteException {
        try {
            return getOrThrow(orderId).getStatut().name();
        } catch (EntityNotFoundException e) {
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
                    throw new IllegalArgumentException("Statut vente inconnu ou non pilotable via RMI: " + status);
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
            return getOrThrow(orderId).getMontantTotal().doubleValue();
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Integer countOrdersByClient(String clientName) throws RemoteException {
        try {
            return (int) commandeRepository.countByClient(clientName);
        } catch (Exception e) {
            log.error("RMI Ventes countOrdersByClient", e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getCommandeNumero(Long orderId) throws RemoteException {
        try {
            return getOrThrow(orderId).getNumero();
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public String getOrderClient(Long orderId) throws RemoteException {
        try {
            return getOrThrow(orderId).getClient();
        } catch (EntityNotFoundException e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }
}
