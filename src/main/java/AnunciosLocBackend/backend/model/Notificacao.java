/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author delacth
 */

@Entity
@Table(name = "notificacoes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "titulo")
    private String titulo;

   
    @Column(name = "descricao")
    private String descricao;

    @Column(name = "anuncio_id")
    private Long anuncioId;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    
     public Notificacao(Long userId, Long anuncioId, String titulo, String descricao) {
        this.userId = userId;
        this.anuncioId = anuncioId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataEnvio = LocalDateTime.now(); // AQUI!
    }

}
