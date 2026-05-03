package com.erp.stock.repository;

import com.erp.stock.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    Optional<Produit> findByReference(String reference);

    @Query("SELECT p FROM Produit p WHERE p.quantiteEnStock <= p.seuilAlerte")
    List<Produit> findProduitsEnAlerte();

    boolean existsByReference(String reference);
}
