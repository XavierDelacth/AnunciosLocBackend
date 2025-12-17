package AnunciosLocBackend.backend.repository;

import AnunciosLocBackend.backend.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUserIdAndActiveTrue(Long userId);
    List<DeviceToken> findAllByActiveTrue();
    Optional<DeviceToken> findByToken(String token);
    void deleteByToken(String token);
}
