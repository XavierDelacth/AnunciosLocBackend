/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.model;
import AnunciosLocBackend.backend.enums.TipoLocalizacao;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

/**
 *
 * @author hp
 */

@Entity
@Table(name = "locais")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Local {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLocalizacao tipo;

    private Double latitude;
    private Double longitude;
    private Integer raioMetros;

    @ElementCollection
    @CollectionTable(name = "local_wifi_ids", joinColumns = @JoinColumn(name = "local_id"))
    @Column(name = "wifi_id")
    private List<String> wifiIds = new ArrayList<>();
}
