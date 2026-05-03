package com.erp.ventes.service;

import com.erp.ventes.client.StockClient;
import com.erp.ventes.dto.CommandeDto;
import com.erp.ventes.messaging.VentesEventPublisher;
import com.erp.ventes.model.Commande;
import com.erp.ventes.model.Commande.StatutCommande;
import com.erp.ventes.model.LigneCommande;
import com.erp.ventes.repository.CommandeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VentesService {

    private final CommandeRepository repo;
    private final StockClient stockClient;
    private final VentesEventPublisher publisher;

    @Transactional(readOnly = true)
    public List<CommandeDto.Response> findAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CommandeDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public CommandeDto.Response creerCommande(CommandeDto.Request req) {
        List<LigneCommande> lignes = new ArrayList<>();

        // Vérification stock via Feign (appel synchrone)
        for (CommandeDto.LigneRequest ligneReq : req.getLignes()) {
            StockClient.ProduitResponse produit = stockClient.getProduitByReference(ligneReq.getProduitReference());
            if (produit.getQuantiteEnStock() < ligneReq.getQuantite()) {
                throw new IllegalStateException("Stock insuffisant pour " + produit.getReference()
                        + " (dispo=" + produit.getQuantiteEnStock() + ")");
            }
            BigDecimal sousTotal = produit.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneReq.getQuantite()));
            lignes.add(LigneCommande.builder()
                    .produitReference(produit.getReference())
                    .produitNom(produit.getNom())
                    .quantite(ligneReq.getQuantite())
                    .prixUnitaire(produit.getPrixUnitaire())
                    .sousTotal(sousTotal)
                    .build());
        }

        String numero = "CMD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Commande commande = Commande.builder()
                .numero(numero)
                .client(req.getClient())
                .statut(StatutCommande.EN_ATTENTE)
                .lignes(new ArrayList<>())
                .build();

        @SuppressWarnings("null")
        Commande saved = repo.save(commande);
        commande = saved;
        @SuppressWarnings("null")
        Commande result = saved;
        commande = result;

        for (LigneCommande ligne : lignes) {
            ligne.setCommande(saved);
            ligne.calcul();
            saved.getLignes().add(ligne);
        }
        saved.recalculerTotal();
        saved = repo.save(saved);

        // Décrémenter stock via Feign
        for (CommandeDto.LigneRequest ligneReq : req.getLignes()) {
            StockClient.ProduitResponse produit = stockClient.getProduitByReference(ligneReq.getProduitReference());
            stockClient.sortieStock(produit.getId(),
                    new StockClient.MouvementRequest(ligneReq.getQuantite(), "Commande " + numero));
        }

        // Publier event async vers Finance
        publisher.publishCommandeCreated(saved);
        return toResponse(saved);
    }

    public CommandeDto.Response confirmerCommande(Long id) {
        Commande commande = getOrThrow(id);
        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new IllegalStateException("Commande déjà " + commande.getStatut());
        }
        commande.setStatut(StatutCommande.CONFIRMEE);
        return toResponse(repo.save(commande));
    }

    public CommandeDto.Response livrerCommande(Long id) {
        Commande commande = getOrThrow(id);
        commande.setStatut(StatutCommande.LIVREE);
        return toResponse(repo.save(commande));
    }

    public CommandeDto.Response annulerCommande(Long id) {
        Commande commande = getOrThrow(id);

        // Ne peut pas annuler une commande déjà livrée
        if (commande.getStatut() == StatutCommande.LIVREE) {
            throw new IllegalStateException("Impossible d'annuler une commande livrée");
        }

        // Restaurer le stock pour chaque ligne
        for (LigneCommande ligne : commande.getLignes()) {
            StockClient.ProduitResponse produit = stockClient.getProduitByReference(ligne.getProduitReference());
            stockClient.entreStock(produit.getId(),
                    new StockClient.MouvementRequest(ligne.getQuantite(),
                            "Annulation commande " + commande.getNumero()));
        }

        commande.setStatut(StatutCommande.ANNULEE);
        return toResponse(repo.save(commande));
    }

    public void supprimerCommande(Long id) {
        Commande commande = getOrThrow(id);

        // Ne peut pas supprimer les commandes livrées ou en attente
        if (commande.getStatut() == StatutCommande.LIVREE || commande.getStatut() == StatutCommande.EN_ATTENTE) {
            throw new IllegalStateException(
                    "Impossible de supprimer une commande " + commande.getStatut().toString().toLowerCase());
        }

        // Si la commande est confirmée, restaurer le stock
        if (commande.getStatut() == StatutCommande.CONFIRMEE) {
            for (LigneCommande ligne : commande.getLignes()) {
                StockClient.ProduitResponse produit = stockClient.getProduitByReference(ligne.getProduitReference());
                stockClient.entreStock(produit.getId(),
                        new StockClient.MouvementRequest(ligne.getQuantite(),
                                "Suppression commande confirmée " + commande.getNumero()));
            }
        }

        repo.deleteById(id);
    }

    // ── RMI : toute la logique transactionnelle / JPA reste dans ce service ──

    @Transactional(readOnly = true)
    public String rmiGetCommandeNumero(Long id) {
        return getOrThrow(id).getNumero();
    }

    @Transactional(readOnly = true)
    public String rmiGetOrderClient(Long id) {
        return getOrThrow(id).getClient();
    }

    @Transactional(readOnly = true)
    public String rmiGetOrderStatusName(Long id) {
        return getOrThrow(id).getStatut().name();
    }

    @Transactional(readOnly = true)
    public double rmiGetOrderTotalAsDouble(Long id) {
        return getOrThrow(id).getMontantTotal().doubleValue();
    }

    @Transactional(readOnly = true)
    public int rmiCountOrdersByClientExact(String clientName) {
        long n = repo.countOrdersForClient(clientName);
        return n > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) n;
    }

    @Transactional
    public Long rmiCreerCommandeSansLignes(String clientName, Double totalAmount, StatutCommande statut) {
        String numero = "CMD-RMI-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
        BigDecimal montant = totalAmount == null ? BigDecimal.ZERO : BigDecimal.valueOf(totalAmount);
        Commande commande = Commande.builder()
                .numero(numero)
                .client(clientName)
                .statut(statut)
                .montantTotal(montant)
                .lignes(new ArrayList<>())
                .build();
        Commande saved = repo.save(commande);
        return saved.getId();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    private Commande getOrThrow(Long id) {
        Long findId = id;
        return repo.findById(findId).orElseThrow(() -> new EntityNotFoundException("Commande introuvable: " + id));
    }

    private CommandeDto.Response toResponse(Commande c) {
        List<CommandeDto.LigneResponse> lignesDto = c.getLignes().stream()
                .map(l -> CommandeDto.LigneResponse.builder()
                        .id(l.getId())
                        .produitReference(l.getProduitReference())
                        .produitNom(l.getProduitNom())
                        .quantite(l.getQuantite())
                        .prixUnitaire(l.getPrixUnitaire())
                        .sousTotal(l.getSousTotal())
                        .build())
                .toList();
        return CommandeDto.Response.builder()
                .id(c.getId())
                .numero(c.getNumero())
                .client(c.getClient())
                .statut(c.getStatut())
                .montantTotal(c.getMontantTotal())
                .lignes(lignesDto)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
