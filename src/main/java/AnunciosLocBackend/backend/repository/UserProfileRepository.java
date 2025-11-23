package AnunciosLocBackend.backend.repository;

import AnunciosLocBackend.backend.model.UserProfile;
import AnunciosLocBackend.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    List<UserProfile> findByUserId(Long userId);
    List<UserProfile> findByUser(User user);
    Optional<UserProfile> findByUserIdAndProfileKeyAndProfileValueNormalized(Long userId, String key, String valueNormalized);
}
