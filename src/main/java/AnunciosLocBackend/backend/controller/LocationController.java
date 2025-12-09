
package AnunciosLocBackend.backend.controller;
import AnunciosLocBackend.backend.service.LocationService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.Data;





@RestController
@RequestMapping("/api/locations")
public class LocationController {
    @Autowired private LocationService service;

    @PostMapping("/update")
    public ResponseEntity<String> update(@RequestBody LocationUpdateRequest req) {
        try {
            service.updateLocation(req.getUserId(), req.getLat(), req.getLng(), req.getWifiIds());
            return ResponseEntity.ok("Localização atualizada");
        } catch (Exception e) {
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