/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.enums.TipoLocalizacao;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.model.Local;
import AnunciosLocBackend.backend.repository.LocalRepository;
import AnunciosLocBackend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import java.util.List;
/**
 *
 * @author hp
 */

@Service
public class LocalService
{
    
    @Autowired private LocalRepository repo;
    @Autowired private UserRepository userRepository; 

    /** F3 – Listar todos */
    public List<Local> listar() {
        return repo.findAll();
    }

    /** F3 – Criar - AGORA COM USER */
    public Local criar(Local local, Long userId) {
        if (repo.findByNome(local.getNome()).isPresent()) {
            throw new RuntimeException("Local já existe: " + local.getNome());
        }

        // Buscar o utilizador
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado (id=" + userId + ")"));
        
        local.setUser(user);

        if (local.getTipo() == TipoLocalizacao.GPS) {
            validarGPS(local);
            local.setWifiIds(null);
        } else {
            validarWIFI(local);
            local.setLatitude(null);
            local.setLongitude(null);
            local.setRaioMetros(null);
        }
        return repo.save(local);
    }

    /** F3 – Remover */
    public void remover(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Local não encontrado (id=" + id + ")");
        }
        repo.deleteById(id);
    }

    /** Busca por bounding-box (centralizado) */
    public List<Local> buscarProximos(Double lat, Double lng, Double distanciaKm) {
        double dLat = distanciaKm / 111.0;
        double dLng = distanciaKm / (111.0 * Math.cos(Math.toRadians(lat)));
        return repo.findByLatitudeBetweenAndLongitudeBetween(
                lat - dLat, lat + dLat, lng - dLng, lng + dLng);
    }

    private void validarGPS(Local l) {
        if (l.getLatitude() == null || l.getLongitude() == null || l.getRaioMetros() == null || l.getRaioMetros() <= 0) {
            throw new RuntimeException("GPS: latitude, longitude e raio (>0) são obrigatórios");
        }
    }

    private void validarWIFI(Local l) {
        if (l.getWifiIds() == null || l.getWifiIds().isEmpty()) {
            throw new RuntimeException("WIFI: pelo menos um SSID é obrigatório");
        }
    }
    
    public List<Local> buscarPorTexto(String query) {
        TipoLocalizacao tipo = null;
        try {
            tipo = TipoLocalizacao.valueOf(query.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Ignora se não for enum
        }

        return repo.searchByText(query, tipo);
    }
    
    public Optional<Local> buscarPorId(Long id) {
        return repo.findById(id);
    }

    /** F3 – Atualizar local existente */
    public Local atualizar(Long id, Local localAtualizado) {
        Local existente = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Local não encontrado (id=" + id + ")"));

        // Não permite mudar o nome se já existir outro com o mesmo nome
        if (!existente.getNome().equals(localAtualizado.getNome())) {
            if (repo.findByNome(localAtualizado.getNome()).isPresent()) {
                throw new RuntimeException("Já existe um local com o nome: " + localAtualizado.getNome());
            }
        }

        // Atualiza campos
        existente.setNome(localAtualizado.getNome());
        existente.setTipo(localAtualizado.getTipo());

        if (localAtualizado.getTipo() == TipoLocalizacao.GPS) {
            validarGPS(localAtualizado);
            existente.setLatitude(localAtualizado.getLatitude());
            existente.setLongitude(localAtualizado.getLongitude());
            existente.setRaioMetros(localAtualizado.getRaioMetros());
            existente.setWifiIds(null);
        } else {
            validarWIFI(localAtualizado);
            existente.setWifiIds(localAtualizado.getWifiIds());
            existente.setLatitude(null);
            existente.setLongitude(null);
            existente.setRaioMetros(null);
        }

        return repo.save(existente);
    }

    // NOVO: Buscar locais por utilizador
    public List<Local> buscarPorUser(User user) {
        return repo.findByUser(user);
    }

    // NOVO: Buscar locais por ID do utilizador
    public List<Local> buscarPorUserId(Long userId) {
        return repo.findByUserId(userId);
    }
}
