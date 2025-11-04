/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.AnuncioRepository;
import AnunciosLocBackend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 *
 * @author hp
 */

@Service
public class AnuncioService
{
    @Autowired private AnuncioRepository anuncioRepo;
    @Autowired private UserRepository userRepo;

    // F4: Registar
    public Anuncio criar(Anuncio anuncio) {
        return anuncioRepo.save(anuncio);
    }

    // F4: Remover
    public void remover(Long id, Long userId) {
        Anuncio a = anuncioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));
        if (!a.getUserId().equals(userId)) {
            throw new RuntimeException("Acesso negado");
        }
        anuncioRepo.delete(a);
    }

    // F5: Listar válidos em um local
    public List<Anuncio> listarValidos(Long localId, Long requesterId) {
        LocalDateTime now = LocalDateTime.now();
        List<Anuncio> anuncios = anuncioRepo.findValidosByLocalId(localId, now);

        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return anuncios.stream()
                .filter(a -> podeVer(a, requester.getProfiles()))
                .toList();
    }

    // F5: Verifica WHITELIST/BLACKLIST
    private boolean podeVer(Anuncio a, Map<String, String> perfil) {
        if (a.getRestricoes().isEmpty()) return true;

        return switch (a.getPolicyType()) {
            case WHITELIST -> a.getRestricoes().entrySet().stream()
                    .allMatch(e -> perfil.getOrDefault(e.getKey(), "").equals(e.getValue()));
            case BLACKLIST -> a.getRestricoes().entrySet().stream()
                    .noneMatch(e -> perfil.getOrDefault(e.getKey(), "").equals(e.getValue()));
        };
    }
}
