package com.erp.finance.controller;

import com.erp.finance.dto.FinanceDto;
import com.erp.finance.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/factures")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService service;

    @GetMapping
    public List<FinanceDto.FactureResponse> getAll() { return service.findAllFactures(); }

    @GetMapping("/{id}")
    public FinanceDto.FactureResponse getById(@PathVariable Long id) { return service.findFactureById(id); }

    @GetMapping("/dashboard")
    public FinanceDto.DashboardResponse dashboard() { return service.getDashboard(); }

    @PostMapping("/{id}/paiements")
    @ResponseStatus(HttpStatus.CREATED)
    public FinanceDto.PaiementResponse payer(@PathVariable Long id,
                                             @Valid @RequestBody FinanceDto.PaiementRequest req) {
        return service.enregistrerPaiement(id, req);
    }

    @PostMapping("/marquer-retard")
    public void marquerEnRetard() { service.marquerEnRetard(); }
}
