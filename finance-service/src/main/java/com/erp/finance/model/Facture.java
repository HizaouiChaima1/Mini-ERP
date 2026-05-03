package com.erp.finance.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "factures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String numero;

    @Column(nullable = false, length = 50)
    private String commandeNumero;

    @Column(nullable = false, length = 200)
    private String client;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantHT;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tauxTVA = new BigDecimal("0.19"); // TVA Tunisie 19%

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTVA;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montantTTC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutFacture statut = StatutFacture.NON_PAYEE;

    @Column(nullable = false)
    private LocalDate dateEcheance;

    private LocalDate datePaiement;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        calculMontants();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void calculMontants() {
        this.montantTVA = montantHT.multiply(tauxTVA);
        this.montantTTC = montantHT.add(montantTVA);
    }

    public enum StatutFacture {
        NON_PAYEE, PAYEE, EN_RETARD, ANNULEE
    }
}
