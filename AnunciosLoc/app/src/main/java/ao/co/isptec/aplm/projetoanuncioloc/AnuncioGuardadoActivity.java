package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.AnuncioAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse;
import ao.co.isptec.aplm.projetoanuncioloc.Service.ApiService;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnuncioGuardadoActivity extends AppCompatActivity implements AnuncioAdapter.OnBookmarkClickListener {

    private CardView cardLocais, cardAnuncios, cardTabs;
    private TextView tabCriados, tabGuardados, tvLocation, tvEmptyGuardados;
    private ImageView ivClear, btnProfile, btnNotification;
    private EditText etSearch;
    private RecyclerView recyclerView;
    private AnuncioAdapter adapter;
    private List<Anuncio> listaAnunciosGuardados;
    private TextView badgeNotificacao;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardado);

        initViews();
        setupClickListeners();
        setupTabs();
        setupSearch();
        selectTab(false);

        // Inicializa lista vazia
        listaAnunciosGuardados = new ArrayList<>();
        setupRecyclerView();

        // Carrega anúncios guardados da API
        carregarAnunciosGuardados();
        carregarContagemNotificacoes();

        // Localização atual
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        obterLocalizacaoAtual();

        // Botão voltar / Back Gesture
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(AnuncioGuardadoActivity.this, MainActivity.class);
                intent.putExtra("fromGuardados", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initViews() {
        cardLocais = findViewById(R.id.cardLocais);
        cardAnuncios = findViewById(R.id.cardAnuncios);
        cardTabs = findViewById(R.id.cardTabs);

        tabCriados = findViewById(R.id.tabCriados);
        tabGuardados = findViewById(R.id.tabGuardados);

        tvLocation = findViewById(R.id.tvLocation);
        tvEmptyGuardados = findViewById(R.id.tvEmptyGuardados);

        etSearch = findViewById(R.id.etSearch);
        ivClear = findViewById(R.id.ivClear);
        recyclerView = findViewById(R.id.recyclerView);

        btnProfile = findViewById(R.id.btnProfile);
        btnNotification = findViewById(R.id.btnNotification);
        badgeNotificacao = findViewById(R.id.badgeNotificacao);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnuncioAdapter(this, listaAnunciosGuardados, this);
        recyclerView.setAdapter(adapter);
    }

    private void carregarAnunciosGuardados() {
        Long usuarioId = getUserIdFromSharedPreferences();
        if (usuarioId == -1) {
            Toast.makeText(this, "Usuário não identificado", Toast.LENGTH_SHORT).show();
            atualizarVisibilidade();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(this);
        Call<List<AnuncioResponse>> call = apiService.listarAnunciosGuardados(usuarioId);

        call.enqueue(new Callback<List<AnuncioResponse>>() {
            @Override
            public void onResponse(Call<List<AnuncioResponse>> call, Response<List<AnuncioResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaAnunciosGuardados.clear();
                    for (AnuncioResponse anuncioResponse : response.body()) {
                        listaAnunciosGuardados.add(anuncioResponse.toAnuncio());
                    }
                    adapter.notifyDataSetChanged();
                    atualizarVisibilidade();
                } else {
                    Toast.makeText(AnuncioGuardadoActivity.this, "Erro ao carregar anúncios guardados", Toast.LENGTH_SHORT).show();
                    atualizarVisibilidade();
                }
            }

            @Override
            public void onFailure(Call<List<AnuncioResponse>> call, Throwable t) {
                Toast.makeText(AnuncioGuardadoActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                atualizarVisibilidade();
            }
        });
    }

    // Implementação do OnBookmarkClickListener
    @Override
    public void onBookmarkClick(Anuncio anuncio, int position) {
        removerAnuncioGuardado(anuncio, position);
    }

    private void removerAnuncioGuardado(Anuncio anuncio, int position) {
        Long usuarioId = getUserIdFromSharedPreferences();
        if (usuarioId == -1) {
            Toast.makeText(this, "Usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (anuncio.id == null) {
            Toast.makeText(this, "Anúncio sem ID", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(this);
        Call<Void> call = apiService.removerAnuncioGuardado(usuarioId, anuncio.id);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Remove da lista local
                    listaAnunciosGuardados.remove(position);
                    adapter.notifyItemRemoved(position);
                    atualizarVisibilidade();
                    Toast.makeText(AnuncioGuardadoActivity.this, "Anúncio removido dos guardados", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AnuncioGuardadoActivity.this, "Erro ao remover anúncio", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AnuncioGuardadoActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Long getUserIdFromSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        return prefs.getLong("userId", -1);
    }

    private void setupClickListeners() {
        cardLocais.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarLocalActivity.class)));

        cardAnuncios.setOnClickListener(v ->
                startActivity(new Intent(this, AdicionarAnunciosActivity.class)));

        btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, PerfilActivity.class)));

        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificacoesActivity.class)));

        ivClear.setOnClickListener(v -> etSearch.setText(""));

        // Tab "Criados" volta para MainActivity mantendo estado
        tabCriados.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fromGuardados", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void setupTabs() {
        tabGuardados.setOnClickListener(v -> selectTab(false));
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

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filtrarAnuncios(s.toString());
            }
        });
    }

    private void filtrarAnuncios(String query) {
        if (adapter == null) return;

        List<Anuncio> listaFiltrada = new ArrayList<>();

        if (query.isEmpty()) {
            listaFiltrada.addAll(listaAnunciosGuardados);
        } else {
            String queryLower = query.toLowerCase();
            for (Anuncio anuncio : listaAnunciosGuardados) {
                if (anuncio.titulo.toLowerCase().contains(queryLower) ||
                        anuncio.descricao.toLowerCase().contains(queryLower) ||
                        anuncio.local.toLowerCase().contains(queryLower)) {
                    listaFiltrada.add(anuncio);
                }
            }
        }

        // Atualiza adapter com lista filtrada
        adapter = new AnuncioAdapter(this, listaFiltrada, this);
        recyclerView.setAdapter(adapter);

        // Atualiza visibilidade
        if (listaFiltrada.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyGuardados.setVisibility(View.VISIBLE);
            if (!query.isEmpty()) {
                tvEmptyGuardados.setText("Nenhum anúncio encontrado");
            } else {
                tvEmptyGuardados.setText("Nenhum anúncio guardado");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyGuardados.setVisibility(View.GONE);
        }
    }

    private void atualizarVisibilidade() {
        if (listaAnunciosGuardados.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyGuardados.setVisibility(View.VISIBLE);
            tvEmptyGuardados.setText("Nenhum anúncio guardado");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyGuardados.setVisibility(View.GONE);
        }
    }

    private void obterLocalizacaoAtual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(this, location -> {
            if (location != null) {
                try {
                    Geocoder geocoder = new Geocoder(AnuncioGuardadoActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            1);

                    if (addresses != null && !addresses.isEmpty()) {
                        String cidade = addresses.get(0).getLocality();
                        String pais = addresses.get(0).getCountryName();

                        String texto = "";
                        if (cidade != null) texto += " " + cidade;
                        //if (pais != null) texto += ", " + pais;

                        tvLocation.setText(texto.trim());
                    }
                } catch (IOException e) {
                    tvLocation.setText("Erro ao obter localização.");
                }
            } else {
                tvLocation.setText("Localização não disponível.");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obterLocalizacaoAtual();
            } else {
                tvLocation.setText("Permissão de localização negada.");
            }
        }
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
}