/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;


import AnunciosLocBackend.backend.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


/**
 *
 * @author delacth
 */
public interface  PerfilRepository extends JpaRepository<Perfil, Long>{
    // Buscar por chave exata
    Optional<Perfil> findByChave(String chave);
    
    // Buscar por chave (contém)
    List<Perfil> findByChaveContainingIgnoreCase(String chave);
    
    // Buscar perfis que contenham um valor específico
    @Query("SELECT p FROM Perfil p JOIN p.valores v WHERE LOWER(v) LIKE LOWER(CONCAT('%', :valor, '%'))")
    List<Perfil> findByValorContaining(@Param("valor") String valor);
    
    // Pesquisa geral (chave ou valores)
    @Query("SELECT p FROM Perfil p WHERE LOWER(p.chave) LIKE LOWER(CONCAT('%', :query, '%')) OR EXISTS (SELECT v FROM p.valores v WHERE LOWER(v) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Perfil> searchByQuery(@Param("query") String query);
    
    // Verificar se um valor existe em uma chave específica
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Perfil p JOIN p.valores v WHERE p.chave = :chave AND v = :valor")
    boolean existsValorInChave(@Param("chave") String chave, @Param("valor") String valor);
}
