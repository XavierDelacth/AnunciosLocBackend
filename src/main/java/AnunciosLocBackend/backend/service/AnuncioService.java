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
import AnunciosLocBackend.backend.service.NotificationService;
import AnunciosLocBackend.backend.repository.NotificacaoRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import AnunciosLocBackend.backend.model.UserProfile;

/**
 *
 * @author hp
 */
@Service
public class AnuncioService {

    @Autowired
    private AnuncioRepository anuncioRepo;
    @Autowired
    private LocalRepository localRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private LocalService localService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificacaoRepository notificacaoRepo;

    private static final String UPLOAD_DIR = "uploads/imagens/";

    public Anuncio criarAnuncio(Anuncio anuncio, Long userId, Long localId, MultipartFile imagem) throws IOException {
        User usuario = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Local local = localRepo.findById(localId).orElseThrow(() -> new RuntimeException("Local não encontrado"));

        // Upload da imagem
        if (imagem != null && !imagem.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + imagem.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(imagem.getInputStream(), path);

            // Novo: Salva SÓ o caminho relativo – NÃO adicione IP ou http!
            anuncio.setImagemUrl("/uploads/imagens/" + fileName);  // Isso resolve tudo!

            // Log para depuração (opcional – adiciona se quiseres)
            System.out.println("Imagem salva em: " + anuncio.getImagemUrl());
            
        } else {
            anuncio.setImagemUrl(null);  // Ou um default se não houver imagem
        }
        anuncio.setUsuario(usuario);
        anuncio.setLocal(local);

        // Validações
        if (anuncio.getDataInicio().isAfter(anuncio.getDataFim())) {
            throw new RuntimeException("Data início deve ser antes da data fim");
        }

        return anuncioRepo.save(anuncio);
    }

