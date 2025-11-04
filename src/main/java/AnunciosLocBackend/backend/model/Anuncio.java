/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.model;
import AnunciosLocBackend.backend.enums.PolicyType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hp
 */

@Entity
@Table(name = "anuncios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Anuncio
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String conteudo;

    @Column(name = "local_id")
    private Long localId;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private PolicyType policyType = PolicyType.WHITELIST;  

    @ElementCollection
    @CollectionTable(name = "anuncio_restricoes", joinColumns = @JoinColumn(name = "anuncio_id"))
    @MapKeyColumn(name = "restricao_key")
    @Column(name = "restricao_value")
    private Map<String, String> restricoes = new HashMap<>();

    private LocalDateTime inicioValidade;
    private LocalDateTime fimValidade;
    
}
