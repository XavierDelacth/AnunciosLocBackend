/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.Perfil;
import AnunciosLocBackend.backend.service.PerfilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *
 * @author delacth
 */

@RestController
@RequestMapping("/api/perfis")
public class PerfilController {
    @Autowired private PerfilService service;

    /** CREATE - Criar novo perfil com chave e valores */
    @PostMapping
    public ResponseEntity<Perfil> criar(@RequestBody Map<String, Object> request) {
        try {
            String chave = (String) request.get("chave");
            @SuppressWarnings("unchecked")
            List<String> valores = (List<String>) request.get("valores");
            
            return ResponseEntity.ok(service.criar(chave, valores));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** CREATE - Criar perfil com objeto */
    @PostMapping("/object")
    public ResponseEntity<Perfil> criar(@RequestBody Perfil perfil) {
        try {
            return ResponseEntity.ok(service.criar(perfil));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** READ - Listar todos os perfis */
    @GetMapping
    public ResponseEntity<List<Perfil>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    /** READ - Buscar perfil por ID */
    @GetMapping("/{id}")
    public ResponseEntity<Perfil> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    /** READ - Buscar por chave exata */
    @GetMapping("/chave/{chave}")
    public ResponseEntity<Perfil> buscarPorChave(@PathVariable String chave) {
        try {
            return ResponseEntity.ok(service.buscarPorChave(chave));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    /** READ - Buscar por chave (contém) */
    @GetMapping("/chave/search/{chave}")
    public ResponseEntity<List<Perfil>> buscarPorChaveContendo(@PathVariable String chave) {
        return ResponseEntity.ok(service.buscarPorChaveContendo(chave));
    }

    /** READ - Buscar por valor (contém) */
    @GetMapping("/valor/{valor}")
    public ResponseEntity<List<Perfil>> buscarPorValor(@PathVariable String valor) {
        return ResponseEntity.ok(service.buscarPorValor(valor));
    }

    /** READ - Pesquisa geral por texto */
    @GetMapping("/search")
    public ResponseEntity<List<Perfil>> pesquisar(@RequestParam String q) {
        return ResponseEntity.ok(service.pesquisar(q));
    }

    /** READ - Listar todas as chaves */
    @GetMapping("/chaves")
    public ResponseEntity<List<String>> listarChaves() {
        return ResponseEntity.ok(service.listarChaves());
    }

    /** UPDATE - Atualizar perfil completo */
    @PutMapping("/{id}")
    public ResponseEntity<Perfil> atualizar(@PathVariable Long id, @RequestBody Perfil perfil) {
        try {
            return ResponseEntity.ok(service.atualizar(id, perfil));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** UPDATE - Adicionar valor a uma chave */
    @PatchMapping("/chave/{chave}/valor")
    public ResponseEntity<Perfil> adicionarValor(
            @PathVariable String chave,
            @RequestParam String valor) {
        try {
            return ResponseEntity.ok(service.adicionarValor(chave, valor));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** UPDATE - Adicionar múltiplos valores a uma chave */
    @PatchMapping("/chave/{chave}/valores")
    public ResponseEntity<Perfil> adicionarValores(
            @PathVariable String chave,
            @RequestBody List<String> valores) {
        try {
            return ResponseEntity.ok(service.adicionarValores(chave, valores));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** UPDATE - Remover valor de uma chave */
    @DeleteMapping("/chave/{chave}/valor/{valor}")
    public ResponseEntity<Perfil> removerValor(
            @PathVariable String chave,
            @PathVariable String valor) {
        try {
            return ResponseEntity.ok(service.removerValor(chave, valor));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** DELETE - Remover perfil por ID */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> remover(@PathVariable Long id) {
        try {
            service.remover(id);
            return ResponseEntity.ok("Perfil removido com sucesso");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** DELETE - Remover perfil por chave */
    @DeleteMapping("/chave/{chave}")
    public ResponseEntity<String> removerPorChave(@PathVariable String chave) {
        try {
            service.removerPorChave(chave);
            return ResponseEntity.ok("Perfil removido com sucesso: " + chave);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
