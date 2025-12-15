/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;



import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.model.Notificacao;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.NotificacaoRepository;
import AnunciosLocBackend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;


/**
 *
 * @author delacth
 */

@Service
public class NotificationService {
    
    @Autowired
    private UserRepository userRepo;
    @Autowired 
    private NotificacaoRepository notificacaoRepo;

    @Transactional
    public void enviarNotificacao(Long userId, Anuncio anuncio) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return;

        // VALIDAÇÃO 1: Não notificar o criador do anúncio
        if (anuncio.getUsuario() != null && anuncio.getUsuario().getId().equals(userId)) {
            System.out.println("Notificação IGNORADA: Utilizador " + userId + " é o criador do anúncio " + anuncio.getId());
            return;
        }

        // VALIDAÇÃO 2: Verificar se a data/hora atual está dentro do intervalo permitido
        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();
        
        if (hoje.isBefore(anuncio.getDataInicio()) || hoje.isAfter(anuncio.getDataFim())) {
            System.out.println("Notificação IGNORADA: Data atual (" + hoje + ") fora do intervalo (" + 
                anuncio.getDataInicio() + " a " + anuncio.getDataFim() + ")");
            return;
        }
        
        if (agora.isBefore(anuncio.getHoraInicio()) || agora.isAfter(anuncio.getHoraFim())) {
            System.out.println("Notificação IGNORADA: Hora atual (" + agora + ") fora do intervalo (" + 
                anuncio.getHoraInicio() + " a " + anuncio.getHoraFim() + ")");
            return;
        }

        // VALIDAÇÃO 3: Verifica se já foi notificado (evita duplicação) - verificação dupla para evitar race condition
        if (notificacaoRepo.existsByUserIdAndAnuncioId(userId, anuncio.getId())) {
            System.out.println("Notificação DUPLICADA ignorada para user " + userId + " e anúncio " + anuncio.getId());
            return;
        }

        // SALVA NO BANCO (dentro da transação para evitar race condition)
        // Verificação dupla após lock da transação
        if (notificacaoRepo.existsByUserIdAndAnuncioId(userId, anuncio.getId())) {
            System.out.println("Notificação DUPLICADA ignorada (race condition detectada) para user " + userId + " e anúncio " + anuncio.getId());
            return;
        }
        
        Notificacao notif = new Notificacao(
            userId,
            anuncio.getId(),
            anuncio.getTitulo(),
            anuncio.getDescricao()
        );
        notificacaoRepo.save(notif);
        System.out.println("Notificação SALVA para user " + userId + " do anúncio " + anuncio.getId());

        // ENVIA NOTIFICAÇÃO PUSH DO SISTEMA (tipo Facebook) SE TIVER TOKEN
        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            try {
                // Configuração para Android - Notificação do sistema com prioridade alta
                AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH) // Prioridade alta para aparecer mesmo com app fechado
                    .setNotification(AndroidNotification.builder()
                        .setTitle(anuncio.getTitulo())
                        .setBody(anuncio.getDescricao())
                        .setChannelId("anuncios_channel") // Mesmo channel do app Android
                        .setSound("default")
                        .setPriority(AndroidNotification.Priority.HIGH)
                        .setClickAction("OPEN_MAIN_ACTIVITY") // Abre o app ao clicar
                        .build())
                    .putData("anuncioId", anuncio.getId().toString()) // Dados adicionais
                    .putData("type", "anuncio") // Tipo de notificação
                    .build();

                // Configuração para iOS
                ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                        .setAlert(anuncio.getTitulo() + ": " + anuncio.getDescricao())
                        .setSound("default")
                        .setContentAvailable(true)
                        .build())
                    .build();

                // Cria mensagem com notificação do sistema
                Message msg = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(Notification.builder()
                        .setTitle(anuncio.getTitulo())
                        .setBody(anuncio.getDescricao())
                        .build())
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig)
                    .build();

                FirebaseMessaging.getInstance().send(msg);
                System.out.println("Notificação do sistema enviada com sucesso (FCM)");
            } catch (Exception e) {
                System.err.println("Erro ao enviar notificação FCM: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("FCM ignorado: token ausente");
        }
    }
}