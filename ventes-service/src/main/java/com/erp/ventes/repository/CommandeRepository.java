package com.erp.ventes.repository;

import com.erp.ventes.model.Commande;
import com.erp.ventes.model.Commande.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
    Optional<Commande> findByNumero(String numero);
    List<Commande> findByStatut(StatutCommande statut);
    List<Commande> findByClientContainingIgnoreCase(String client);

    /** JPQL explicite : évite les erreurs de dérivation Spring Data ({@code countByClient} ambigu avec certains scans). */
    @Query("SELECT COUNT(c) FROM Commande c WHERE c.client = :client")
    long countOrdersForClient(@Param("client") String client);
}
