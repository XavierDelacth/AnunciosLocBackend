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
import AnunciosLocBackend.backend.service.LocalService;

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
import AnunciosLocBackend.backend.service.NotificationService;
import AnunciosLocBackend.backend.repository.NotificacaoRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author hp
 */

@Service
public class AnuncioService
{
    
    @Autowired private AnuncioRepository anuncioRepo;
    @Autowired private LocalRepository localRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private LocalService localService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private NotificationService notificationService;
    @Autowired private NotificacaoRepository notificacaoRepo;
    

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

    // 3. Simula usuário com o perfil passado
    User usuarioVirtual = new User();
    Map<String, String> perfis = new HashMap<>();
    perfis.put(chavePerfil, valorPerfil);
    usuarioVirtual.setProfiles(perfis);

    // 4. Usa aplicarPolicy (WHITELIST/BLACKLIST)
    return anuncios.stream()
        .filter(a -> a.getModoEntrega() == ModoEntrega.CENTRALIZADO)
        .filter(a -> !a.getDataInicio().isAfter(hoje) && !a.getDataFim().isBefore(hoje))
        .filter(a -> !agora.isBefore(a.getHoraInicio()) && !agora.isAfter(a.getHoraFim()))
        .filter(a -> aplicarPolicy(a, usuarioVirtual))
        .collect(Collectors.toList());
}
    
    // F5 – MODO CENTRALIZADO: Busca anúncios centralizados próximos
    public List<Anuncio> buscarAnunciosCentralizadosProximos(Long userId, Double lat, Double lng, Double distanciaKm) {
        User usuario = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        double dLat = distanciaKm / 111.0; // 1° = 111km
        double dLng = distanciaKm / (111.0 * Math.cos(Math.toRadians(lat)));

        List<Local> locaisProximos = localRepo.findByLatitudeBetweenAndLongitudeBetween(
            lat - dLat, lat + dLat,
            lng - dLng, lng + dLng
        );

        List<Anuncio> anuncios = new ArrayList<>();
        for (Local local : locaisProximos) {
            anuncios.addAll(anuncioRepo.findByLocalId(local.getId()));
        }
             
        for (Anuncio a : anuncios) {
            notificationService.enviarNotificacao(userId, a);
            
        }

        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        return anuncios.stream()
            .filter(a -> a.getModoEntrega() == ModoEntrega.CENTRALIZADO)
            .filter(a -> !a.getDataInicio().isAfter(hoje) && !a.getDataFim().isBefore(hoje))
            .filter(a -> !agora.isBefore(a.getHoraInicio()) && !agora.isAfter(a.getHoraFim()))
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
    
    
    public void processarEntradaNaZona(Long userId, Double lat, Double lng, Double distanciaKm) {
        User usuario = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 1. Buscar locais próximos
        List<Local> locaisProximos = localService.buscarProximos(lat, lng, distanciaKm);

        // 2. Para cada local → buscar anúncios ativos
        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        for (Local local : locaisProximos) {
            List<Anuncio> anuncios = anuncioRepo.findByLocalId(local.getId());

            for (Anuncio anuncio : anuncios) {
                // Filtrar: CENTRALIZADO + válido + perfil compatível
                if (anuncio.getModoEntrega() != ModoEntrega.CENTRALIZADO) continue;
                if (anuncio.getDataInicio().isAfter(hoje) || anuncio.getDataFim().isBefore(hoje)) continue;
                if (agora.isBefore(anuncio.getHoraInicio()) || agora.isAfter(anuncio.getHoraFim())) continue;
                if (!aplicarPolicy(anuncio, usuario)) continue;

                // Evitar duplicata
                if (notificacaoRepo.existsByUserIdAndAnuncioId(userId, anuncio.getId())) {
                    continue;
                }

                // ENVIAR NOTIFICAÇÃO
                notificationService.enviarNotificacao(userId, anuncio);
            }
        }
    }
    
    
    
        /** Obter anúncio por ID */
    public Anuncio obterPorId(Long id) {
        return anuncioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));
    }

    /** Atualizar anúncio */
    public Anuncio atualizarAnuncio(Long anuncioId, Long userId, Long localId, Anuncio anuncioAtualizado, MultipartFile imagem) throws IOException {
        Anuncio anuncioExistente = anuncioRepo.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));

        // Verifica se o usuário é o dono do anúncio
        if (!anuncioExistente.getUsuario().getId().equals(userId)) {
            throw new RuntimeException("Você só pode atualizar seus próprios anúncios");
        }

        // Atualiza os campos se fornecidos
        if (anuncioAtualizado.getTitulo() != null) {
            anuncioExistente.setTitulo(anuncioAtualizado.getTitulo());
        }
        if (anuncioAtualizado.getDescricao() != null) {
            anuncioExistente.setDescricao(anuncioAtualizado.getDescricao());
        }
        if (anuncioAtualizado.getDataInicio() != null) {
            anuncioExistente.setDataInicio(anuncioAtualizado.getDataInicio());
        }
        if (anuncioAtualizado.getDataFim() != null) {
            anuncioExistente.setDataFim(anuncioAtualizado.getDataFim());
        }
        if (anuncioAtualizado.getHoraInicio() != null) {
            anuncioExistente.setHoraInicio(anuncioAtualizado.getHoraInicio());
        }
        if (anuncioAtualizado.getHoraFim() != null) {
            anuncioExistente.setHoraFim(anuncioAtualizado.getHoraFim());
        }
        if (anuncioAtualizado.getPolicyType() != null) {
            anuncioExistente.setPolicyType(anuncioAtualizado.getPolicyType());
        }
        if (anuncioAtualizado.getModoEntrega() != null) {
            anuncioExistente.setModoEntrega(anuncioAtualizado.getModoEntrega());
        }
        if (anuncioAtualizado.getRestricoes() != null && !anuncioAtualizado.getRestricoes().isEmpty()) {
            anuncioExistente.setRestricoes(anuncioAtualizado.getRestricoes());
        }

        // Atualiza local se fornecido
        if (localId != null) {
            Local local = localRepo.findById(localId)
                    .orElseThrow(() -> new RuntimeException("Local não encontrado"));
            anuncioExistente.setLocal(local);
        }

        // Atualiza imagem se fornecida
        if (imagem != null && !imagem.isEmpty()) {
            String imagemUrl = salvarImagem(imagem);
            anuncioExistente.setImagemUrl(imagemUrl);
        }

        // Validações
        if (anuncioExistente.getDataInicio().isAfter(anuncioExistente.getDataFim())) {
            throw new RuntimeException("Data início deve ser antes da data fim");
        }

        return anuncioRepo.save(anuncioExistente);
    }
    
    /** Atualização parcial de anúncio */
    public Anuncio atualizacaoParcial(Long anuncioId, Long userId, Map<String, Object> updates) {
        Anuncio anuncioExistente = anuncioRepo.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));

        // Verifica se o usuário é o dono do anúncio
        if (!anuncioExistente.getUsuario().getId().equals(userId)) {
            throw new RuntimeException("Você só pode atualizar seus próprios anúncios");
        }

        // Atualiza os campos fornecidos no map
        if (updates.containsKey("titulo")) {
            anuncioExistente.setTitulo((String) updates.get("titulo"));
        }
        if (updates.containsKey("descricao")) {
            anuncioExistente.setDescricao((String) updates.get("descricao"));
        }
        if (updates.containsKey("dataInicio")) {
            String dataInicioStr = (String) updates.get("dataInicio");
            LocalDate dataInicio = LocalDate.parse(dataInicioStr);
            anuncioExistente.setDataInicio(dataInicio);
        }
        if (updates.containsKey("dataFim")) {
            String dataFimStr = (String) updates.get("dataFim");
            LocalDate dataFim = LocalDate.parse(dataFimStr);
            anuncioExistente.setDataFim(dataFim);
        }
        if (updates.containsKey("horaInicio")) {
            String horaInicioStr = (String) updates.get("horaInicio");
            LocalTime horaInicio = LocalTime.parse(horaInicioStr);
            anuncioExistente.setHoraInicio(horaInicio);
        }
        if (updates.containsKey("horaFim")) {
            String horaFimStr = (String) updates.get("horaFim");
            LocalTime horaFim = LocalTime.parse(horaFimStr);
            anuncioExistente.setHoraFim(horaFim);
        }
        if (updates.containsKey("policyType")) {
            String policyTypeStr = (String) updates.get("policyType");
            PolicyType policyType = PolicyType.valueOf(policyTypeStr);
            anuncioExistente.setPolicyType(policyType);
        }
        if (updates.containsKey("modoEntrega")) {
            String modoEntregaStr = (String) updates.get("modoEntrega");
            ModoEntrega modoEntrega = ModoEntrega.valueOf(modoEntregaStr);
            anuncioExistente.setModoEntrega(modoEntrega);
        }
        if (updates.containsKey("localId")) {
            Long localId = Long.valueOf(updates.get("localId").toString());
            Local local = localRepo.findById(localId)
                    .orElseThrow(() -> new RuntimeException("Local não encontrado"));
            anuncioExistente.setLocal(local);
        }
        if (updates.containsKey("restricoes")) {
            // Assume que restricoes é um Map<String, String>
            @SuppressWarnings("unchecked")
            Map<String, String> restricoes = (Map<String, String>) updates.get("restricoes");
            anuncioExistente.setRestricoes(restricoes);
        }

        // Validações
        if (anuncioExistente.getDataInicio().isAfter(anuncioExistente.getDataFim())) {
            throw new RuntimeException("Data início deve ser antes da data fim");
        }

        return anuncioRepo.save(anuncioExistente);
    }
    
    
    /** Remover anúncio por ID (sem verificação de usuário) */
    public void removerAnuncioPorId(Long anuncioId) {
        if (!anuncioRepo.existsById(anuncioId)) {
            throw new RuntimeException("Anúncio não encontrado");
        }
        anuncioRepo.deleteById(anuncioId);
    }

    /** Listar todos os anúncios */
    public List<Anuncio> listarTodos() {
        return anuncioRepo.findAll();
    }
}
