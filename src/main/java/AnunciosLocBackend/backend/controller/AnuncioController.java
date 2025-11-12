/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.controller;

import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.service.AnuncioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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
            @RequestParam(required = false) String perfilChave,
            @RequestParam(required = false) String perfilValor,
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
                anuncio.getRestricoes().put(perfilChave, perfilValor);
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
    
}
