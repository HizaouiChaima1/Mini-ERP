package com.erp.stock.service;

import com.erp.stock.dto.ProduitDto;
import com.erp.stock.messaging.StockEventPublisher;
import com.erp.stock.model.Produit;
import com.erp.stock.repository.ProduitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StockService {

    private final ProduitRepository repo;
    private final StockEventPublisher publisher;

    @Transactional(readOnly = true)
    public List<ProduitDto.Response> findAll() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProduitDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProduitDto.Response findByReference(String reference) {
        return toResponse(repo.findByReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Produit non trouvé: " + reference)));
    }

    @Transactional(readOnly = true)
    public List<ProduitDto.Response> findProduitsEnAlerte() {
        return repo.findProduitsEnAlerte().stream().map(this::toResponse).toList();
    }

    public ProduitDto.Response create(ProduitDto.Request req) {
        if (repo.existsByReference(req.getReference())) {
            throw new IllegalArgumentException("Référence déjà existante: " + req.getReference());
        }
        Produit produit = Produit.builder()
                .reference(req.getReference())
                .nom(req.getNom())
                .description(req.getDescription())
                .prixUnitaire(req.getPrixUnitaire())
                .quantiteEnStock(req.getQuantiteEnStock())
                .seuilAlerte(req.getSeuilAlerte())
                .build();
        @SuppressWarnings("null")
        Produit saved = repo.save(produit);
        produit = saved;
        publisher.publishStockUpdated(produit);
        return toResponse(produit);
    }

    public ProduitDto.Response update(Long id, ProduitDto.Request req) {
        Produit produit = getOrThrow(id);
        produit.setNom(req.getNom());
        produit.setDescription(req.getDescription());
        produit.setPrixUnitaire(req.getPrixUnitaire());
        produit.setQuantiteEnStock(req.getQuantiteEnStock());
        produit.setSeuilAlerte(req.getSeuilAlerte());
        produit = repo.save(produit);
        publisher.publishStockUpdated(produit);
        checkAlerte(produit);
        return toResponse(produit);
    }

    public ProduitDto.Response entreeStock(Long id, int quantite) {
        Produit produit = getOrThrow(id);
        produit.setQuantiteEnStock(produit.getQuantiteEnStock() + quantite);
        produit = repo.save(produit);
        publisher.publishStockUpdated(produit);
        return toResponse(produit);
    }

    public ProduitDto.Response sortieStock(Long id, int quantite) {
        Produit produit = getOrThrow(id);
        if (produit.getQuantiteEnStock() < quantite) {
            throw new IllegalStateException("Stock insuffisant pour " + produit.getReference()
                    + " (disponible=" + produit.getQuantiteEnStock() + ", demandé=" + quantite + ")");
        }
        produit.setQuantiteEnStock(produit.getQuantiteEnStock() - quantite);
        produit = repo.save(produit);
        publisher.publishStockUpdated(produit);
        checkAlerte(produit);
        return toResponse(produit);
    }

    @SuppressWarnings("null")
    public void delete(Long id) {
        boolean exists = repo.existsById(id);
        if (!exists)
            throw new EntityNotFoundException("Produit introuvable: " + id);
        Long deleteId = id;
        repo.deleteById(deleteId);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    private Produit getOrThrow(Long id) {
        Long findId = id;
        return repo.findById(findId).orElseThrow(() -> new EntityNotFoundException("Produit introuvable: " + id));
    }

    private void checkAlerte(Produit p) {
        if (p.getQuantiteEnStock() <= p.getSeuilAlerte()) {
            publisher.publishStockAlert(p);
        }
    }

    private ProduitDto.Response toResponse(Produit p) {
        return ProduitDto.Response.builder()
                .id(p.getId())
                .reference(p.getReference())
                .nom(p.getNom())
                .description(p.getDescription())
                .prixUnitaire(p.getPrixUnitaire())
                .quantiteEnStock(p.getQuantiteEnStock())
                .seuilAlerte(p.getSeuilAlerte())
                .enAlerte(p.getQuantiteEnStock() <= p.getSeuilAlerte())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
