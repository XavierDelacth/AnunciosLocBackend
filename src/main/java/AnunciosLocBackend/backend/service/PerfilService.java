/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.model.Perfil;
import AnunciosLocBackend.backend.repository.PerfilRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author delacth
 */

@Service
@Transactional
public class PerfilService {
    @Autowired private PerfilRepository repo;

    /** CREATE - Criar novo perfil com chave e valores */
    public Perfil criar(String chave, List<String> valores) {
        // Verificar se já existe perfil com esta chave
        String chaveNorm = chave == null ? null : chave.trim();
        if (repo.findByChave(chaveNorm).isPresent()) {
            throw new RuntimeException("Já existe um perfil com a chave: " + chave);
        }
        
        Perfil perfil = new Perfil();
        perfil.setChave(chaveNorm);
        if (valores != null) {
            perfil.setValores(valores.stream().map(v -> v == null ? null : v.trim()).collect(Collectors.toList()));
        }
        
        try {
            return repo.save(perfil);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Erro de integridade ao criar perfil: " + ex.getMessage(), ex);
        }
    }

    /** CREATE - Criar perfil com objeto completo */
    public Perfil criar(Perfil perfil) {
        // Verificar se já existe perfil com esta chave
        String chaveNorm = perfil.getChave() == null ? null : perfil.getChave().trim();
        if (repo.findByChave(chaveNorm).isPresent()) {
            throw new RuntimeException("Já existe um perfil com a chave: " + perfil.getChave());
        }
        perfil.setChave(chaveNorm);
        if (perfil.getValores() != null) {
            perfil.setValores(perfil.getValores().stream().map(v -> v == null ? null : v.trim()).collect(Collectors.toList()));
        }
        try {
            return repo.save(perfil);
        } catch (DataIntegrityViolationException ex) {
            throw new RuntimeException("Erro de integridade ao criar perfil: " + ex.getMessage(), ex);
        }
    }

    /** READ - Listar todos os perfis */
    public List<Perfil> listarTodos() {
        return repo.findAll();
    }

    /** READ - Buscar perfil por ID */
    public Perfil buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado (id=" + id + ")"));
    }

    /** READ - Buscar por chave exata */
    public Perfil buscarPorChave(String chave) {
        return repo.findByChave(chave)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para chave: " + chave));
    }

    /** READ - Buscar por chave (contém) */
    public List<Perfil> buscarPorChaveContendo(String chave) {
        return repo.findByChaveContainingIgnoreCase(chave);
    }

    /** READ - Buscar por valor (contém) */
    public List<Perfil> buscarPorValor(String valor) {
        return repo.findByValorContaining(valor);
    }

    /** READ - Pesquisa geral por texto (chave ou valores) */
    public List<Perfil> pesquisar(String query) {
        return repo.searchByQuery(query);
    }

    /** READ - Listar todas as chaves */
    public List<String> listarChaves() {
        return repo.findAll().stream()
                .map(Perfil::getChave)
                .toList();
    }

    /** UPDATE - Atualizar perfil completo */
    public Perfil atualizar(Long id, Perfil perfilAtualizado) {
        return repo.findById(id)
                .map(perfilExistente -> {
                    // Verificar se a nova chave já existe (em outro perfil)
                    if (!perfilExistente.getChave().equals(perfilAtualizado.getChave())) {
                        Optional<Perfil> perfilConflito = repo.findByChave(perfilAtualizado.getChave());
                        if (perfilConflito.isPresent() && !perfilConflito.get().getId().equals(id)) {
                            throw new RuntimeException("Já existe outro perfil com a chave: " + perfilAtualizado.getChave());
                        }
                    }
                    
                    perfilExistente.setChave(perfilAtualizado.getChave());
                    perfilExistente.setValores(perfilAtualizado.getValores());
                    
                    return repo.save(perfilExistente);
                })
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado (id=" + id + ")"));
    }

    /** UPDATE - Adicionar valor a uma chave existente */
    public Perfil adicionarValor(String chave, String novoValor) {
        Perfil perfil = repo.findByChave(chave)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para chave: " + chave));
        String novo = novoValor == null ? null : novoValor.trim();
        boolean exists = perfil.getValores().stream().anyMatch(v -> v != null && v.equalsIgnoreCase(novo));
        if (exists) {
            throw new RuntimeException("Valor '" + novoValor + "' já existe na chave '" + chave + "'");
        }
        perfil.getValores().add(novo);
        return repo.save(perfil);
    }

    /** UPDATE - Adicionar múltiplos valores a uma chave existente */
    public Perfil adicionarValores(String chave, List<String> novosValores) {
        Perfil perfil = repo.findByChave(chave)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para chave: " + chave));
        for (String valor : novosValores) {
            String v = valor == null ? null : valor.trim();
            boolean exists = perfil.getValores().stream().anyMatch(existing -> existing != null && existing.equalsIgnoreCase(v));
            if (!exists) {
                perfil.getValores().add(v);
            }
        }

        return repo.save(perfil);
    }

    /** UPDATE - Remover valor de uma chave */
    public Perfil removerValor(String chave, String valor) {
        Perfil perfil = repo.findByChave(chave)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para chave: " + chave));
        String v = valor == null ? null : valor.trim();
        boolean removed = perfil.getValores().removeIf(existing -> existing != null && existing.equalsIgnoreCase(v));
        if (!removed) {
            throw new RuntimeException("Valor '" + valor + "' não encontrado na chave '" + chave + "'");
        }
        return repo.save(perfil);
    }

    /** DELETE - Remover perfil por ID */
    public void remover(Long id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Perfil não encontrado (id=" + id + ")");
        }
        repo.deleteById(id);
    }

    /** DELETE - Remover perfil por chave */
    public void removerPorChave(String chave) {
        Perfil perfil = repo.findByChave(chave)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado para chave: " + chave));
        repo.delete(perfil);
    }

}
