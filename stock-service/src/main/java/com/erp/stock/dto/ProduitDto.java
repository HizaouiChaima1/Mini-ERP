package com.erp.stock.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProduitDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank
        private String reference;

        @NotBlank
        private String nom;

        private String description;

        @NotNull
        @DecimalMin("0.0")
        private BigDecimal prixUnitaire;

        @NotNull
        @Min(0)
        private Integer quantiteEnStock;

        @Min(0)
        @Builder.Default
        private Integer seuilAlerte = 10;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String reference;
        private String nom;
        private String description;
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
    public static class MouvementRequest {
        @NotNull
        @Min(1)
        private Integer quantite;
        private String motif;
    }
}
