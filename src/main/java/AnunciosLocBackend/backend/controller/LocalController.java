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

    @PostMapping
    public ResponseEntity<Local> criar(@RequestBody  Local local) {
        try {
            return ResponseEntity.ok(service.criar(local));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> remover(@PathVariable Long id) {
        try {
            service.remover(id);
            return ResponseEntity.ok("Removido");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // GET /api/locais/proximos?lat=-8.81&lng=13.23&dist=1
    @GetMapping("/proximos")
    public List<Local> proximos(@RequestParam Double lat,
                                @RequestParam Double lng,
                                @RequestParam(defaultValue = "1") Double dist) {
        return service.buscarProximos(lat, lng, dist);
    }
}
