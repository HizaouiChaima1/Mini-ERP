package com.erp.stock.controller;

import com.erp.stock.dto.ProduitDto;
import com.erp.stock.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produits")
@RequiredArgsConstructor
public class StockController {

    private final StockService service;

    @GetMapping
    public List<ProduitDto.Response> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProduitDto.Response getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/reference/{ref}")
    public ProduitDto.Response getByReference(@PathVariable String ref) {
        return service.findByReference(ref);
    }

    @GetMapping("/alertes")
    public List<ProduitDto.Response> getAlertes() {
        return service.findProduitsEnAlerte();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProduitDto.Response create(@Valid @RequestBody ProduitDto.Request req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public ProduitDto.Response update(@PathVariable Long id, @Valid @RequestBody ProduitDto.Request req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/entree")
    public ProduitDto.Response entree(@PathVariable Long id,
            @Valid @RequestBody ProduitDto.MouvementRequest req) {
        return service.entreeStock(id, req.getQuantite());
    }

    @PostMapping("/{id}/sortie")
    public ProduitDto.Response sortie(@PathVariable Long id,
            @Valid @RequestBody ProduitDto.MouvementRequest req) {
        return service.sortieStock(id, req.getQuantite());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
