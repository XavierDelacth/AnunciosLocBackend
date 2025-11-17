/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.UserRepository;
import AnunciosLocBackend.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest req) {       
        return ResponseEntity.ok(service.login(req.username(), req.password()));
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId) {
        service.logout(userId);
        return ResponseEntity.ok("Logout realizado com sucesso");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> profile(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getProfile(userId));
    }
    
    @PostMapping("/{id}/perfil")
    public ResponseEntity<User> adicionarPerfil(
            @PathVariable Long id,
            @RequestParam String chave,
            @RequestParam String valor) {
        return ResponseEntity.ok(service.adicionarPerfil(id, chave, valor));
    }

    @DeleteMapping("/{id}/perfil/{chave}")
    public ResponseEntity<User> removerPerfil(
            @PathVariable Long id,
            @PathVariable String chave) {
        return ResponseEntity.ok(service.removerPerfil(id, chave));
    }
    
    @GetMapping
    public ResponseEntity<List<User>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
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
}
