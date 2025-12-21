package AnunciosLocBackend.backend.service;

import AnunciosLocBackend.backend.model.DeviceToken;
import AnunciosLocBackend.backend.repository.DeviceTokenRepository;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ScheduledNotificationService {

    @Autowired
    private DeviceTokenRepository deviceTokenRepo;

    @Scheduled(fixedRateString = "${notifications.heartbeatRateMs:180000}", initialDelayString = "${notifications.heartbeatInitialDelayMs:60000}")
    public void sendHeartbeatToActiveTokens() {
        List<DeviceToken> tokens = deviceTokenRepo.findAllByActiveTrue();
        if (tokens == null || tokens.isEmpty()) return;

        for (var dt : tokens) {
            if (dt.getToken() == null) continue;
            // Build a small heartbeat message. Add Notification block so system delivers it when app is backgrounded/closed
            Message msg = Message.builder()
                    .setToken(dt.getToken())
                    // Data-only heartbeat: avoid system showing UI notification automatically
                    .putData("type", "heartbeat")
                    .putData("ts", String.valueOf(System.currentTimeMillis()))
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            // Send async to avoid blocking
            CompletableFuture.runAsync(() -> {
                try {
                    FirebaseMessaging.getInstance().send(msg);
                } catch (Exception e) {
                    // If token invalid, deactivate it
                    try {
                        var opt = deviceTokenRepo.findByToken(dt.getToken());
                        opt.ifPresent(t -> { t.setActive(false); deviceTokenRepo.save(t); });
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            });
        }
    }
}
