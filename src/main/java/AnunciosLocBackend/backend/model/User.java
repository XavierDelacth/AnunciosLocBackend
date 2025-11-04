/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.model;
import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import lombok.*;
/**
 *
 * @author hp
 */

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    // Para login (F2)
    private String sessionId;

    // Para F6: Perfil key-value (ex: "club", "interesse")
    @ElementCollection
    @CollectionTable(name = "user_profiles", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "profile_key")
    @Column(name = "profile_value")
    private Map<String, String> profiles = new HashMap<>();
}
