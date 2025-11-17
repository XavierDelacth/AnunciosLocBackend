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

import java.util.List;
/**
 *
 * @author hp
 */

@RestController
@RequestMapping("/api/locais")
public class LocalController
{
     @Autowired private LocalService service;
     
    @GetMapping
    public List<Local> listar() {
        return service.listar();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Local> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // ATUALIZADO: Agora recebe o ID do utilizador como parâmetro
    @PostMapping
    public ResponseEntity<Local> criar(@RequestBody Local local, @RequestParam Long userId) {
        try {
            Local criado = service.criar(local, userId);
            return ResponseEntity.ok(criado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Local> atualizar(@PathVariable Long id, @RequestBody Local localAtualizado) {
        try {
            Local atualizado = service.atualizar(id, localAtualizado);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        try {
            service.remover(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404
        }
    }

    // GET /api/locais/proximos?lat=-8.81&lng=13.23&dist=1
    @GetMapping("/proximos")
    public List<Local> proximos(@RequestParam Double lat,
                                @RequestParam Double lng,
                                @RequestParam(defaultValue = "1") Double dist) {
        return service.buscarProximos(lat, lng, dist);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Local>> search(@RequestParam String query) {
        return ResponseEntity.ok(service.buscarPorTexto(query));
    }

    // NOVO ENDPOINT: Buscar locais por utilizador
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Local>> buscarPorUtilizador(@PathVariable Long userId) {
        try {
            List<Local> locais = service.buscarPorUserId(userId);
            return ResponseEntity.ok(locais);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
