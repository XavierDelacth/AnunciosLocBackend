
package AnunciosLocBackend.backend.controller;
import AnunciosLocBackend.backend.service.LocationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    @Autowired private LocationService service;

    @PostMapping("/update")
    public ResponseEntity<String> update(@RequestBody LocationUpdateRequest req, HttpServletRequest request) {
        System.out.println("[LocationController] === REQUISIÇÃO RECEBIDA ===");
        System.out.println("[LocationController] Path: " + request.getRequestURI());
        System.out.println("[LocationController] Method: " + request.getMethod());
        try {
            // Obtém o userId do JWT (definido pelo JwtFilter)
            Object attr = request.getAttribute("userId");
            if (attr == null) {
                System.out.println("[LocationController] ERRO: userId não encontrado no request - JWT não validado");
                return ResponseEntity.status(401).body("JWT ausente ou inválido");
            }
            Long authUserId = null;
            try {
                authUserId = (Long) attr;
            } catch (ClassCastException ex) {
                authUserId = Long.valueOf(attr.toString());
            }
            
            System.out.println("[LocationController] authUserId do JWT: " + authUserId);
            System.out.println("[LocationController] userId do request body: " + req.getUserId());
            
            // Validação: Se o request body tem userId, deve corresponder ao JWT
            // Mas sempre usa o userId do JWT (mais seguro - não confia no cliente)
            if (req.getUserId() != null && !authUserId.equals(req.getUserId())) {
                System.out.println("[LocationController] AVISO: userId do JWT (" + authUserId + ") diferente do request body (" + req.getUserId() + ") - usando JWT");
                // Não retorna erro, apenas usa o userId do JWT
            }
            
            // SEMPRE usa o userId do JWT (não confia no request body)
            System.out.println("[LocationController] Atualizando localização para userId (do JWT): " + authUserId);
            service.updateLocation(authUserId, req.getLat(), req.getLng(), req.getWifiIds());
            return ResponseEntity.ok("Localização atualizada");
        } catch (Exception e) {
            System.err.println("[LocationController] EXCEÇÃO: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

@Data
class LocationUpdateRequest {
    private Long userId;
    private Double lat;
    private Double lng;
    private List<String> wifiIds;
}