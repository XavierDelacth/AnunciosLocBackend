/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;
import AnunciosLocBackend.backend.model.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
/**
 *
 * @author hp
 */
public interface LocalRepository extends JpaRepository<Local, Long>
{
    List<Local> findByUserId(Long userId);

    List<Local> findByLatitudeBetweenAndLongitudeBetween(
        double minLat, double maxLat, double minLng, double maxLng
    );

    List<Local> findByNomeContainingIgnoreCase(String nome);
}
