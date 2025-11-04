/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.service.UserService;
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

    // F1: Registar
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(service.register(user));
    }

    // F2: Login
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        User user = service.login(request.username(), request.password());
        return ResponseEntity.ok(user);
    }

    // F2: Logout
    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId) {
        service.logout(userId);
        return ResponseEntity.ok("Logout com sucesso");
    }

    // F6: Perfil
    @GetMapping("/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getProfile(userId));
    }

    // DTO para login
    record LoginRequest(String username, String password) {}
    
}
