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
    
}
