package AnunciosLocBackend.backend.repository;

import AnunciosLocBackend.backend.model.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    Optional<UserLocation> findByUserId(Long userId);
}