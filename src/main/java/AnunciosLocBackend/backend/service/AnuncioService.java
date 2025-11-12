/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;







import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.model.Local;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.AnuncioRepository;
import AnunciosLocBackend.backend.repository.LocalRepository;
import AnunciosLocBackend.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.List;
import AnunciosLocBackend.backend.enums.PolicyType;
import AnunciosLocBackend.backend.enums.ModoEntrega;
import AnunciosLocBackend.backend.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 *
 * @author hp
 */

@Service
public class AnuncioService
{
    @Autowired private LocalService localService;
    @Autowired private AnuncioRepository anuncioRepo;
    @Autowired private LocalRepository localRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;
    

    private static final String UPLOAD_DIR = "uploads/imagens/";

    public Anuncio criarAnuncio(Anuncio anuncio, Long userId, Long localId, MultipartFile imagem) throws IOException {
        User usuario = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Local local = localRepo.findById(localId).orElseThrow(() -> new RuntimeException("Local não encontrado"));

        // Upload da imagem
        String imagemUrl = salvarImagem(imagem);
        anuncio.setImagemUrl(imagemUrl);
        anuncio.setUsuario(usuario);
        anuncio.setLocal(local);

        // Validações
        if (anuncio.getDataInicio().isAfter(anuncio.getDataFim())) {
            throw new RuntimeException("Data início deve ser antes da data fim");
        }

        return anuncioRepo.save(anuncio);
    }

    private String salvarImagem(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + filename);
        Files.copy(file.getInputStream(), path);
        return "/uploads/imagens/" + filename;
    }
    
  

    // F5 – MODO CENTRALIZADO: Busca anúncios centralizados próximos
    public List<Anuncio> buscarAnunciosCentralizadosProximos(Long userId, Double lat, Double lng, Double distanciaKm) {
        User usuario = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<Local> locaisProximos = localRepo.findByLatitudeBetweenAndLongitudeBetween(lat - 0.01, lat + 0.01, lng - 0.01, lng + 0.01); // Bounding box

        List<Anuncio> anuncios = new ArrayList<>();
        for (Local local : locaisProximos) {
            anuncios.addAll(anuncioRepo.findByLocalId(local.getId()));
        }

        LocalDate hoje = LocalDate.now();
        LocalDateTime agora = LocalDateTime.now();

        return anuncios.stream()
            .filter(a -> a.getModoEntrega() == ModoEntrega.CENTRALIZADO)
            .filter(a -> !a.getDataInicio().isAfter(hoje) && !a.getDataFim().isBefore(hoje))
            .filter(a -> {
                LocalTime inicio = a.getHoraInicio();
                LocalTime fim = a.getHoraFim();
                LocalTime horaAtual = LocalTime.now();
                return !horaAtual.isBefore(inicio) && !horaAtual.isAfter(fim);
            })
            .filter(a -> aplicarPolicy(a, usuario))
            .collect(Collectors.toList());
    }

    /** Verifica WHITELIST / BLACKLIST conforme o PDF */
    private boolean aplicarPolicy(Anuncio a, User u) {
        if (a.getPolicyType() == PolicyType.WHITELIST) {
            return a.getRestricoes().entrySet().stream()
                    .allMatch(e -> u.getProfiles().getOrDefault(e.getKey(), "").equals(e.getValue()));
        } else if (a.getPolicyType() == PolicyType.BLACKLIST) {
            return a.getRestricoes().entrySet().stream()
                    .noneMatch(e -> u.getProfiles().getOrDefault(e.getKey(), "").equals(e.getValue()));
        }
        return true;
    }

    /** F4 – Listar anúncios do próprio usuário (gerenciar seus anúncios) */
    public List<Anuncio> listarMeusAnuncios(Long userId) {
        return anuncioRepo.findByUsuarioId(userId);
    }

    /** F4 – Remover anúncio próprio */
    public void removerAnuncio(Long anuncioId, Long userId) {
        Anuncio anuncio = anuncioRepo.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));

        if (!anuncio.getUsuario().getId().equals(userId)) {
            throw new RuntimeException("Você só pode remover seus próprios anúncios");
        }

        anuncioRepo.delete(anuncio);
    }
    
    // BROADCAST: Anúncios para todos com um perfil específico (ex: club=Benfica)
public List<Anuncio> buscarAnunciosCentralizadosBroadcast(
        Double lat, Double lng, Double distanciaKm, String chavePerfil, String valorPerfil) {

    // 1. Busca locais próximos
    List<Local> locaisProximos = localService.buscarProximos(lat, lng, distanciaKm);

    // 2. Busca anúncios CENTRALIZADOS desses locais
    List<Anuncio> anuncios = new ArrayList<>();
    for (Local local : locaisProximos) {
        anuncios.addAll(anuncioRepo.findByLocalId(local.getId()));
    }

    LocalDate hoje = LocalDate.now();
    LocalTime agora = LocalTime.now();

    // 3. Filtra por período, modo e política (BROADCAST)
    return anuncios.stream()
        .filter(a -> a.getModoEntrega() == ModoEntrega.CENTRALIZADO)
        .filter(a -> !a.getDataInicio().isAfter(hoje) && !a.getDataFim().isBefore(hoje))
        .filter(a -> !agora.isBefore(a.getHoraInicio()) && !agora.isAfter(a.getHoraFim()))
        .filter(a -> {
            if (a.getPolicyType() == PolicyType.WHITELIST) {
                return a.getRestricoes().containsKey(chavePerfil) 
                    && a.getRestricoes().get(chavePerfil).equals(valorPerfil);
            } else if (a.getPolicyType() == PolicyType.BLACKLIST) {
                return !a.getRestricoes().containsKey(chavePerfil) 
                    || !a.getRestricoes().get(chavePerfil).equals(valorPerfil);
            }
            return true;
        })
        .collect(Collectors.toList());
}
}
