/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.model.Local;
import AnunciosLocBackend.backend.repository.LocalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
/**
 *
 * @author hp
 */

@Service
public class LocalService
{
    
    @Autowired private LocalRepository repo;

    // F3: Criar
    public Local criar(Local local) {
        return repo.save(local);
    }

    // F3: Listar do usuário
    public List<Local> listarPorUsuario(Long userId) {
        return repo.findByUserId(userId);
    }

    // F3: Remover
    public void remover(Long id, Long userId) {
        Local local = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Local não encontrado"));
        if (!local.getUserId().equals(userId)) {
            throw new RuntimeException("Acesso negado");
        }
        repo.delete(local);
    }

    // F5: Buscar próximos (caixa delimitadora)
    public List<Local> buscarProximos(double lat, double lng, double raioKm) {
        double raioGraus = raioKm / 111.0;
        double minLat = lat - raioGraus;
        double maxLat = lat + raioGraus;
        double minLng = lng - raioGraus;
        double maxLng = lng + raioGraus;
        return repo.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng);
    }
}
