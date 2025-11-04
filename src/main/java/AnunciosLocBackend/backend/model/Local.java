/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.model;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

/**
 *
 * @author hp
 */

@Entity
@Table(name = "locais")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Local
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    // GPS (latitude e longitude)
    private Double latitude;
    private Double longitude;

    // Raio de detecção em metros (ex: 100m)
    private Integer raio = 100;

    // IDs WiFi para detecção sem GPS
    @ElementCollection
    @CollectionTable(name = "local_wifi_ids", joinColumns = @JoinColumn(name = "local_id"))
    @Column(name = "wifi_id")
    private List<String> wifiIds = new ArrayList<>();

    // Dono do local
    @Column(name = "user_id")
    private Long userId;
}
