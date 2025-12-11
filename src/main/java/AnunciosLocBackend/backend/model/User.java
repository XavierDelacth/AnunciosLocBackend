/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
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
    
    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    // Para login (F2)
    private String sessionId;
    
    // Para F6: Perfis do utilizador (um registo por par key/value)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserProfile> profiles = new HashSet<>();
    
@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserLocation location;

	
}