    private String salvarImagem(MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
            System.out.println("Pasta criada: " + uploadDir.toAbsolutePath());
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path path = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), path);

        System.out.println("Imagem salva: " + path.toAbsolutePath());
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
        Set<UserProfile> perfis = new HashSet<>();
        UserProfile up = new UserProfile();
        up.setUser(usuarioVirtual);
        up.setProfileKey(chavePerfil);
        up.setProfileValue(valorPerfil);
        up.setProfileValueNormalized(valorPerfil == null ? null : valorPerfil.trim().toLowerCase());
        perfis.add(up);
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

    /**
     * Verifica WHITELIST / BLACKLIST conforme o PDF
     */
    private boolean aplicarPolicy(Anuncio a, User u) {
        System.out.println("   🔐 === VERIFICANDO POLÍTICA ===");
        System.out.println("   Tipo de política: " + a.getPolicyType());
        System.out.println("   Restrições do anúncio: " + a.getRestricoes());
        System.out.println("   Perfis do usuário: " + u.getProfiles());

        if (a.getPolicyType() == PolicyType.WHITELIST) {
            boolean resultado = a.getRestricoes().entrySet().stream()
                    .allMatch(e -> {
                        String valorUsuario = getProfileValue(u, e.getKey());
                        boolean match = valorUsuario.equals(e.getValue());
                        System.out.println("   🔍 WHITELIST: " + e.getKey() + " -> Anúncio: '" + e.getValue() + "', Usuário: '" + valorUsuario + "', Match: " + match);
                        return match;
                    });
            System.out.println("   ✅ Resultado WHITELIST: " + resultado);
            return resultado;
        } else if (a.getPolicyType() == PolicyType.BLACKLIST) {
            boolean resultado = a.getRestricoes().entrySet().stream()
                    .noneMatch(e -> {
                        String valorUsuario = getProfileValue(u, e.getKey());
                        boolean match = valorUsuario.equals(e.getValue());
                        System.out.println("   🔍 BLACKLIST: " + e.getKey() + " -> Anúncio: '" + e.getValue() + "', Usuário: '" + valorUsuario + "', Match: " + match);
                        return match;
                    });
            System.out.println("   ✅ Resultado BLACKLIST: " + resultado);
            return resultado;
        }
        System.out.println("   ✅ Política NENHUMA - sempre true");
        return true;
    }

    private String getProfileValue(User u, String key) {
        if (u == null || u.getProfiles() == null) {
            return "";
        }
        return u.getProfiles().stream()
                .filter(p -> p.getProfileKey() != null && p.getProfileKey().equals(key))
                .map(UserProfile::getProfileValue)
                .filter(v -> v != null)
                .findFirst()
                .orElse("");
    }

    /**
     * F4 – Listar anúncios do próprio usuário (gerenciar seus anúncios)
     */
    public List<Anuncio> listarMeusAnuncios(Long userId) {
        return anuncioRepo.findByUsuarioId(userId);
    }

    /**
     * F4 – Remover anúncio próprio
     */
    public void removerAnuncio(Long anuncioId, Long userId) {
        Anuncio anuncio = anuncioRepo.findById(anuncioId)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));

        if (!anuncio.getUsuario().getId().equals(userId)) {
            throw new RuntimeException("Você só pode remover seus próprios anúncios");
        }

        anuncioRepo.delete(anuncio);
    }

    public void processarEntradaNaZona(Long userId, Double lat, Double lng, Double distanciaKm) {
        System.out.println("🔍 === INICIANDO DIAGNÓSTICO DE CHECK-IN ===");
        System.out.println("📱 UserID: " + userId + ", Lat: " + lat + ", Lng: " + lng + ", Dist: " + distanciaKm);

        try {
            User usuario = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            System.out.println("✅ Usuário encontrado: " + usuario.getUsername());
            System.out.println("📊 Perfis do usuário: " + usuario.getProfiles());

            // 1. Buscar locais próximos
            List<Local> locaisProximos = localService.buscarProximos(lat, lng, distanciaKm);
            System.out.println("📍 Locais próximos encontrados: " + locaisProximos.size());

            for (Local local : locaisProximos) {
                System.out.println("   - Local: " + local.getNome() + " (ID: " + local.getId() + ")");
            }

            if (locaisProximos.isEmpty()) {
                System.out.println("❌ NENHUM local próximo encontrado!");
                return;
            }

            // 2. Para cada local → buscar anúncios ativos
            LocalDate hoje = LocalDate.now();
            LocalTime agora = LocalTime.now();

            System.out.println("📅 Data atual: " + hoje + ", 🕒 Hora atual: " + agora);

            boolean algumAnuncioProcessado = false;

            for (Local local : locaisProximos) {
                List<Anuncio> anuncios = anuncioRepo.findByLocalId(local.getId());
                System.out.println("📢 Anúncios no local '" + local.getNome() + "': " + anuncios.size());

                for (Anuncio anuncio : anuncios) {
                    algumAnuncioProcessado = true;
                    System.out.println("\n🔎 Analisando anúncio: " + anuncio.getTitulo());
                    System.out.println("   - ID: " + anuncio.getId());
                    System.out.println("   - Modo entrega: " + anuncio.getModoEntrega());
                    System.out.println("   - Data: " + anuncio.getDataInicio() + " a " + anuncio.getDataFim());
                    System.out.println("   - Horário: " + anuncio.getHoraInicio() + " às " + anuncio.getHoraFim());
                    System.out.println("   - Policy: " + anuncio.getPolicyType());
                    System.out.println("   - Restrições: " + anuncio.getRestricoes());

                    // Verificar cada filtro individualmente
                    boolean filtroModoEntrega = anuncio.getModoEntrega() == ModoEntrega.CENTRALIZADO;
                    boolean filtroData = !anuncio.getDataInicio().isAfter(hoje) && !anuncio.getDataFim().isBefore(hoje);
                    boolean filtroHorario = !agora.isBefore(anuncio.getHoraInicio()) && !agora.isAfter(anuncio.getHoraFim());
                    boolean filtroPolicy = aplicarPolicy(anuncio, usuario);
                    boolean filtroDuplicata = !notificacaoRepo.existsByUserIdAndAnuncioId(userId, anuncio.getId());

                    System.out.println("   📋 RESULTADO DOS FILTROS:");
                    System.out.println("     - Modo entrega (CENTRALIZADO): " + filtroModoEntrega);
                    System.out.println("     - Data válida: " + filtroData);
                    System.out.println("     - Horário válido: " + filtroHorario + " (agora=" + agora + ")");
                    System.out.println("     - Policy atendida: " + filtroPolicy);
                    System.out.println("     - Não é duplicata: " + filtroDuplicata);

                    // Aplicar todos os filtros
                    if (!filtroModoEntrega) {
                        System.out.println("   ❌ REPROVADO: Modo de entrega não é CENTRALIZADO");
                        continue;
                    }
                    if (!filtroData) {
                        System.out.println("   ❌ REPROVADO: Fora do período de datas");
                        continue;
                    }
                    if (!filtroHorario) {
                        System.out.println("   ❌ REPROVADO: Fora do horário permitido");
                        continue;
                    }
                    if (!filtroPolicy) {
                        System.out.println("   ❌ REPROVADO: Política não atendida");
                        continue;
                    }
                    if (!filtroDuplicata) {
                        System.out.println("   ❌ REPROVADO: Notificação já existe");
                        continue;
                    }

                    // TODOS OS FILTROS PASSARAM - ENVIAR NOTIFICAÇÃO
                    System.out.println("   ✅ TODOS OS FILTROS APROVADOS - ENVIANDO NOTIFICAÇÃO!");
                    notificationService.enviarNotificacao(userId, anuncio);
                    System.out.println("   📨 Notificação enviada para o usuário " + userId);
                }
            }

            if (!algumAnuncioProcessado) {
                System.out.println("⚠️  Nenhum anúncio foi processado nos locais encontrados");
            }

        } catch (Exception e) {
            System.err.println("💥 ERRO durante processamento: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("🔚 === FIM DO DIAGNÓSTICO ===");
    }

    /**
     * Obter anúncio por ID
     */
    public Anuncio obterPorId(Long id) {
        return anuncioRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Anúncio não encontrado"));
    }

    /**
     * Atualizar anúncio
     */
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

    /**
     * Atualização parcial de anúncio
     */
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

    /**
     * Remover anúncio por ID (sem verificação de usuário)
     */
    public void removerAnuncioPorId(Long anuncioId) {
        if (!anuncioRepo.existsById(anuncioId)) {
            throw new RuntimeException("Anúncio não encontrado");
        }
        anuncioRepo.deleteById(anuncioId);
    }

    /**
     * Listar todos os anúncios
     */
    public List<Anuncio> listarTodos() {
        return anuncioRepo.findAll();
    }
}
