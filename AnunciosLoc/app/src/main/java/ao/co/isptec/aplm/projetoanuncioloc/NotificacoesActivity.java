package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.NotificacaoAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificacoesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificacaoAdapter adapter;
    private List<Notificacao> listaNotificacoes = new ArrayList<>();
    private Button btnClearNotifications;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacao);

        recyclerView = findViewById(R.id.recyclerNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnClearNotifications = findViewById(R.id.btnClearNotifications);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnClearNotifications.setOnClickListener(v -> limparNotificacoes());

        ImageView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> {
            finish(); // volta para trás
        });

        // Configurar o adapter com o listener de guardar
        adapter = new NotificacaoAdapter(this, listaNotificacoes, new NotificacaoAdapter.OnSaveClickListener() {
            @Override
            public void onSaveClick(Notificacao notificacao, int position) {
                guardarAnuncio(notificacao, position);
            }
        });
        recyclerView.setAdapter(adapter);

        carregarNotificacoes();
    }

    private void carregarNotificacoes() {
        tvEmpty.setText("Carregando...");
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Toast.makeText(this, "Erro: usuário não encontrado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RetrofitClient.getApiService(this).getNotificacoes(userId).enqueue(new Callback<List<Notificacao>>() {
            @Override
            public void onResponse(Call<List<Notificacao>> call, Response<List<Notificacao>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    tvEmpty.setText("Nenhuma notificação encontrada");
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }

                List<Notificacao> notificacoesApi = response.body();

                // Buscar anúncios do próprio utilizador para filtrar
                RetrofitClient.getApiService(NotificacoesActivity.this)
                        .getMeusAnuncios(userId)
                        .enqueue(new Callback<List<AnuncioResponse>>() {
                            @Override
                            public void onResponse(Call<List<AnuncioResponse>> callMeus,
                                                   Response<List<AnuncioResponse>> responseMeus) {
                                Set<Long> meusAnunciosIds = new HashSet<>();
                                if (responseMeus.isSuccessful() && responseMeus.body() != null) {
                                    for (AnuncioResponse ar : responseMeus.body()) {
                                        if (ar != null && ar.getId() != null) {
                                            meusAnunciosIds.add(ar.getId());
                                        }
                                    }
                                }
                                aplicarFiltros(notificacoesApi, meusAnunciosIds);
                            }

                            @Override
                            public void onFailure(Call<List<AnuncioResponse>> callMeus, Throwable tMeus) {
                                // Se falhar, ainda aplica deduplicação básica
                                aplicarFiltros(notificacoesApi, null);
                            }
                        });
            }

            @Override
            public void onFailure(Call<List<Notificacao>> call, Throwable t) {
                tvEmpty.setText("Sem conexão");
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Toast.makeText(NotificacoesActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Aplica filtros:
     * 1. Remove notificações de anúncios criados pelo próprio utilizador
     * 2. Remove duplicados (mesmo anuncioId aparece apenas uma vez)
     */
    private void aplicarFiltros(List<Notificacao> notificacoesApi, Set<Long> meusAnunciosIds) {
        listaNotificacoes.clear();
        Set<Long> anunciosVistos = new HashSet<>();

        if (notificacoesApi != null) {
            for (Notificacao n : notificacoesApi) {
                if (n == null || n.getAnuncioId() == null) continue;

                // 1. Filtrar notificações de anúncios próprios
                if (meusAnunciosIds != null && meusAnunciosIds.contains(n.getAnuncioId())) {
                    continue; // Ignora notificação do próprio anúncio
                }

                // 2. Deduplicação: apenas uma notificação por anuncioId
                if (anunciosVistos.contains(n.getAnuncioId())) {
                    continue; // Já existe uma notificação deste anúncio
                }

                anunciosVistos.add(n.getAnuncioId());
                listaNotificacoes.add(n);
            }
        }

        adapter.notifyDataSetChanged();

        Toast.makeText(NotificacoesActivity.this,
                "Você tem " + listaNotificacoes.size() + " notificação" +
                        (listaNotificacoes.size() != 1 ? "s" : ""), Toast.LENGTH_LONG).show();

        atualizarVisibilidade();
    }

    private void guardarAnuncio(Notificacao notificacao, int position) {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);

        // Usa o anuncioId da notificação
        Long anuncioId = notificacao.getAnuncioId();

        if (userId == -1L || anuncioId == null) {
            Toast.makeText(this, "Erro: dados insuficientes para guardar anúncio", Toast.LENGTH_SHORT).show();
            // Reverter o ícone em caso de erro
            adapter.updateItemSavedState(position, false);
            return;
        }

        // Chamar API para guardar o anúncio
        RetrofitClient.getApiService(this).guardarAnuncio(userId, anuncioId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(NotificacoesActivity.this, "Anúncio guardado com sucesso!", Toast.LENGTH_SHORT).show();
                    // Atualizar o ícone na lista
                    adapter.updateItemSavedState(position, true);
                } else {
                    Toast.makeText(NotificacoesActivity.this, "Erro ao guardar anúncio", Toast.LENGTH_SHORT).show();
                    // Reverter o ícone em caso de erro
                    adapter.updateItemSavedState(position, false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NotificacoesActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Reverter o ícone em caso de erro
                adapter.updateItemSavedState(position, false);
            }
        });
    }

    private void limparNotificacoes() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);

        if (userId == -1L) {
            Toast.makeText(this, "Erro: usuário não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getApiService(this).limparNotificacoes(userId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    listaNotificacoes.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(NotificacoesActivity.this, "Notificações limpas!", Toast.LENGTH_SHORT).show();
                    atualizarVisibilidade();
                } else {
                    Toast.makeText(NotificacoesActivity.this, "Erro ao limpar notificações", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(NotificacoesActivity.this, "Erro de rede ao limpar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarVisibilidade() {
        if (listaNotificacoes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Nenhuma notificação");
            btnClearNotifications.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            btnClearNotifications.setVisibility(View.VISIBLE);
        }
    }
}