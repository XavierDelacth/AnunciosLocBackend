/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.Local;
import AnunciosLocBackend.backend.service.LocalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 *
 * @author hp
 */

@RestController
@RequestMapping("/api/locais")
public class LocalController
{
    @Autowired private LocalService service;

    // F3: Criar
    @PostMapping
    public ResponseEntity<Local> criar(@RequestBody Local local) {
        return ResponseEntity.ok(service.criar(local));
    }

    // F3: Listar do usuário
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Local>> listarPorUsuario(@PathVariable Long userId) {
        return ResponseEntity.ok(service.listarPorUsuario(userId));
    }

    // F3: Remover
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<String> remover(@PathVariable Long id, @PathVariable Long userId) {
        service.remover(id, userId);
        return ResponseEntity.ok("Local removido");
    }

    // F5: Buscar próximos
    @GetMapping("/proximos")
    public ResponseEntity<List<Local>> buscarProximos(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1") double raioKm) {
        return ResponseEntity.ok(service.buscarProximos(lat, lng, raioKm));
    }
}
