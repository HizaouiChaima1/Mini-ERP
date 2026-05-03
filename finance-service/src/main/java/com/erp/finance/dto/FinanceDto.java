package com.erp.finance.dto;

import com.erp.finance.model.Facture.StatutFacture;
import com.erp.finance.model.Paiement.ModePaiement;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FinanceDto {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class FactureResponse {
        private Long id;
        private String numero;
        private String commandeNumero;
        private String client;
        private BigDecimal montantHT;
        private BigDecimal tauxTVA;
        private BigDecimal montantTVA;
        private BigDecimal montantTTC;
        private StatutFacture statut;
        private LocalDate dateEcheance;
        private LocalDate datePaiement;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaiementRequest {
        @NotNull @DecimalMin("0.01")
        private BigDecimal montant;

        @NotNull
        private ModePaiement mode;

        private String reference;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PaiementResponse {
        private Long id;
        private Long factureId;
        private BigDecimal montant;
        private ModePaiement mode;
        private String reference;
        private LocalDateTime createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DashboardResponse {
        private long totalFactures;
        private long facturesNonPayees;
        private long facturesPayees;
        private long facturesEnRetard;
        private BigDecimal chiffreAffairesHT;
        private BigDecimal chiffreAffairesTTC;
    }
}
