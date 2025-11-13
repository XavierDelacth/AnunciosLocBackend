/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package AnunciosLocBackend.backend.service;



import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import AnunciosLocBackend.backend.model.Anuncio;
import AnunciosLocBackend.backend.model.Notificacao;
import AnunciosLocBackend.backend.model.User;
import AnunciosLocBackend.backend.repository.NotificacaoRepository;
import AnunciosLocBackend.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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

    public void enviarNotificacao(Long userId, Anuncio anuncio) {
        User user = userRepo.findById(userId).orElse(null);
     if (user == null) return;

     // SEMPRE SALVA NO BANCO
     Notificacao notif = new Notificacao(
         userId,
         anuncio.getId(),
         anuncio.getTitulo(),
         anuncio.getDescricao()
     );
     notificacaoRepo.save(notif);
     System.out.println("Notificação SALVA para user " + userId);

     // SÓ ENVIA FCM SE TIVER TOKEN
     if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
         try {
             Message msg = Message.builder()
                 .setToken(user.getFcmToken())
                 .setNotification(Notification.builder()
                     .setTitle(anuncio.getTitulo())
                     .setBody(anuncio.getDescricao())
                     .build())
                 .build();
             FirebaseMessaging.getInstance().send(msg);
             System.out.println("FCM enviado com sucesso");
         } catch (Exception e) {
             System.err.println("Erro FCM: " + e.getMessage());
         }
     } else {
         System.err.println("FCM ignorado: token ausente");
     }
    
}
}