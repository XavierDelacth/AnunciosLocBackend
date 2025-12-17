package ao.co.isptec.aplm.projetoanuncioloc.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ao.co.isptec.aplm.projetoanuncioloc.MainActivity;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "anuncios_channel";
    private static final String CHANNEL_NAME = "Anúncios Localizados";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Novo FCM Token: " + token);
        // Salva o token no servidor
        saveTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d(TAG, "Mensagem FCM recebida - From: " + message.getFrom());
        Log.d(TAG, "Notification: " + message.getNotification());
        Log.d(TAG, "Data: " + message.getData());

        // IMPORTANTE: Quando o app está em BACKGROUND, o FCM exibe automaticamente a notificação
        // Se criarmos outra manualmente aqui, teremos DUPLICAÇÃO!
        // Só criar notificação manual quando:
        // 1. App está em FOREGROUND (onMessageReceived só é chamado em foreground)
        // 2. OU quando vem apenas dados (sem notification payload)

        if (message.getNotification() != null) {
            // Se tem notification payload, o FCM já exibe automaticamente quando app está em background
            // Só criar manualmente se app estiver em foreground (que é quando onMessageReceived é chamado)
            String title = message.getNotification().getTitle();
            String body = message.getNotification().getBody();
            Log.d(TAG, "Exibindo notificação manualmente (app em foreground) - Title: " + title + ", Body: " + body);
            showNotification(title, body);
        } else if (!message.getData().isEmpty()) {
            // Se vier apenas dados (sem notification payload), criar notificação manualmente
            String title = message.getData().get("title");
            String body = message.getData().get("body");
            if (title != null && body != null) {
                Log.d(TAG, "Exibindo notificação de dados - Title: " + title + ", Body: " + body);
                showNotification(title, body);
            }
        }
    }

    private void saveTokenToServer(String token) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Log.e(TAG, "UserId não encontrado - Token não salvo");
            return;
        }

        // Salva localmente (para permitir desregisto no logout)
        prefs.edit().putString("fcmToken", token).apply();

        // Cria o Map esperado pelo backend
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("token", token);
        String deviceInfo = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        body.put("deviceInfo", deviceInfo);

        Call<Void> call = RetrofitClient.getApiService(this).updateFcmToken(userId, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Token FCM salvo no servidor com sucesso");
                } else {
                    Log.e(TAG, "Erro ao salvar token: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Falha na rede ao salvar token: " + t.getMessage());
            }
        });
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Cria canal de notificação com importância alta (aparece mesmo com app fechado)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificações de anúncios localizados");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cria notificação do sistema (tipo Facebook) - aparece mesmo com app fechado
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.logoapp)  // Ícone do app
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridade alta
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Som, vibração e luz
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Visível mesmo na tela de bloqueio
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body)); // Texto expandido

        // Gera ID único para cada notificação (evita sobreposição)
        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        Log.d(TAG, "Notificação do sistema exibida: " + title);
    }
}

