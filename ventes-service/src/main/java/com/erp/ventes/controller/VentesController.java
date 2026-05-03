package com.erp.ventes.controller;

import com.erp.ventes.dto.CommandeDto;
import com.erp.ventes.service.VentesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/commandes")
@RequiredArgsConstructor
public class VentesController {

    private final VentesService service;

    @GetMapping
    public List<CommandeDto.Response> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CommandeDto.Response getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommandeDto.Response create(@Valid @RequestBody CommandeDto.Request req) {
        return service.creerCommande(req);
    }

    @PatchMapping("/{id}/confirmer")
    public CommandeDto.Response confirmer(@PathVariable Long id) {
        return service.confirmerCommande(id);
    }

    @PatchMapping("/{id}/livrer")
    public CommandeDto.Response livrer(@PathVariable Long id) {
        return service.livrerCommande(id);
    }

    @PatchMapping("/{id}/annuler")
    public CommandeDto.Response annuler(@PathVariable Long id) {
        return service.annulerCommande(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.supprimerCommande(id);
    }
}
