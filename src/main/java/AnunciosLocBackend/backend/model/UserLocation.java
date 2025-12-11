package AnunciosLocBackend.backend.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double latitude;
    private Double longitude;

    @ElementCollection
    @CollectionTable(name = "user_wifi_ids", joinColumns = @JoinColumn(name = "user_location_id"))
    @Column(name = "wifi_id")
    private List<String> wifiIds = new ArrayList<>();

    private LocalDateTime lastUpdated = LocalDateTime.now();

    // Opcional: Últimos anúncios notificados (para evitar spam)
    @ElementCollection
    private Set<Long> notifiedAnuncioIds = new HashSet<>();
}