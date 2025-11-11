/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.repository;

import AnunciosLocBackend.backend.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 *
 * @author hp
 */
public interface UserRepository extends JpaRepository<User, Long>
{
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findBySessionId(String sessionId);
}
