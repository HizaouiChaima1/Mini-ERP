package com.erp.finance.service;

import com.erp.finance.dto.FinanceDto;
import com.erp.finance.model.Facture;
import com.erp.finance.model.Facture.StatutFacture;
import com.erp.finance.model.Paiement;
import com.erp.finance.repository.FRepository;
import com.erp.finance.repository.PaiementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanceService {

    private final FRepository factureRepo;
    private final PaiementRepository paiementRepo;

    @Transactional(readOnly = true)
    public List<FinanceDto.FactureResponse> findAllFactures() {
        return factureRepo.findAll().stream().map(this::toFactureResponse).toList();
    }

    @Transactional(readOnly = true)
    public FinanceDto.FactureResponse findFactureById(Long id) {
        return toFactureResponse(getFactureOrThrow(id));
    }

    @Transactional(readOnly = true)
    public FinanceDto.DashboardResponse getDashboard() {
        List<Facture> all = factureRepo.findAll();
        BigDecimal ca = all.stream()
                .filter(f -> f.getStatut() == StatutFacture.PAYEE)
                .map(Facture::getMontantHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal caTTC = all.stream()
                .filter(f -> f.getStatut() == StatutFacture.PAYEE)
                .map(Facture::getMontantTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinanceDto.DashboardResponse.builder()
                .totalFactures(all.size())
                .facturesNonPayees(all.stream().filter(f -> f.getStatut() == StatutFacture.NON_PAYEE).count())
                .facturesPayees(all.stream().filter(f -> f.getStatut() == StatutFacture.PAYEE).count())
                .facturesEnRetard(all.stream().filter(f -> f.getStatut() == StatutFacture.EN_RETARD).count())
                .chiffreAffairesHT(ca)
                .chiffreAffairesTTC(caTTC)
                .build();
    }

    public FinanceDto.PaiementResponse enregistrerPaiement(Long factureId, FinanceDto.PaiementRequest req) {
        Facture facture = getFactureOrThrow(factureId);
        if (facture.getStatut() == StatutFacture.PAYEE) {
            throw new IllegalStateException("Facture déjà payée");
        }

        Paiement paiement = Paiement.builder()
                .facture(facture)
                .montant(req.getMontant())
                .mode(req.getMode())
                .reference(req.getReference())
                .build();
        @SuppressWarnings("null")
        Paiement saved = paiementRepo.save(paiement);
        paiement = saved;

        facture.setStatut(StatutFacture.PAYEE);
        facture.setDatePaiement(LocalDate.now());
        factureRepo.save(facture);

        return FinanceDto.PaiementResponse.builder()
                .id(paiement.getId())
                .factureId(factureId)
                .montant(paiement.getMontant())
                .mode(paiement.getMode())
                .reference(paiement.getReference())
                .createdAt(paiement.getCreatedAt())
                .build();
    }

    public void marquerEnRetard() {
        List<Facture> enRetard = factureRepo.findByStatut(StatutFacture.NON_PAYEE)
                .stream()
                .filter(f -> f.getDateEcheance().isBefore(LocalDate.now()))
                .toList();
        enRetard.forEach(f -> f.setStatut(StatutFacture.EN_RETARD));
        factureRepo.saveAll(enRetard);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    private Facture getFactureOrThrow(Long id) {
        Long findId = id;
        return factureRepo.findById(findId)
                .orElseThrow(() -> new EntityNotFoundException("Facture introuvable: " + id));
    }

    private FinanceDto.FactureResponse toFactureResponse(Facture f) {
        return FinanceDto.FactureResponse.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .commandeNumero(f.getCommandeNumero())
                .client(f.getClient())
                .montantHT(f.getMontantHT())
                .tauxTVA(f.getTauxTVA())
                .montantTVA(f.getMontantTVA())
                .montantTTC(f.getMontantTTC())
                .statut(f.getStatut())
                .dateEcheance(f.getDateEcheance())
                .datePaiement(f.getDatePaiement())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
