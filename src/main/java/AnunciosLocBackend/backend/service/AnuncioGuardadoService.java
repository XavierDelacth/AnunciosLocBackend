/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.model.AnuncioGuardado;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.AnuncioGuardadoRepository;
import AnunciosLocBackend.backend.repository.AnuncioRepository;
import AnunciosLocBackend.backend.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author delacth
 */

@Service
public class AnuncioGuardadoService {
      @Autowired private AnuncioGuardadoRepository repo;
    @Autowired private UserRepository userRepo;
    @Autowired private AnuncioRepository anuncioRepo;

    public AnuncioGuardado guardarAnuncio(Long usuarioId, Long anuncioId) {
       // Verificar se já existe
        if (repo.existsByUsuarioIdAndAnuncioId(usuarioId, anuncioId)) {
            throw new RuntimeException("Anúncio já está guardado");
        }
        
        User usuario = userRepo.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Anuncio anuncio = anuncioRepo.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));
        
        return repo.save(new AnuncioGuardado(usuario, anuncio));
    }

    public List<Anuncio> listarAnunciosGuardados(Long usuarioId) {
        return repo.findByUsuarioId(usuarioId).stream()
                .map(AnuncioGuardado::getAnuncio) 
                .collect(Collectors.toList());
    }

    
    public void removerAnuncioGuardado(Long usuarioId, Long anuncioId) {
        if (!repo.existsByUsuarioIdAndAnuncioId(usuarioId, anuncioId)) {
            throw new RuntimeException("Anúncio não está guardado");
        }
        repo.deleteByUsuarioIdAndAnuncioId(usuarioId, anuncioId);
    }
    
      public void removerAnuncioGuardadoAlternativo(Long usuarioId, Long anuncioId) {
        List<AnuncioGuardado> guardados = repo.findByUsuarioIdAndAnuncioId(usuarioId, anuncioId);
        
        if (guardados.isEmpty()) {
            throw new RuntimeException("Anúncio não está guardado");
        }
        
        // Deleta cada registro encontrado
        for (AnuncioGuardado guardado : guardados) {
            repo.delete(guardado);
        }
    }

    public boolean isAnuncioGuardado(Long usuarioId, Long anuncioId) {
        return repo.existsByUsuarioIdAndAnuncioId(usuarioId, anuncioId);
    }
}
