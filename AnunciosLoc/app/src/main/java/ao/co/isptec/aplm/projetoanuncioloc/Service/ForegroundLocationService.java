package ao.co.isptec.aplm.projetoanuncioloc.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocationUpdateRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class ForegroundLocationService extends Service {

    private static final String TAG = "FGLocationService";
    public static final String CHANNEL_ID = "location_channel";
    private static final int NOTIF_ID = 4321;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (result == null) return;
                var loc = result.getLastLocation();
                if (loc == null) return;
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();
                Log.d(TAG, "Location update: " + lat + ", " + lng);
                sendLocationToServer(lat, lng);
            }
        };
    }

    private void sendLocationToServer(double lat, double lng) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) return;

        List<String> wifiIds = new ArrayList<>();
        LocationUpdateRequest request = new LocationUpdateRequest(userId, lat, lng, wifiIds);
        Call<Void> call = RetrofitClient.getApiService(this).updateLocation(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, response.isSuccessful() ? "Location sent" : "HTTP " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to send location: " + t.getMessage());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Intent i = new Intent(this, ao.co.isptec.aplm.projetoanuncioloc.MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("AnúnciosLoc — Localização")
                .setContentText("Enviando localização em segundo plano")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(NOTIF_ID, notif);

        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        if (checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            Log.e(TAG, "Sem permissão de localização");
            stopSelf();
            return;
        }
        LocationRequest req = LocationRequest.create();
        req.setInterval(5 * 60 * 1000); // 5 minutos
        req.setFastestInterval(60 * 1000); // 1 minuto
        req.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        fusedLocationClient.requestLocationUpdates(req, locationCallback, null);
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Localização em segundo plano", NotificationManager.IMPORTANCE_LOW);
            NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (mgr != null) mgr.createNotificationChannel(channel);
        }
    }
}
