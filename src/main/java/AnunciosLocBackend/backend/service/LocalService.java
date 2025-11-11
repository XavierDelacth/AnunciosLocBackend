/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.enums.TipoLocalizacao;
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

    /** F3 – Listar todos */
    public List<Local> listar() {
        return repo.findAll();
    }

    /** F3 – Criar */
    public Local criar(Local local) {
        if (repo.findByNome(local.getNome()).isPresent()) {
            throw new RuntimeException("Local já existe: " + local.getNome());
        }

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
}
