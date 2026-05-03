package com.erp.finance.repository;

import com.erp.finance.model.Facture;
import com.erp.finance.model.Facture.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    Optional<Facture> findByNumero(String numero);

    Optional<Facture> findByCommandeNumero(String commandeNumero);

    List<Facture> findByStatut(StatutFacture statut);

    long countByStatut(StatutFacture statut);

    List<Facture> findByClient(String client);

    List<Facture> findByStatutAndDateEcheanceBefore(StatutFacture statut, LocalDate date);

    @Query("SELECT COALESCE(SUM(f.montantHT), 0) FROM Facture f WHERE f.statut = :statut")
    BigDecimal sumMontantHtByStatut(@Param("statut") StatutFacture statut);

    @Query("SELECT COALESCE(SUM(f.montantTTC), 0) FROM Facture f WHERE f.statut = :statut")
    BigDecimal sumMontantTtcByStatut(@Param("statut") StatutFacture statut);

    @Query("SELECT COALESCE(SUM(f.montantTTC), 0) FROM Facture f WHERE f.statut IN :statuts")
    BigDecimal sumMontantTtcByStatutIn(@Param("statuts") Collection<StatutFacture> statuts);
}
