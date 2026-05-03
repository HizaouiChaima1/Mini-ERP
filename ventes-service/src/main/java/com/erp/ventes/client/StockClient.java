package com.erp.ventes.client;

import lombok.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FeignClient(name = "stock-service", url = "${stock.service.url}")
public interface StockClient {

    @GetMapping("/produits/reference/{ref}")
    ProduitResponse getProduitByReference(@PathVariable String ref);

    @PostMapping("/produits/{id}/entree")
    ProduitResponse entreStock(@PathVariable Long id, @RequestBody MouvementRequest req);

    @PostMapping("/produits/{id}/sortie")
    ProduitResponse sortieStock(@PathVariable Long id, @RequestBody MouvementRequest req);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    class ProduitResponse {
        private Long id;
        private String reference;
        private String nom;
        private BigDecimal prixUnitaire;
        private Integer quantiteEnStock;
        private Integer seuilAlerte;
        private boolean enAlerte;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class MouvementRequest {
        private Integer quantite;
        private String motif;
    }
}
