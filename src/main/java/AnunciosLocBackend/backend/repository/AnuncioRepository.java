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
    List<Anuncio> findByUserId(Long userId);

    // F5: Anúncios válidos agora (com janela de tempo)
    @Query("SELECT a FROM Anuncio a WHERE " +
           "a.inicioValidade <= :now AND a.fimValidade >= :now")
    List<Anuncio> findValidosNoMomento(@Param("now") LocalDateTime now);

    // F5: Anúncios sem validade
    List<Anuncio> findByInicioValidadeIsNullAndFimValidadeIsNull();

    // F5: Anúncios válidos em um local (EXISTE AGORA!)
    @Query("SELECT a FROM Anuncio a WHERE a.localId = :localId AND " +
           "(a.inicioValidade IS NULL OR a.inicioValidade <= :now) AND " +
           "(a.fimValidade IS NULL OR a.fimValidade >= :now)")
    List<Anuncio> findValidosByLocalId(
        @Param("localId") Long localId,
        @Param("now") LocalDateTime now);

   
    
}
