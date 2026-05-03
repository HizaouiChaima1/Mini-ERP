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

    /** Charge commandes et lignes en une requête (évite le N+1 sur {@link com.erp.ventes.model.Commande#getLignes}). */
    @Query("SELECT DISTINCT c FROM Commande c LEFT JOIN FETCH c.lignes ORDER BY c.createdAt DESC")
    List<Commande> findAllWithLignes();
}
