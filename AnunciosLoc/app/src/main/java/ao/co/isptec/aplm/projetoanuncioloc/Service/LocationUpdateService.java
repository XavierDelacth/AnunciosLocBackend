// LocationUpdateService.java - VERSÃO FINAL (sem crash em Xiaomi)
package ao.co.isptec.aplm.projetoanuncioloc.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocationUpdateRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class LocationUpdateService extends Service {

    private static final String TAG = "LocationService";
    private static final long UPDATE_INTERVAL = 5 * 60 * 1000; // 5 minutos

    private FusedLocationProviderClient fusedLocationClient;
    private WifiManager wifiManager;
    private Handler handler;
    private Runnable updateRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        handler = new Handler(Looper.getMainLooper());

        updateRunnable = () -> {
            updateLocation();
            handler.postDelayed(updateRunnable, UPDATE_INTERVAL);
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(updateRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(updateRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateLocation() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Log.e(TAG, "userId não encontrado");
            return;
        }

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            Log.e(TAG, "Sem permissão de localização");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Log.w(TAG, "Localização nula");
                return;
            }

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            // Só usa o SSID da rede conectada (não precisa de startScan!)
            List<String> wifiIds = new ArrayList<>();
            String connectedSsid = getConnectedWifiSsid();
            if (connectedSsid != null) {
                wifiIds.add(connectedSsid);
            }

            LocationUpdateRequest request = new LocationUpdateRequest(userId, lat, lng, wifiIds);
            Call<Void> call = RetrofitClient.getApiService(this).updateLocation(request);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d(TAG, response.isSuccessful() ? "Localização enviada com sucesso" : "Erro HTTP " + response.code());
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Falha no envio: " + t.getMessage());
                }
            });

        }).addOnFailureListener(e -> Log.e(TAG, "Erro ao obter localização", e));
    }

    // 100% seguro - não usa startScan nem CHANGE_WIFI_STATE
    private String getConnectedWifiSsid() {
        try {
            var info = wifiManager.getConnectionInfo();
            if (info != null) {
                String ssid = info.getSSID();
                if (ssid != null && !ssid.equals("<unknown ssid>") && !ssid.isEmpty()) {
                    return ssid.replace("\"", ""); // remove aspas
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Erro ao obter SSID conectado", e);
        }
        return null;
    }
}