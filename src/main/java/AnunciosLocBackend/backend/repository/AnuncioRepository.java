/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;

import AnunciosLocBackend.backend.model.Anuncio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
/**
 *
 * @author hp
 */
public interface AnuncioRepository extends JpaRepository<Anuncio, Long>
{
   List<Anuncio> findByLocalId(Long localId);
   List<Anuncio> findByUsuarioId(Long usuarioId);

   
    
}
