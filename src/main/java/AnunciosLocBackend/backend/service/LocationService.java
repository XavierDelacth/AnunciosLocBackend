
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

    public void updateLocation(Long userId, Double lat, Double lng, List<String> wifiIds) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User não encontrado"));

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

            // Checa se já notificado (evita spam)
            Set<Long> notified = location.getNotifiedAnuncioIds();
            if (notified.contains(anuncio.getId())) continue;

            // Envia notif
            notificationService.enviarNotificacao(user.getId(), anuncio);

            // Marca como notificado
            notified.add(anuncio.getId());
            locationRepo.save(location);
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

        boolean match = false;
        for (UserProfile profile : user.getProfiles()) {
            String userKey = profile.getProfileKey();
            String userVal = profile.getProfileValueNormalized(); // Use normalized para case-insensitive
            if (restricoes.containsKey(userKey) && restricoes.get(userKey).equalsIgnoreCase(userVal)) {
                match = true;
                break;
            }
        }

        if (anuncio.getPolicyType() == PolicyType.WHITELIST) {
            return match; // Só se match
        } else if (anuncio.getPolicyType() == PolicyType.BLACKLIST) {
            return !match; // Só se NÃO match
        }
        return false;
    }
}