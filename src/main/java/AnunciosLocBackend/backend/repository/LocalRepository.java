/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;
import AnunciosLocBackend.backend.model.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

/**
 *
 * @author hp
 */
public interface LocalRepository extends JpaRepository<Local, Long> {
    Optional<Local> findByNome(String nome);

    /** Bounding-box simples (ignora raio) */
    List<Local> findByLatitudeBetweenAndLongitudeBetween(
            Double minLat, Double maxLat, Double minLng, Double maxLng);
}
