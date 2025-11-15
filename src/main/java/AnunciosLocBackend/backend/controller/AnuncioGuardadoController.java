/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.service.AnuncioGuardadoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author delacth
 */

@RestController
@RequestMapping("/api/guardados")
public class AnuncioGuardadoController {
     @Autowired private AnuncioGuardadoService service;

    @PostMapping("/usuario/{usuarioId}/anuncio/{anuncioId}")
    public ResponseEntity<String> guardarAnuncio(
            @PathVariable Long usuarioId,
            @PathVariable Long anuncioId) {
        try {
            service.guardarAnuncio(usuarioId, anuncioId);
            return ResponseEntity.ok("Anúncio guardado com sucesso");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Anuncio>> listarGuardados(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(service.listarAnunciosGuardados(usuarioId));
    }

    @DeleteMapping("/usuario/{usuarioId}/anuncio/{anuncioId}")
    public ResponseEntity<String> removerGuardado(
            @PathVariable Long usuarioId,
            @PathVariable Long anuncioId) {
        try {
            service.removerAnuncioGuardado(usuarioId, anuncioId);
            return ResponseEntity.ok("Anúncio removido dos guardados");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{usuarioId}/anuncio/{anuncioId}/verificar")
    public ResponseEntity<Boolean> verificarGuardado(
            @PathVariable Long usuarioId,
            @PathVariable Long anuncioId) {
        return ResponseEntity.ok(service.isAnuncioGuardado(usuarioId, anuncioId));
    }
}
