/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;

import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.enums.TipoLocalizacao;
import AnunciosLocBackend.backend.model.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author hp
 */
public interface LocalRepository extends JpaRepository<Local, Long> {
    Optional<Local> findByNome(String nome);
    
    // Método existente para busca por texto
    @Query("SELECT l FROM Local l WHERE " +
           "LOWER(l.nome) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "l.tipo = :tipo")
    List<Local> searchByText(@Param("query") String query, @Param("tipo") TipoLocalizacao tipo);
    
    // Método existente para busca por coordenadas
    List<Local> findByLatitudeBetweenAndLongitudeBetween(Double minLat, Double maxLat, Double minLng, Double maxLng);
    
    // NOVO: Buscar locais por utilizador
    List<Local> findByUser(User user);
    
    // NOVO: Buscar locais por ID do utilizador
    List<Local> findByUserId(Long userId);
}
