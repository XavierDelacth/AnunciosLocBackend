/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.Notificacao;
import AnunciosLocBackend.backend.repository.NotificacaoRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author delacth
 */

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {
    @Autowired private NotificacaoRepository repo;

    // LISTAR
    @GetMapping
    public List<Notificacao> listar(@RequestParam Long userId) {
        return repo.findByUserIdOrderByDataEnvioDesc(userId);
    }

    // LIMPAR TODAS
    @DeleteMapping
    @Transactional  
    public ResponseEntity<String> limpar(@RequestParam Long userId) {
        repo.deleteByUserId(userId);
        return ResponseEntity.ok("Notificações limpas");
    }
}
