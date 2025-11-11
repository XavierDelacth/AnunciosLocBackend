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
        return ResponseEntity.ok("Logout OK");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> profile(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getProfile(userId));
    }
    
}
