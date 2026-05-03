package com.erp.finance.service;

import com.erp.finance.dto.FinanceDto;
import com.erp.finance.model.Facture;
import com.erp.finance.model.Facture.StatutFacture;
import com.erp.finance.model.Paiement;
import com.erp.finance.repository.FactureRepository;
import com.erp.finance.repository.PaiementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanceService {

    private final FactureRepository factureRepo;
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
        BigDecimal ca = factureRepo.sumMontantHtByStatut(StatutFacture.PAYEE);
        BigDecimal caTTC = factureRepo.sumMontantTtcByStatut(StatutFacture.PAYEE);

        return FinanceDto.DashboardResponse.builder()
                .totalFactures(factureRepo.count())
                .facturesNonPayees(factureRepo.countByStatut(StatutFacture.NON_PAYEE))
                .facturesPayees(factureRepo.countByStatut(StatutFacture.PAYEE))
                .facturesEnRetard(factureRepo.countByStatut(StatutFacture.EN_RETARD))
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
        Paiement saved = paiementRepo.save(paiement);

        facture.setStatut(StatutFacture.PAYEE);
        facture.setDatePaiement(LocalDate.now());
        factureRepo.save(facture);

        return FinanceDto.PaiementResponse.builder()
                .id(saved.getId())
                .factureId(factureId)
                .montant(saved.getMontant())
                .mode(saved.getMode())
                .reference(saved.getReference())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public void marquerEnRetard() {
        List<Facture> enRetard = factureRepo.findByStatutAndDateEcheanceBefore(
                StatutFacture.NON_PAYEE, LocalDate.now());
        enRetard.forEach(f -> f.setStatut(StatutFacture.EN_RETARD));
        factureRepo.saveAll(enRetard);
    }

    /**
     * Crée une facture alignée sur le flux Rabbit, ou renvoie l'id existant (pour RMI).
     */
    public Long creerOuRecupererFactureLieeCommande(String numeroCommande, String client,
            BigDecimal montantHTCommercial, StatutFacture statutInitial) {
        return factureRepo.findByCommandeNumero(numeroCommande).map(Facture::getId).orElseGet(() -> {
            StatutFacture statut = statutInitial != null ? statutInitial : StatutFacture.NON_PAYEE;
            String numeroFac = "FAC-" + numeroCommande.replace("CMD-", "");
            Facture facture = Facture.builder()
                    .numero(numeroFac)
                    .commandeNumero(numeroCommande)
                    .client(client)
                    .montantHT(montantHTCommercial)
                    .tauxTVA(new BigDecimal("0.19"))
                    .dateEcheance(LocalDate.now().plusDays(30))
                    .statut(statut)
                    .build();
            Facture saved = factureRepo.save(facture);
            return saved.getId();
        });
    }

    public void definirStatutFacture(Long id, StatutFacture statut) {
        Facture f = getFactureOrThrow(id);
        f.setStatut(statut);
        factureRepo.save(f);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalImpayesTtc() {
        return factureRepo.sumMontantTtcByStatutIn(EnumSet.of(StatutFacture.NON_PAYEE, StatutFacture.EN_RETARD));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("null")
    private Facture getFactureOrThrow(Long id) {
        return factureRepo.findById(id)
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
