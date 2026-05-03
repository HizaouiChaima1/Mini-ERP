package com.erp.ventes.dto;

import com.erp.ventes.model.Commande.StatutCommande;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CommandeDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank private String client;

        @NotNull @Size(min = 1)
        private List<LigneRequest> lignes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LigneRequest {
        @NotBlank private String produitReference;

        @NotNull @Min(1)
        private Integer quantite;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String numero;
        private String client;
        private StatutCommande statut;
        private BigDecimal montantTotal;
        private List<LigneResponse> lignes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class LigneResponse {
        private Long id;
        private String produitReference;
        private String produitNom;
        private Integer quantite;
        private BigDecimal prixUnitaire;
        private BigDecimal sousTotal;
    }
}
