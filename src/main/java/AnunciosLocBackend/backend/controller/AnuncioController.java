/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.service.AnuncioService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 *
 * @author hp
 */

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioController
{
    @Autowired private AnuncioService service;

    // F4: Registar
    @PostMapping
    public ResponseEntity<Anuncio> criar(@RequestBody Anuncio anuncio) {
        return ResponseEntity.ok(service.criar(anuncio));
    }

    // F4: Remover
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<String> remover(@PathVariable Long id, @PathVariable Long userId) {
        service.remover(id, userId);
        return ResponseEntity.ok("Anúncio removido");
    }

    // F5: Visualizar válidos em um local
    @GetMapping("/local/{localId}/user/{requesterId}")
    public ResponseEntity<List<Anuncio>> listarValidos(
            @PathVariable Long localId,
            @PathVariable Long requesterId) {
        return ResponseEntity.ok(service.listarValidos(localId, requesterId));
    }
    
}
