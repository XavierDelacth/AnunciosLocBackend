
package AnunciosLocBackend.backend.service;
import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.model.Local;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.model.UserLocation;
import AnunciosLocBackend.backend.enums.ModoEntrega;
import AnunciosLocBackend.backend.enums.PolicyType;
import AnunciosLocBackend.backend.enums.TipoLocalizacao;
import AnunciosLocBackend.backend.repository.AnuncioRepository;
import AnunciosLocBackend.backend.repository.LocalRepository;
import AnunciosLocBackend.backend.repository.NotificacaoRepository;
import AnunciosLocBackend.backend.repository.UserLocationRepository;
import AnunciosLocBackend.backend.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import AnunciosLocBackend.backend.model.UserProfile;


@Service
public class LocationService {

    @Autowired private UserLocationRepository locationRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private AnuncioRepository anuncioRepo;
    @Autowired private NotificationService notificationService;
    @Autowired private LocalRepository localRepo; // Se precisar listar locais
    @Autowired private NotificacaoRepository notificacaoRepo; // Para verificar duplicação no banco

    public void updateLocation(Long userId, Double lat, Double lng, List<String> wifiIds) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User não encontrado"));
        // Log explícito conforme requisito: Localização recebida
        System.out.println("[LocationService] Localização recebida do user " + userId + ": " + lat + ", " + lng);

        UserLocation location = locationRepo.findByUserId(userId)
            .orElse(new UserLocation());
        location.setUser(user);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setWifiIds(wifiIds != null ? wifiIds : new ArrayList<>());
        location.setLastUpdated(LocalDateTime.now());
        locationRepo.save(location);

        // Verifica anúncios próximos e envia notifs
        checkAndNotifyAnuncios(user, location);
    }

    private void checkAndNotifyAnuncios(User user, UserLocation location) {
        LocalDateTime now = LocalDateTime.now();
        List<Anuncio> anuncios = anuncioRepo.findAll(); // Otimizar: Use query para filtrar ativos por data/hora/modo=Centralizado

        for (Anuncio anuncio : anuncios) {
            if (anuncio.getModoEntrega() != ModoEntrega.CENTRALIZADO) continue; // Só centralizado

            // Checa janela de tempo
            LocalDate today = now.toLocalDate();
            LocalTime time = now.toLocalTime();
            if (today.isBefore(anuncio.getDataInicio()) || today.isAfter(anuncio.getDataFim()) ||
                time.isBefore(anuncio.getHoraInicio()) || time.isAfter(anuncio.getHoraFim())) {
                continue;
            }

            // Checa se usuário está no local (GPS ou WiFi)
            Local anuncioLocal = anuncio.getLocal();
            if (!isUserInLocal(location, anuncioLocal)) continue;

            // Checa policy e restricoes (whitelist/blacklist com perfis)
            if (!matchesPolicy(user, anuncio)) continue;

            // VERIFICA DUPLICAÇÃO NO BANCO DE DADOS (mais confiável que Set em memória)
            if (notificacaoRepo.existsByUserIdAndAnuncioId(user.getId(), anuncio.getId())) {
                continue; // Já foi notificado, ignora
            }

            // Envia notif (o NotificationService também verifica duplicação como segurança extra)
            System.out.println("[LocationService] Anúncio encontrado para user " + user.getId() + " - anuncioId=" + anuncio.getId() + " | titulo=" + anuncio.getTitulo());
            notificationService.enviarNotificacao(user.getId(), anuncio);
        }
    }

    private boolean isUserInLocal(UserLocation userLoc, Local anuncioLocal) {
        if (anuncioLocal.getTipo() == TipoLocalizacao.GPS) {
            if (userLoc.getLatitude() == null || userLoc.getLongitude() == null) return false;
            double distance = calculateDistance(userLoc.getLatitude(), userLoc.getLongitude(),
                                               anuncioLocal.getLatitude(), anuncioLocal.getLongitude());
            return distance <= anuncioLocal.getRaioMetros();
        } else if (anuncioLocal.getTipo() == TipoLocalizacao.WIFI) {
            return !Collections.disjoint(userLoc.getWifiIds(), anuncioLocal.getWifiIds());
        }
        return false;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Fórmula Haversine (em metros)
        final int R = 6371000; // Raio da Terra
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private boolean matchesPolicy(User user, Anuncio anuncio) {
        Map<String, String> restricoes = anuncio.getRestricoes();
        if (restricoes.isEmpty()) return true; // Sem restrições = todos
        
        // Se policyType é null, permite todos
        if (anuncio.getPolicyType() == null) return true;

        if (anuncio.getPolicyType() == PolicyType.WHITELIST) {
            // WHITELIST: TODAS as restrições devem corresponder (AND)
            // Verifica se o utilizador tem TODOS os pares chave-valor exigidos
            for (Map.Entry<String, String> restricao : restricoes.entrySet()) {
                String chaveExigida = restricao.getKey();
                String valorExigido = restricao.getValue();
                
                // Busca o valor do utilizador para esta chave
                String valorUsuario = getUserProfileValue(user, chaveExigida);
                
                // Compara case-insensitive
                if (!valorExigido.equalsIgnoreCase(valorUsuario)) {
                    // Utilizador não tem este critério → não recebe
                    return false;
                }
            }
            // Todas as restrições corresponderam → recebe
            return true;
            
        } else if (anuncio.getPolicyType() == PolicyType.BLACKLIST) {
            // BLACKLIST: bloqueia se corresponder a QUALQUER critério (OR)
            for (UserProfile profile : user.getProfiles()) {
                String userKey = profile.getProfileKey();
                String userVal = profile.getProfileValueNormalized();
                if (restricoes.containsKey(userKey) && restricoes.get(userKey).equalsIgnoreCase(userVal)) {
                    // Utilizador corresponde a algum critério → bloqueado
                    return false;
                }
            }
            // Utilizador não corresponde a nenhum critério → recebe
            return true;
        }
        
        return false;
    }
    
    /**
     * Obtém o valor do perfil do utilizador para uma chave específica
     * Retorna string vazia se não encontrar (case-insensitive)
     */
    private String getUserProfileValue(User user, String chave) {
        if (user == null || user.getProfiles() == null) {
            return "";
        }
        return user.getProfiles().stream()
                .filter(p -> p.getProfileKey() != null && p.getProfileKey().equals(chave))
                .map(UserProfile::getProfileValueNormalized) // Usa normalized para case-insensitive
                .filter(v -> v != null)
                .findFirst()
                .orElse("");
    }
}