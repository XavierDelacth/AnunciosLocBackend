/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.model;


import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author delacth
 */


@Entity
@Table(name = "perfis_catalogo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"chave"})  // Chave única por perfil
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Perfil {
       @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String chave;

    // Lista de valores para esta chave
    @ElementCollection
    @CollectionTable(name = "perfil_valores", joinColumns = @JoinColumn(name = "perfil_id"))
    @Column(name = "valor")
    private List<String> valores = new ArrayList<>();
}
