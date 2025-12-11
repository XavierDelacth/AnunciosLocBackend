/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;

import AnunciosLocBackend.backend.model.AnuncioGuardado;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author delacth
 */
public interface AnuncioGuardadoRepository extends JpaRepository<AnuncioGuardado, Long> {
     // Verificar se usuário já guardou o anúncio
    boolean existsByUsuarioIdAndAnuncioId(Long usuarioId, Long anuncioId);
    
    // Listar anúncios guardados por usuário
    List<AnuncioGuardado> findByUsuarioId(Long usuarioId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AnuncioGuardado ag WHERE ag.usuario.id = :usuarioId AND ag.anuncio.id = :anuncioId")
    void deleteByUsuarioIdAndAnuncioId(@Param("usuarioId") Long usuarioId, @Param("anuncioId") Long anuncioId);
    
    // Contar anúncios guardados por usuário
    long countByUsuarioId(Long usuarioId);

    
     // Método alternativo - buscar o registro para depois deletar
    List<AnuncioGuardado> findByUsuarioIdAndAnuncioId(Long usuarioId, Long anuncioId);
    
    // Buscar todos os registros guardados de um anúncio (para deletar antes de deletar o anúncio)
    @Query("SELECT ag FROM AnuncioGuardado ag WHERE ag.anuncio.id = :anuncioId")
    List<AnuncioGuardado> findByAnuncioId(@Param("anuncioId") Long anuncioId);
}
