package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.MainAnuncioAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse;
import ao.co.isptec.aplm.projetoanuncioloc.Service.LocationUpdateService;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_EDITAR_ANUNCIO = 1001;
    private CardView cardLocais, cardAnuncios;
    private TextView tabCriados, tabGuardados, tvLocation, tvEmptyAnuncios;
    private ImageView btnProfile, btnNotification;
    private RecyclerView rvAnunciosMain;
    private MainAnuncioAdapter adapter;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private TextView badgeNotificacao;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private boolean gpsAtivado = false;
    private static final int REQUEST_ENABLE_GPS = 1002;
    private LocationCallback locationCallback;
    private Handler locationTimeoutHandler;

    private static final int LOCATION_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1);
        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Explicação opcional para o utilizador
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Toast.makeText(this,
                            "A app precisa da localização para mostrar anúncios próximos",
                            Toast.LENGTH_LONG).show();
                }

                // Pede a permissão
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
            } else {
                // Já tem permissão → inicia o serviço
                iniciarLocationService();
            }
        } else {
            // Android antigo → inicia direto
            iniciarLocationService();
        }

        initViews();
        setupClickListeners();
        setupTabs();
        selectTab(true);

        // Inicializa provedor de localização
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializa o handler para timeout
        locationTimeoutHandler = new Handler(Looper.getMainLooper());

        // Verifica estado inicial do GPS
        gpsAtivado = isGPSEnabled();

        if (!gpsAtivado) {
            mostrarDialogGPS();
        } else {
            // Tenta obter localização
            obterLocalizacaoAtual();
        }

        carregarAnuncios();
        carregarContagemNotificacoes();

        // Compatível com back gesture (Android 13+)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getIntent().getBooleanExtra("fromGuardados", false)) {
                    selectTab(true);
                    getIntent().removeExtra("fromGuardados");
                } else {
                    finish();
                }
            }
        });

        startService(new Intent(this, LocationUpdateService.class));
    }

    private void initViews() {
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);
        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);
        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);
        tvLocation = findViewById(R.id.tvLocation);
        tvEmptyAnuncios = findViewById(R.id.tvEmptyAnuncios);
        rvAnunciosMain = findViewById(R.id.recyclerView);
        badgeNotificacao = findViewById(R.id.badgeNotificacao);
    }

    private void setupClickListeners() {
        cardLocais.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarLocalActivity.class)));

        /*cardAnuncios.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarAnunciosActivity.class)));
    */
        cardAnuncios.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdicionarAnunciosActivity.class);
            startActivityForResult(intent, AdicionarAnunciosActivity.REQUEST_CODE_EDIT);
        });

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacoesActivity.class)));
    }

    private void setupTabs() {
        tabCriados.setOnClickListener(v -> selectTab(true));

        tabGuardados.setOnClickListener(v -> {
            selectTab(false);
            Intent intent = new Intent(this, AnuncioGuardadoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void selectTab(boolean isCriados) {
        if (isCriados) {
            tabCriados.setBackgroundColor(getColor(R.color.verde_principal));
            tabCriados.setTextColor(getColor(R.color.white));
            tabGuardados.setBackgroundColor(getColor(R.color.white));
            tabGuardados.setTextColor(getColor(R.color.verde_principal));
        } else {
            tabCriados.setBackgroundColor(getColor(R.color.white));
            tabCriados.setTextColor(getColor(R.color.verde_principal));
            tabGuardados.setBackgroundColor(getColor(R.color.verde_principal));
            tabGuardados.setTextColor(getColor(R.color.white));
        }
    }

    private void obterLocalizacaoAtual() {
        Log.d("MAPS_DEBUG", "Iniciando obtenção de localização...");

        // Mostra estado de carregamento
        runOnUiThread(() -> tvLocation.setText("Obtendo localização..."));

        // Verifica se já tem permissão
        if (temPermissaoLocalizacao()) {
            Log.d("MAPS_DEBUG", "Permissões já concedidas, obtendo localização...");
            obterLocalizacaoComPermissao();
        } else {
            // Solicita permissões
            Log.d("MAPS_DEBUG", "Solicitando permissões de localização...");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void obterLocalizacaoComPermissao() {
        // Primeiro tenta obter a última localização conhecida (mais rápido)
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(this, location -> {
            if (location != null && location.getTime() > System.currentTimeMillis() - 2 * 60 * 1000) {
                // Localização recente (menos de 2 minutos)
                Log.d("MAPS_DEBUG", "Localização recente obtida: " + location.getLatitude() + ", " + location.getLongitude());
                converterCoordenadasParaEndereco(location);
            } else {
                Log.d("MAPS_DEBUG", "Última localização não disponível ou muito antiga, solicitando atualizações...");
                solicitarAtualizacoesDeLocalizacao();
            }
        });

        task.addOnFailureListener(this, e -> {
            Log.e("MAPS_DEBUG", "Erro ao obter última localização: " + e.getMessage());
            solicitarAtualizacoesDeLocalizacao();
        });
    }

    @SuppressLint("MissingPermission")
    private void solicitarAtualizacoesDeLocalizacao() {
        Log.d("MAPS_DEBUG", "Solicitando atualizações de localização...");

        if (!temPermissaoLocalizacao()) {
            Log.e("MAPS_DEBUG", "Permissões não concedidas para atualizações");
            return;
        }

        try {
            // Remove callbacks anteriores para evitar múltiplas solicitações
            if (locationCallback != null) {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            }

            // Configuração para obter localização rápida
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000); // 10 segundos
            locationRequest.setFastestInterval(5000); // 5 segundos
            locationRequest.setNumUpdates(1); // Apenas uma atualização
            locationRequest.setMaxWaitTime(15000); // Timeout de 15 segundos

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        Location location = locationResult.getLastLocation();
                        if (location != null) {
                            Log.d("MAPS_DEBUG", "Localização obtida via atualizações: " +
                                    location.getLatitude() + ", " + location.getLongitude());
                            converterCoordenadasParaEndereco(location);
                        } else {
                            Log.e("MAPS_DEBUG", "Localização nula nas atualizações");
                            runOnUiThread(() -> tvLocation.setText("Localização não disponível"));
                        }
                    } else {
                        Log.e("MAPS_DEBUG", "LocationResult é nulo");
                        runOnUiThread(() -> tvLocation.setText("Localização não disponível"));
                    }

                    // Para as atualizações após receber uma localização
                    fusedLocationProviderClient.removeLocationUpdates(this);

                    // Remove o timeout
                    if (locationTimeoutHandler != null) {
                        locationTimeoutHandler.removeCallbacksAndMessages(null);
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );

            // Timeout: Se após 15 segundos não obtiver localização, mostra mensagem
            locationTimeoutHandler.postDelayed(() -> {
                runOnUiThread(() -> {
                    String textoAtual = tvLocation.getText().toString();
                    if (textoAtual.equals("Obtendo localização...")) {
                        tvLocation.setText("Localização não disponível");
                        Log.d("MAPS_DEBUG", "Timeout na obtenção de localização");

                        // Para as atualizações
                        if (locationCallback != null) {
                            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                        }
                    }
                });
            }, 15000);

        } catch (SecurityException e) {
            Log.e("MAPS_DEBUG", "Erro de segurança: " + e.getMessage());
            runOnUiThread(() -> tvLocation.setText("Erro de permissão"));
        } catch (Exception e) {
            Log.e("MAPS_DEBUG", "Erro inesperado: " + e.getMessage());
            runOnUiThread(() -> tvLocation.setText("Erro ao obter localização"));
        }
    }

    private void setupListaAnuncios() {


        // Setup RecyclerView
        rvAnunciosMain.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAnuncioAdapter(this, listaAnuncios, true, null); // ← MUDAR AQUI
        rvAnunciosMain.setAdapter(adapter);

        atualizarVisibilidade();
    }

    private void atualizarVisibilidade() {
        if (listaAnuncios.isEmpty()) {
            rvAnunciosMain.setVisibility(View.GONE);
            tvEmptyAnuncios.setVisibility(View.VISIBLE);
            tvEmptyAnuncios.setText("Nenhum anúncio");
        } else {
            rvAnunciosMain.setVisibility(View.VISIBLE);
            tvEmptyAnuncios.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida → inicia o serviço
                Toast.makeText(this, "Permissão de localização concedida", Toast.LENGTH_SHORT).show();
                iniciarLocationService();
            } else {
                // Permissão negada
                Toast.makeText(this, "Sem permissão de localização, os anúncios próximos não funcionarão",
                        Toast.LENGTH_LONG).show();
                // Opcional: desabilita botões ou features que usam localização
            }
        }

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("MAPS_DEBUG", "Permissão concedida, obtendo localização...");

                // Pequeno delay para garantir que as permissões foram processadas
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (temPermissaoLocalizacao()) {
                        obterLocalizacaoComPermissao();
                    }
                }, 500);

            } else {
                // Permissão negada
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Usuário negou, mas não marcou "nunca mais perguntar"
                    tvLocation.setText("Localização necessária para mostrar anúncios próximos");

                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Permissão de Localização")
                            .setMessage("O app precisa da localização para mostrar anúncios relevantes da sua área. Deseja permitir?")
                            .setPositiveButton("Sim", (dialog, which) -> obterLocalizacaoAtual())
                            .setNegativeButton("Não", (dialog, which) -> {
                                tvLocation.setText("Localização não permitida");
                                setupListaAnuncios();
                            })
                            .show();
                } else {
                    // Usuário marcou "nunca mais perguntar"
                    tvLocation.setText("Ative a localização nas configurações");

                    new android.app.AlertDialog.Builder(this)
                            .setTitle("Localização Necessária")
                            .setMessage("Para usar todos os recursos do app, ative a localização nas configurações do dispositivo.")
                            .setPositiveButton("Configurações", (dialog, which) -> {
                                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancelar", (dialog, which) -> {
                                tvLocation.setText("Localização desativada");
                                setupListaAnuncios();
                            })
                            .show();
                }
            }
        }
    }

    private void converterCoordenadasParaEndereco(Location location) {
        if (location == null) {
            Log.e("MAPS_DEBUG", "Localização é nula");
            runOnUiThread(() -> tvLocation.setText("Localização não disponível"));
            return;
        }

        Log.d("MAPS_DEBUG", "Convertendo coordenadas: " + location.getLatitude() + ", " + location.getLongitude());

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1
                );

                runOnUiThread(() -> {
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        // Tenta obter o nome da localidade primeiro
                        String localidade = address.getLocality();
                        if (localidade != null && !localidade.isEmpty()) {
                            tvLocation.setText(localidade);
                            Log.d("MAPS_DEBUG", "Localidade definida: " + localidade);
                        } else {
                            // Fallback para o endereço completo
                            String enderecoCompleto = address.getAddressLine(0);
                            if (enderecoCompleto != null && !enderecoCompleto.isEmpty()) {
                                tvLocation.setText(enderecoCompleto);
                                Log.d("MAPS_DEBUG", "Endereço completo definido: " + enderecoCompleto);
                            } else {
                                tvLocation.setText("Localização atual");
                                Log.d("MAPS_DEBUG", "Usando fallback: Localização atual");
                            }
                        }
                    } else {
                        tvLocation.setText("Localização atual");
                        Log.d("MAPS_DEBUG", "Nenhum endereço encontrado");
                    }
                });

            } catch (IOException e) {
                Log.e("MAPS_DEBUG", "Erro Geocoder: " + e.getMessage());
                runOnUiThread(() -> tvLocation.setText("Localização atual"));
            } catch (Exception e) {
                Log.e("MAPS_DEBUG", "Erro inesperado: " + e.getMessage());
                runOnUiThread(() -> tvLocation.setText("Localização atual"));
            }
        }).start();
    }



    private boolean isGPSEnabled() {
        try {
            android.location.LocationManager locationManager =
                    (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e("MAPS_DEBUG", "Erro ao verificar GPS: " + e.getMessage());
            return false;
        }
    }

    private void mostrarDialogGPS() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("GPS Desativado")
                .setMessage("Para uma melhor experiência, ative o GPS para ver anúncios na sua localização atual.")
                .setPositiveButton("Ativar GPS", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, REQUEST_ENABLE_GPS);
                })
                .setNegativeButton("Continuar Sem GPS", (dialog, which) -> {
                    tvLocation.setText("GPS desativado");
                    setupListaAnuncios();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDITAR_ANUNCIO && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra("anuncio_editado")) {
                Anuncio anuncioEditado = data.getParcelableExtra("anuncio_editado");
                int position = data.getIntExtra("position", -1);

                if (anuncioEditado != null && position != -1 && position < listaAnuncios.size()) {
                    // Atualiza o anúncio na lista
                    listaAnuncios.set(position, anuncioEditado);
                    adapter.notifyItemChanged(position);
                    Toast.makeText(this, "Anúncio atualizado!", Toast.LENGTH_SHORT).show();
                } else {
                    // Se algo deu errado, recarrega toda a lista
                    carregarAnuncios();
                }
            }
        }

        if (requestCode == REQUEST_ENABLE_GPS) {
            // Usuário voltou das configurações de GPS
            if (isGPSEnabled()) {
                Log.d("MAPS_DEBUG", "GPS ativado pelo usuário, obtendo localização...");
                gpsAtivado = true;
                // Pequeno delay para garantir que o GPS está realmente ativo
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    obterLocalizacaoAtual();
                }, 1000);
            } else {
                Log.d("MAPS_DEBUG", "Usuário não ativou o GPS");
                tvLocation.setText("GPS desativado");
            }
        }

        if (requestCode == AdicionarAnunciosActivity.REQUEST_CODE_EDIT  // Ou o código que usas para adicionar
                && resultCode == RESULT_OK
                && data != null
                && data.getBooleanExtra("anuncio_criado", false)) {

            Log.d("MainActivity", "Novo anúncio criado – atualizando lista");
            Long userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("userId", -1);
            if (userId != -1) {
                carregarAnuncios(); // Reaproveita o teu método existente para recarregar a lista
                Toast.makeText(this, "Lista atualizada!", Toast.LENGTH_SHORT).show();
            }
            }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Verifica se o GPS foi ativado enquanto a app estava em segundo plano
        boolean gpsAgoraAtivo = isGPSEnabled();
        if (!gpsAtivado && gpsAgoraAtivo) {
            Log.d("MAPS_DEBUG", "GPS ativado durante onResume, obtendo localização...");
            gpsAtivado = true;
            obterLocalizacaoAtual();
        } else if (gpsAtivado && !gpsAgoraAtivo) {
            Log.d("MAPS_DEBUG", "GPS desativado durante onResume");
            gpsAtivado = false;
            tvLocation.setText("GPS desativado");
        }

        // Verifica se precisamos tentar obter localização novamente
        verificarEstadoLocalizacao();
        carregarContagemNotificacoes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Para as atualizações de localização quando a activity não está visível
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        if (locationTimeoutHandler != null) {
            locationTimeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpa recursos
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
        if (locationTimeoutHandler != null) {
            locationTimeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    private void verificarEstadoLocalizacao() {
        String textoAtual = tvLocation.getText().toString();

        // Se a localização atual não foi obtida ou está com mensagem de erro, tenta novamente
        if (textoAtual.equals("GPS desativado") ||
                textoAtual.equals("Localização não disponível") ||
                textoAtual.equals("Erro ao obter localização") ||
                textoAtual.equals("Erro de permissão") ||
                textoAtual.equals("Obtendo localização...") ||
                textoAtual.equals("Localização atual")) {

            if (isGPSEnabled() && temPermissaoLocalizacao()) {
                Log.d("MAPS_DEBUG", "Tentando obter localização novamente em onResume...");
                obterLocalizacaoAtual();
            }
        }
    }

    private boolean temPermissaoLocalizacao() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void carregarAnuncios() {
        Long userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("userId", -1);
        if (userId == -1) return;

        // Inicializa o adapter com o listener de ações - MUDAR false para true
        rvAnunciosMain.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MainAnuncioAdapter(this, listaAnuncios, true, new MainAnuncioAdapter.OnActionClickListener() { // ← MUDAR AQUI
            @Override
            public void onEditClick(Anuncio anuncio, int position) {
                Log.d("MAIN_EDIT", "=== EDITANDO ANÚNCIO ===");
                Log.d("MAIN_EDIT", "Título: " + anuncio.titulo);
                Log.d("MAIN_EDIT", "ID: " + anuncio.id);
                Log.d("MAIN_EDIT", "Posição: " + position);

                Intent intent = new Intent(MainActivity.this, AdicionarAnunciosActivity.class);

                // Use letras minúsculas para consistência
                intent.putExtra("ANUNCIO_PARA_EDITAR", anuncio);  // Parcelable!
                intent.putExtra("POSICAO", position);
                startActivityForResult(intent, REQUEST_CODE_EDITAR_ANUNCIO);
            }

            @Override
            public void onDeleteClick(Anuncio anuncio, int position) {
                // CHAMA O MÉTODO PARA ELIMINAR O ANÚNCIO
                eliminarAnuncio(anuncio, position);
            }

            @Override
            public void onSaveClick(Anuncio anuncio, int position) {
                // Implementar salvar/guardar se necessário
                Toast.makeText(MainActivity.this, "Guardar: " + anuncio.titulo, Toast.LENGTH_SHORT).show();
            }
        });
        rvAnunciosMain.setAdapter(adapter);

        // Resto do código para carregar anúncios...
        Call<List<AnuncioResponse>> call = RetrofitClient.getApiService(this).getMeusAnuncios(userId);
        call.enqueue(new Callback<List<AnuncioResponse>>() {
            @Override
            public void onResponse(Call<List<AnuncioResponse>> call, Response<List<AnuncioResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAnuncios.clear();

                    for (AnuncioResponse responseItem : response.body()) {
                        Anuncio anuncio = responseItem.toAnuncio();
                        listaAnuncios.add(anuncio);
                    }

                    adapter.notifyDataSetChanged();
                    atualizarVisibilidade();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao carregar anúncios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AnuncioResponse>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void eliminarAnuncio(Anuncio anuncio, int position) {
        if (anuncio == null || anuncio.id == null) {
            Log.e("ELIMINAR_ANUNCIO", "ERRO: Anúncio nulo ou sem ID!");
            Toast.makeText(this, "Erro: Anúncio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Toast.makeText(this, "Erro: Utilizador não logado", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ELIMINAR_ANUNCIO", "=== INICIANDO ELIMINAÇÃO ===");
        Log.d("ELIMINAR_ANUNCIO", "Título: " + anuncio.titulo);
        Log.d("ELIMINAR_ANUNCIO", "ID do anúncio: " + anuncio.id);
        Log.d("ELIMINAR_ANUNCIO", "Posição na lista: " + position);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Anúncio")
                .setMessage("Tem certeza que deseja eliminar o anúncio \"" + anuncio.titulo + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Log.d("ELIMINAR_ANUNCIO", "Usuário confirmou eliminação. Chamando API...");

                    Call<Void> call = RetrofitClient.getApiService(this).eliminarAnuncio(anuncio.id, userId);
                    call.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            Log.d("ELIMINAR_ANUNCIO", "Resposta do servidor recebida. Código HTTP: " + response.code());

                            if (response.isSuccessful()) {
                                Log.d("ELIMINAR_ANUNCIO", "SUCESSO! Anúncio eliminado no servidor.");

                                listaAnuncios.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, listaAnuncios.size());

                                Toast.makeText(MainActivity.this, "Anúncio eliminado com sucesso!", Toast.LENGTH_LONG).show();
                                atualizarVisibilidade(); // ou atualizarVisibilidadeListaVazia()

                                Log.d("ELIMINAR_ANUNCIO", "UI atualizada. Anúncio removido da posição " + position);
                            } else {
                                Log.e("ELIMINAR_ANUNCIO", "FALHA no servidor! Código: " + response.code());
                                Log.e("ELIMINAR_ANUNCIO", "Mensagem: " + response.message());
                                Toast.makeText(MainActivity.this,
                                        "Erro do servidor: " + response.code() + " " + response.message(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Log.e("ELIMINAR_ANUNCIO", "FALHA TOTAL: Sem ligação ou erro de rede");
                            Log.e("ELIMINAR_ANUNCIO", "Erro: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                            t.printStackTrace();

                            Toast.makeText(MainActivity.this,
                                    "Sem ligação à internet ou servidor offline", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Log.d("ELIMINAR_ANUNCIO", "Usuário cancelou a eliminação");
                })
                .show();
    }


    private void carregarContagemNotificacoes() {
        Long userId = getSharedPreferences("app_prefs", MODE_PRIVATE).getLong("userId", -1);
        if (userId == -1) {
            Log.e("NOTIFICACOES", "UserId não encontrado");
            return;
        }

        Log.d("NOTIFICACOES", "Carregando contagem para userId: " + userId);

        Call<Integer> call = RetrofitClient.getApiService(this).getContagemNotificacoes(userId);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                Log.d("NOTIFICACOES", "Resposta recebida. Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    int contagem = response.body();
                    Log.d("NOTIFICACOES", "Contagem de notificações: " + contagem);
                    atualizarBadgeNotificacao(contagem);
                } else {
                    Log.e("NOTIFICACOES", "Resposta não sucedida. Código: " + response.code());
                    atualizarBadgeNotificacao(0);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("NOTIFICACOES", "Erro de rede: " + t.getMessage());
                atualizarBadgeNotificacao(0);
            }
        });
    }

    private void atualizarBadgeNotificacao(int contagem) {
        runOnUiThread(() -> {
            Log.d("NOTIFICACOES", "Atualizando badge com: " + contagem);

            if (contagem > 0) {
                badgeNotificacao.setText(String.valueOf(contagem));
                badgeNotificacao.setVisibility(View.VISIBLE);
            } else {
                // Mostra "0" em vez de esconder
                badgeNotificacao.setText("0");
                badgeNotificacao.setVisibility(View.VISIBLE);
            }
        });
    }

    private void iniciarLocationService() {
        startService(new Intent(this, LocationUpdateService.class));
        Log.d("MainActivity", "LocationUpdateService iniciado");
    }
}