package com.erp.ventes.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_commande")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commande_id", nullable = false)
    @ToString.Exclude
    private Commande commande;

    @Column(nullable = false, length = 100)
    private String produitReference;

    @Column(nullable = false, length = 200)
    private String produitNom;

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal sousTotal;

    @PrePersist
    @PreUpdate
    public void calcul() {
        this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}
