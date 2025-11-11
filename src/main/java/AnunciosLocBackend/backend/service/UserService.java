/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;
import org.springframework.stereotype.Service;
/**
 *
 * @author hp
 */

@Service
public class UserService
{
   @Autowired private UserRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User register(User user) {
        if (repo.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username já existe");
        }
        user.setPasswordHash(encoder.encode(user.getPasswordHash()));
        return repo.save(user);
    }

    public User login(String username, String password) {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Senha incorreta");
        }
        user.setSessionId(UUID.randomUUID().toString());
        return repo.save(user);
    }

    public void logout(Long userId) {
        User user = repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setSessionId(null);
        repo.save(user);
    }

    public User getProfile(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
