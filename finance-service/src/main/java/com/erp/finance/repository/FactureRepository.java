package com.erp.finance.repository;

import com.erp.finance.model.Facture;
import com.erp.finance.model.Facture.StatutFacture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    Optional<Facture> findByNumero(String numero);

    Optional<Facture> findByCommandeNumero(String commandeNumero);

    List<Facture> findByStatut(StatutFacture statut);

    List<Facture> findByClient(String client);
}
