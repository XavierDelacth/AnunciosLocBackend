/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package AnunciosLocBackend.backend.controller;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.UserRepository;
import AnunciosLocBackend.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
/**
 *
 * @author hp
 */

@RestController
@RequestMapping("/api/users")
public class UserController
{
   @Autowired private UserService service;
   @Autowired private UserRepository repo;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(service.register(user));
    }
    record LoginRequest(String username, String password) {}

    
    public record UserDTO(Long id, String username, String passwordHash, String fcmToken, String sessionId, Map<String, String> profiles) {}

    private Map<String, String> profilesToMap(User user) {
        Map<String, String> perfis = new HashMap<>();
        if (user.getProfiles() != null) {
            user.getProfiles().forEach(p -> {
                String key = p.getProfileKey();
                String val = p.getProfileValue();
                if (key == null) return;
                if (perfis.containsKey(key)) {
                    String prev = perfis.get(key);
                    perfis.put(key, prev + "," + val);
                } else {
                    perfis.put(key, val);
                }
            });
        }
        return perfis;
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequest req) {
        User u = service.login(req.username(), req.password());
        Map<String, String> perfis = profilesToMap(u);
        return ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername(), u.getPasswordHash(), u.getFcmToken(), u.getSessionId(), perfis));
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId, HttpServletRequest request) {
        // Verifica que o utilizador autenticado (definido pelo JwtFilter) corresponda ao id do path
        Object attr = request.getAttribute("userId");
        if (attr == null) {
            return ResponseEntity.status(401).body("JWT ausente ou inválido");
        }
        Long authUserId = null;
        try {
            authUserId = (Long) attr;
        } catch (ClassCastException ex) {
            authUserId = Long.valueOf(attr.toString());
        }
        if (!authUserId.equals(userId)) {
            return ResponseEntity.status(403).body("Não autorizado a efetuar logout deste utilizador");
        }

        // extrai token do header Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        service.logout(userId, token);
        return ResponseEntity.ok("Logout realizado com sucesso");
    }

    // Endpoint alternativo para logout quando o cliente não conseguir enviar o header Authorization
    // Recebe um JSON com { "sessionId": "<token>" } e valida que o token corresponde ao armazenado
    // antes de invalidar e limpar o campo. Útil para clientes que perderam o header.
    @PostMapping("/logout-raw/{userId}")
    public ResponseEntity<String> logoutRaw(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        String token = body == null ? null : body.get("sessionId");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("sessionId ausente no corpo");
        }
        // Verifica que o token enviado corresponde ao que está salvo para esse user
        var userOpt = repo.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var user = userOpt.get();
        String stored = user.getSessionId();
        if (stored == null || !stored.equals(token)) {
            return ResponseEntity.status(403).body("Token não corresponde ao sessionId armazenado");
        }
        service.logout(userId, token);
        return ResponseEntity.ok("Logout realizado com sucesso (raw)");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> profile(@PathVariable Long userId) {
        User u = service.getProfile(userId);
        return ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername(), u.getPasswordHash(), u.getFcmToken(), u.getSessionId(), profilesToMap(u)));
    }
    
        @PostMapping("/{id}/perfil")
        public ResponseEntity<UserDTO> adicionarPerfil(
            @PathVariable Long id,
            @RequestParam String chave,
            @RequestParam String valor,
            HttpServletRequest request) {
        // Verifica que o utilizador autenticado (definido pelo JwtFilter) corresponda ao id do path
        Object attr = request.getAttribute("userId");
        if (attr == null) {
            return ResponseEntity.status(401).build();
        }
        Long authUserId = null;
        try {
            authUserId = (Long) attr;
        } catch (ClassCastException ex) {
            // às vezes é String (defensivo)
            authUserId = Long.valueOf(attr.toString());
        }
        if (!authUserId.equals(id)) {
            return ResponseEntity.status(403).build();
        }

        User u = service.adicionarPerfil(id, chave, valor);
        return ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername(), u.getPasswordHash(), u.getFcmToken(), u.getSessionId(), profilesToMap(u)));
    }

    @DeleteMapping("/{id}/perfil/{chave}")
    public ResponseEntity<UserDTO> removerPerfil(
            @PathVariable Long id,
            @PathVariable String chave) {
        User u = service.removerPerfil(id, chave);
        return ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername(), u.getPasswordHash(), u.getFcmToken(), u.getSessionId(), profilesToMap(u)));
    }

        @DeleteMapping("/{id}/perfil/{chave}/{valor}")
        public ResponseEntity<UserDTO> removerPerfilValor(
            @PathVariable Long id,
            @PathVariable String chave,
            @PathVariable String valor,
            HttpServletRequest request) {
        // Require authentication and ensure user matches
        Object attr = request.getAttribute("userId");
        if (attr == null) return ResponseEntity.status(401).build();
        Long authUserId;
        try { authUserId = (Long) attr; } catch (ClassCastException ex) { authUserId = Long.valueOf(attr.toString()); }
        if (!authUserId.equals(id)) return ResponseEntity.status(403).build();

        User u = service.removerPerfilValor(id, chave, valor);
        return ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername(), u.getPasswordHash(), u.getFcmToken(), u.getSessionId(), profilesToMap(u)));
    }
    
    @GetMapping
    public ResponseEntity<List<UserDTO>> listarTodos(HttpServletRequest request) {
        // Exige autenticação
        Object attr = request.getAttribute("userId");
        if (attr == null) return ResponseEntity.status(401).build();
        List<User> users = service.listarTodos();
        List<UserDTO> dtos = new ArrayList<>();
        for (User u : users) {
            dtos.add(new UserDTO(u.getId(), u.getUsername(), u.getPasswordHash(), u.getFcmToken(), u.getSessionId(), profilesToMap(u)));
        }
        return ResponseEntity.ok(dtos);
    }
    
    
    record AlterarSenhaRequest(String senhaAtual, String novaSenha) {}
    @PatchMapping("/{id}/alterar-senha")
    public ResponseEntity<User> alterarSenha(
            @PathVariable Long id,
            @RequestBody AlterarSenhaRequest req) {
        try {
            User user = service.alterarSenha(id, req.senhaAtual(), req.novaSenha());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/chaves-publicas")
    public ResponseEntity<Set<String>> chavesPublicas() {
        return ResponseEntity.ok(service.listarChavesPerfis());
    }
    
    @PutMapping("/{id}/fcm-token")
    public ResponseEntity<String> atualizarFcmToken(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        User user = service.getProfile(id);
        user.setFcmToken(token);
        repo.save(user);
        return ResponseEntity.ok("Token FCM salvo");
    }   

     @GetMapping("/{id}/perfil")
        public ResponseEntity<Map<String, String>> getPerfis(@PathVariable Long id) {
            User user = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Converte Set<UserProfile> para Map<String, String> (valores múltiplos são unidos por ",")
            Map<String, String> perfis = new HashMap<>();
            if (user.getProfiles() != null) {
                user.getProfiles().forEach(p -> {
                    String key = p.getProfileKey();
                    String val = p.getProfileValue();
                    if (key == null) return;
                    if (perfis.containsKey(key)) {
                        String prev = perfis.get(key);
                        perfis.put(key, prev + "," + val);
                    } else {
                        perfis.put(key, val);
                    }
                });
            }

            return ResponseEntity.ok(perfis);
        }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<String> resetPassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nova senha não pode estar vazia");
        }
        
        try {
            service.resetPassword(userId, newPassword);
            return ResponseEntity.ok("Senha alterada com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Utilizador não encontrado");
        }
    }


    record ChangeUsernameRequest(String newUsername) {}

    @PatchMapping("/{id}/change-username")
    public ResponseEntity<UserDTO> changeUsername(
            @PathVariable Long id,
            @RequestBody ChangeUsernameRequest req) {
      
        try {
            User u = service.changeUsername(id, req.newUsername());
            return ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername(), u.getPasswordHash(), 
                u.getFcmToken(), u.getSessionId(), profilesToMap(u)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

     
@PatchMapping("/{id}/fcm-token")
    public ResponseEntity<UserDTO> updateFcmToken(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        service.updateFcmToken(id, token);
        User u = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        return ResponseEntity.ok(new UserDTO(u.getId(), u.getUsername(), u.getPasswordHash(), 
                    u.getFcmToken(), u.getSessionId(), profilesToMap(u)));
    }


    
}
