/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.service.LocationTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author delacth
 */
@RestController
@RequestMapping("/api/location")
public class LocationController {
    
    @Autowired private LocationTrackingService locationService;
    
    @PostMapping("/update")
    public ResponseEntity<String> atualizarLocalizacao(
            @RequestParam Long userId,
            @RequestParam Double lat,
            @RequestParam Double lng) {
        
        try {
            locationService.atualizarLocalizacaoUsuario(userId, lat, lng);
            return ResponseEntity.ok("Localização atualizada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao atualizar localização");
        }
    }
}
