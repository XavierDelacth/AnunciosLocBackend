/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;
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

    /** Bounding-box simples (ignora raio) */
    List<Local> findByLatitudeBetweenAndLongitudeBetween(
            Double minLat, Double maxLat, Double minLng, Double maxLng);
    
   @Query("SELECT l FROM Local l WHERE LOWER(l.nome) LIKE LOWER(concat('%', :query, '%')) OR l.tipo = :tipo OR CAST(l.latitude AS string) LIKE :query OR CAST(l.longitude AS string) LIKE :query OR CAST(l.raioMetros AS string) LIKE :query OR EXISTS (SELECT w FROM l.wifiIds w WHERE LOWER(w) LIKE LOWER(concat('%', :query, '%'))) ")
   List<Local> searchByText(@Param("query") String query, @Param("tipo") TipoLocalizacao tipo);
}
