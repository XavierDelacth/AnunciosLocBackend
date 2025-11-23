/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.UserRepository;
import AnunciosLocBackend.backend.security.JwtBlacklist;
import AnunciosLocBackend.backend.security.JwtUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import AnunciosLocBackend.backend.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
/**
 *
 * @author hp
 */

@Service
public class UserService
{
   @Autowired private UserRepository repo;
   @Autowired private JwtUtil jwtUtil;
   @Autowired private JwtBlacklist jwtBlacklist;
   
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
        // GERA JWT
        String token = jwtUtil.generateToken(username, user.getId());
        user.setSessionId(token); // Reaproveita o campo
        return repo.save(user);
    }
    
    public void logout(Long userId, String tokenFromHeader) {
        User user = repo.findById(userId).orElseThrow();
        String token = tokenFromHeader;
        // fallback to stored sessionId if header not provided
        if (token == null || token.isEmpty()) {
            token = user.getSessionId();
        }
        if (token != null && !token.isEmpty()) {
            jwtBlacklist.add(token);
        }
        // clear stored session id to ensure logged out state
        user.setSessionId(null);
        repo.save(user);
    }
    
        public User adicionarPerfil(Long userId, String chave, String valor) {
        User user = repo.findById(userId).orElseThrow();
            String chaveNorm = chave == null ? null : chave.trim();
            String valorNorm = valor == null ? null : valor.trim();
            // evita duplicados
                System.out.println("[UserService] adicionarPerfil called for userId=" + userId + ", chave=" + chaveNorm + ", valor=" + valorNorm);
            boolean existe = user.getProfiles().stream()
                    .anyMatch(p -> p.getProfileKey() != null && p.getProfileKey().equals(chaveNorm)
                            && p.getProfileValueNormalized() != null && p.getProfileValueNormalized().equalsIgnoreCase(valorNorm));
            if (!existe) {
                UserProfile up = new UserProfile();
                up.setUser(user);
                up.setProfileKey(chaveNorm);
                up.setProfileValue(valorNorm);
                up.setProfileValueNormalized(valorNorm == null ? null : valorNorm.toLowerCase());
                user.getProfiles().add(up);
            }
                User saved = repo.save(user);
                System.out.println("[UserService] adicionarPerfil saved, total profiles=" + (saved.getProfiles() == null ? 0 : saved.getProfiles().size()));
                return saved;
    }

    public User removerPerfil(Long userId, String chave) {
        User user = repo.findById(userId).orElseThrow();
        String chaveNorm = chave == null ? null : chave.trim();
        user.getProfiles().removeIf(p -> p.getProfileKey() != null && p.getProfileKey().equals(chaveNorm));
        return repo.save(user);
    }

    public User removerPerfilValor(Long userId, String chave, String valor) {
        User user = repo.findById(userId).orElseThrow();
        String chaveNorm = chave == null ? null : chave.trim();
        String valorNorm = valor == null ? null : valor.trim();
        System.out.println("[UserService] removerPerfilValor called for userId=" + userId + ", chave=" + chaveNorm + ", valor=" + valorNorm);
        user.getProfiles().removeIf(p -> p.getProfileKey() != null && p.getProfileKey().equals(chaveNorm)
            && p.getProfileValue() != null && p.getProfileValue().equals(valorNorm));
        User saved = repo.save(user);
        System.out.println("[UserService] removerPerfilValor saved, total profiles=" + (saved.getProfiles() == null ? 0 : saved.getProfiles().size()));
        return saved;
    }


    public User getProfile(Long userId) {
        return repo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
    
    public List<User> listarTodos() {
    return repo.findAll().stream()
        .map(user -> {
            User u = new User();
            u.setId(user.getId());
            u.setUsername(user.getUsername());
            // copia set de UserProfile para não expor a referência original
            Set<UserProfile> copia = new HashSet<>();
            copia.addAll(user.getProfiles());
            u.setProfiles(copia);
            return u;
        })
        .collect(Collectors.toList());
    }
    
    public User alterarSenha(Long userId, String senhaAtual, String novaSenha) {
        User user = repo.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Valida senha atual
        if (!encoder.matches(senhaAtual, user.getPasswordHash())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        // Valida força da nova senha (opcional)
        if (novaSenha == null || novaSenha.length() < 3) {
            throw new RuntimeException("Nova senha deve ter pelo menos 3 caracteres");
        }

        // Atualiza
        user.setPasswordHash(encoder.encode(novaSenha));
        return repo.save(user);
    }
    public Set<String> listarChavesPerfis() {
    return repo.findAll().stream()
        .flatMap(u -> u.getProfiles().stream().map(p -> p.getProfileKey()))
        .filter(k -> k != null)
        .collect(Collectors.toSet());
    }
    
}
