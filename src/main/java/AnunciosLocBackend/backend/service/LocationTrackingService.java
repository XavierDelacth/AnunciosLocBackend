/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author delacth
 */

@Service
public class LocationTrackingService {
    
     @Autowired private UserRepository userRepo;
    @Autowired private AnuncioService anuncioService;
    
    // Mapa para armazenar última localização dos usuários
    private final Map<Long, UserLocation> userLocations = new ConcurrentHashMap<>();
    
    @Scheduled(fixedRate = 30000) // Executa a cada 30 segundos
    public void verificarLocalizacoesUsuarios() {
        System.out.println(" Verificando localizações de usuários...");
        
        for (Map.Entry<Long, UserLocation> entry : userLocations.entrySet()) {
            Long userId = entry.getKey();
            UserLocation userLoc = entry.getValue();
            
            // Verifica se a localização ainda é recente (últimos 2 minutos)
            if (userLoc.isRecent()) {
                try {
                    anuncioService.processarEntradaNaZona(
                        userId, 
                        userLoc.getLatitude(), 
                        userLoc.getLongitude(), 
                        1.0 // distância padrão de 1km
                    );
                } catch (Exception e) {
                    System.err.println("Erro ao processar localização do usuário " + userId + ": " + e.getMessage());
                }
            } else {
                // Remove localizações antigas
                userLocations.remove(userId);
            }
        }
    }
    
    public void atualizarLocalizacaoUsuario(Long userId, Double lat, Double lng) {
        userLocations.put(userId, new UserLocation(lat, lng));
        System.out.println("Localização atualizada - User: " + userId + ", Lat: " + lat + ", Lng: " + lng);
    }
    
    // Classe interna para armazenar localização
    private static class UserLocation {

        
        private Double latitude;
        private Double longitude;
        private LocalDateTime timestamp;
        
        public UserLocation(Double lat, Double lng) {
            this.latitude = lat;
            this.longitude = lng;
            this.timestamp = LocalDateTime.now();
        }
        
        public boolean isRecent() {
            return Duration.between(timestamp, LocalDateTime.now()).toMinutes() < 2;
        }
        
       public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }
    }
}
