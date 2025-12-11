/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.service.AnuncioService;
import AnunciosLocBackend.backend.service.AnuncioGuardadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.MediaType;

/**
 *
 * @author hp
 */

@RestController
@RequestMapping("/api/anuncios")
public class AnuncioController
{
    @Autowired private AnuncioService service;
    @Autowired private AnuncioGuardadoService serviceGuardado;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Anuncio> criar(
            @RequestParam Long userId,
            @RequestParam Long localId,
            @RequestParam String titulo,
            @RequestParam String descricao,
            @RequestParam String dataInicio, // dd/MM/yyyy
            @RequestParam String dataFim,
            @RequestParam String horaInicio, // HH:mm
            @RequestParam String horaFim,
            @RequestParam String policyType,
            @RequestParam String modoEntrega,
            @RequestParam(required = false) List<String> perfilChave,  
            @RequestParam(required = false) List<String> perfilValor,
            @RequestPart("imagem") MultipartFile imagem
    ) {
        try {
            Anuncio anuncio = new Anuncio();
            anuncio.setTitulo(titulo);
            anuncio.setDescricao(descricao);
            anuncio.setDataInicio(LocalDate.parse(dataInicio, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            anuncio.setDataFim(LocalDate.parse(dataFim, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            anuncio.setHoraInicio(LocalTime.parse(horaInicio));
            anuncio.setHoraFim(LocalTime.parse(horaFim));
            anuncio.setPolicyType(AnunciosLocBackend.backend.enums.PolicyType.valueOf(policyType));
            anuncio.setModoEntrega(AnunciosLocBackend.backend.enums.ModoEntrega.valueOf(modoEntrega));
            
            if (perfilChave != null && perfilValor != null) {
            int size = Math.min(perfilChave.size(), perfilValor.size());
            for (int i = 0; i < size; i++) {
                String chave = perfilChave.get(i).trim();
                String valor = perfilValor.get(i).trim();
                if (!chave.isEmpty() && !valor.isEmpty()) {
                    anuncio.getRestricoes().put(chave, valor);
                }
            }
        }

            Anuncio salvo = service.criarAnuncio(anuncio, userId, localId, imagem);
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    // F5 – MODO CENTRALIZADO: Buscar anúncios próximos (ESSA É A JOIA DO PROJETO)
    @GetMapping("/centralizado/proximos")
    public ResponseEntity<List<Anuncio>> proximosCentralizado(
            @RequestParam Long userId,
            @RequestParam Double lat,
                @RequestParam Double lng,
            @RequestParam(defaultValue = "1.0") Double distanciaKm) {
        
        List<Anuncio> anuncios = service.buscarAnunciosCentralizadosProximos(userId, lat, lng, distanciaKm);
        return ResponseEntity.ok(anuncios);
    }

    // F4 – Meus anúncios
    @GetMapping("/meus")
    public ResponseEntity<List<Anuncio>> meusAnuncios(@RequestParam Long userId) {
        return ResponseEntity.ok(service.listarMeusAnuncios(userId));
    }

    // F4 – Remover anúncio próprio
    @DeleteMapping("/{id}")
    public ResponseEntity<String> remover(@PathVariable Long id, @RequestParam Long userId) {
        try {
            service.removerAnuncio(id, userId);
            return ResponseEntity.ok("Anúncio removido com sucesso");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/centralizado/broadcast")
    public ResponseEntity<List<Anuncio>> anunciosBroadcast(
                @RequestParam Double lat,
                @RequestParam Double lng,
                @RequestParam(defaultValue = "10.0") Double distanciaKm,
                @RequestParam String chavePerfil,
                @RequestParam String valorPerfil) {
        List<Anuncio> anuncios = service.buscarAnunciosCentralizadosBroadcast(lat, lng, distanciaKm, chavePerfil, valorPerfil);
        return ResponseEntity.ok(anuncios);
    }
    
    @PostMapping("/centralizado/checkin")
    public ResponseEntity<String> checkin(
            @RequestParam Long userId,
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "1.0") Double distanciaKm) {

        service.processarEntradaNaZona(userId, lat, lng, distanciaKm);
        return ResponseEntity.ok("Check-in processado");
    }
    
    
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Anuncio> atualizar(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam(required = false) Long localId,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) String horaInicio,
            @RequestParam(required = false) String horaFim,
            @RequestParam(required = false) String policyType,
            @RequestParam(required = false) String modoEntrega,
            @RequestParam(required = false) List<String> perfilChave,
            @RequestParam(required = false) List<String> perfilValor,
            @RequestPart(required = false) MultipartFile imagem) {
        try {
            System.out.println("=== INICIANDO ATUALIZAÇÃO ===");
            System.out.println("Anuncio ID: " + id);
            System.out.println("User ID: " + userId);
            System.out.println("Titulo: " + titulo);

            Anuncio anuncioAtualizado = new Anuncio();

            if (titulo != null) anuncioAtualizado.setTitulo(titulo);
            if (descricao != null) anuncioAtualizado.setDescricao(descricao);
            if (dataInicio != null) {
                anuncioAtualizado.setDataInicio(LocalDate.parse(dataInicio, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                System.out.println("Data Inicio: " + anuncioAtualizado.getDataInicio());
            }
            if (dataFim != null) {
                anuncioAtualizado.setDataFim(LocalDate.parse(dataFim, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                System.out.println("Data Fim: " + anuncioAtualizado.getDataFim());
            }
            if (horaInicio != null) anuncioAtualizado.setHoraInicio(LocalTime.parse(horaInicio));
            if (horaFim != null) anuncioAtualizado.setHoraFim(LocalTime.parse(horaFim));
            if (policyType != null) anuncioAtualizado.setPolicyType(AnunciosLocBackend.backend.enums.PolicyType.valueOf(policyType));
            if (modoEntrega != null) anuncioAtualizado.setModoEntrega(AnunciosLocBackend.backend.enums.ModoEntrega.valueOf(modoEntrega));

            if (perfilChave != null && perfilValor != null) {
                Map<String, String> restricoes = new HashMap<>();
                int size = Math.min(perfilChave.size(), perfilValor.size());
                for (int i = 0; i < size; i++) {
                    String chave = perfilChave.get(i).trim();
                    String valor = perfilValor.get(i).trim();
                    if (!chave.isEmpty() && !valor.isEmpty()) {
                        restricoes.put(chave, valor);
                    }
                }
                anuncioAtualizado.setRestricoes(restricoes);
                System.out.println("Restrições: " + restricoes);
            }

            Anuncio salvo = service.atualizarAnuncio(id, userId, localId, anuncioAtualizado, imagem);
            System.out.println("=== ATUALIZAÇÃO CONCLUÍDA ===");
            return ResponseEntity.ok(salvo);
        } catch (Exception e) {
            System.err.println("=== ERRO NA ATUALIZAÇÃO ===");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    
    // PATCH - Atualização parcial com JSON
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Anuncio> atualizacaoParcial(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            System.out.println("=== INICIANDO ATUALIZAÇÃO PARCIAL ===");
            System.out.println("Anuncio ID: " + id);
            System.out.println("Updates: " + updates);

            // Extrai o userId (obrigatório) para verificar permissão
            Long userId = null;
            if (updates.containsKey("userId")) {
                userId = Long.valueOf(updates.get("userId").toString());
            } else {
                return ResponseEntity.badRequest().body(null);
            }

            // Chama o serviço para atualização parcial
            Anuncio anuncioAtualizado = service.atualizacaoParcial(id, userId, updates);
            return ResponseEntity.ok(anuncioAtualizado);
        } catch (Exception e) {
            System.err.println("=== ERRO NA ATUALIZAÇÃO PARCIAL ===");
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    
    // DELETE - Remover anúncio por ID (sem verificação de usuário - para admin)
    @DeleteMapping("/{id}/admin")
    public ResponseEntity<String> removerPorId(@PathVariable Long id) {
        try {
            service.removerAnuncioPorId(id);
            return ResponseEntity.ok("Anúncio removido com sucesso");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // GET - Listar todos os anúncios 
    @GetMapping
    public ResponseEntity<List<Anuncio>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }
    
    // GET - Obter anúncio por ID
    @GetMapping("/{id}")
    public ResponseEntity<Anuncio> obterPorId(@PathVariable Long id) {
        try {
            Anuncio anuncio = service.obterPorId(id);
            return ResponseEntity.ok(anuncio);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
}
