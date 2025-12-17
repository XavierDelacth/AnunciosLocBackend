package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocalAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Adapters.LocalAdapterTodosLocais;
import ao.co.isptec.aplm.projetoanuncioloc.Interface.OnLocalAddedListener;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.Request.LocalRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdicionarLocalActivity extends AppCompatActivity implements OnLocalAddedListener {

    // Views principais
    private ImageView btnBackLocais;
    private EditText etSearchLocais;
    private Button btnAddLocalFixo;

    // Tabs
    private LinearLayout tabTodosLocais, tabCriadosPorSi;
    private TextView tvTabTodosLocais, tvTabCriadosPorSi;
    private View indicatorTodosLocais, indicatorCriadosPorSi;
    private LinearLayout contentTodosLocais, contentCriadosPorSi;

    // RecyclerViews
    private RecyclerView rvTodosLocais, rvCriadosPorSi;
    private TextView tvEmptyTodosLocais, tvEmptyCriadosPorSi;

    // Adapters
    private LocalAdapterTodosLocais adapterTodos;
    private LocalAdapter adapterCriados;

    // Listas de dados
    private List<Local> listaTodosLocais;  // Lista completa (incluindo criados pelo usuário)
    private List<Local> listaCriadosPorSi;  // Apenas locais criados pelo usuário
    private List<Local> listaTodosFiltrada;  // Para busca na tab "Todos"
    private List<Local> listaCriadosFiltrada;  // Para busca na tab "Criados Por Si"

    // Controle de tab ativa
    private boolean isTabTodosAtiva = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_local);

        initViews();
        setupToolbar();
        setupTabs();
        setupListasLocais();
        setupSearchFilter();
        setupAddButton();
    }

    private void initViews() {
        btnBackLocais = findViewById(R.id.btnBackLocais);
        etSearchLocais = findViewById(R.id.etSearchLocais);
        btnAddLocalFixo = findViewById(R.id.btnAddLocalFixo);

        // Tabs
        tabTodosLocais = findViewById(R.id.tabTodosLocais);
        tabCriadosPorSi = findViewById(R.id.tabCriadosPorSi);
        tvTabTodosLocais = findViewById(R.id.tvTabTodosLocais);
        tvTabCriadosPorSi = findViewById(R.id.tvTabCriadosPorSi);
        indicatorTodosLocais = findViewById(R.id.indicatorTodosLocais);
        indicatorCriadosPorSi = findViewById(R.id.indicatorCriadosPorSi);

        // Conteúdos das tabs
        contentTodosLocais = findViewById(R.id.contentTodosLocais);
        contentCriadosPorSi = findViewById(R.id.contentCriadosPorSi);

        // RecyclerViews
        rvTodosLocais = findViewById(R.id.rvTodosLocais);
        rvCriadosPorSi = findViewById(R.id.rvCriadosPorSi);
        tvEmptyTodosLocais = findViewById(R.id.tvEmptyTodosLocais);
        tvEmptyCriadosPorSi = findViewById(R.id.tvEmptyCriadosPorSi);
    }

    private void setupToolbar() {
        btnBackLocais.setOnClickListener(v -> finish());
    }

    private void setupTabs() {
        // Click na tab "Todos Locais"
        tabTodosLocais.setOnClickListener(v -> {
            if (!isTabTodosAtiva) {
                mostrarTabTodos();
            }
        });

        // Click na tab "Criados Por Si"
        tabCriadosPorSi.setOnClickListener(v -> {
            if (isTabTodosAtiva) {
                mostrarTabCriados();
            }
        });
    }

    private void mostrarTabTodos() {
        isTabTodosAtiva = true;

        // Atualiza UI das tabs
        tvTabTodosLocais.setTextColor(getColor(R.color.verde_principal));
        tvTabCriadosPorSi.setTextColor(getColor(R.color.cinza_texto));
        indicatorTodosLocais.setVisibility(View.VISIBLE);
        indicatorCriadosPorSi.setVisibility(View.GONE);

        // Mostra conteúdo correspondente
        contentTodosLocais.setVisibility(View.VISIBLE);
        contentCriadosPorSi.setVisibility(View.GONE);

        // Limpa pesquisa ao trocar de tab
        etSearchLocais.setText("");
    }

    private void mostrarTabCriados() {
        isTabTodosAtiva = false;

        // Atualiza UI das tabs
        tvTabTodosLocais.setTextColor(getColor(R.color.cinza_texto));
        tvTabCriadosPorSi.setTextColor(getColor(R.color.verde_principal));
        indicatorTodosLocais.setVisibility(View.GONE);
        indicatorCriadosPorSi.setVisibility(View.VISIBLE);

        // Mostra conteúdo correspondente
        contentTodosLocais.setVisibility(View.GONE);
        contentCriadosPorSi.setVisibility(View.VISIBLE);

        // Limpa pesquisa ao trocar de tab
        etSearchLocais.setText("");
    }

    private void setupListasLocais() {
        // Inicializa listas vazias
        listaTodosLocais = new ArrayList<>();
        listaCriadosPorSi = new ArrayList<>();
        listaTodosFiltrada = new ArrayList<>();
        listaCriadosFiltrada = new ArrayList<>();

        // Setup dos adapters primeiro (com listas vazias)
        setupAdapters();

        // Carrega dados do backend
        carregarTodosLocais();
        carregarLocaisDoUsuario();
    }

    private void setupAdapters() {
        // Setup Adapter "Todos Locais" (Read-only)
        adapterTodos = new LocalAdapterTodosLocais(listaTodosFiltrada);
        adapterTodos.setOnLocalClickListener((local, position) -> {
            retornarLocalSelecionado(local);
        });
        rvTodosLocais.setLayoutManager(new LinearLayoutManager(this));
        rvTodosLocais.setAdapter(adapterTodos);

        // Setup Adapter "Criados Por Si" (Editable)
        adapterCriados = new LocalAdapter(listaCriadosFiltrada);
        adapterCriados.setOnLocalActionListener(new LocalAdapter.OnLocalActionListener() {
            @Override
            public void onLocalClick(Local local, int position) {
                retornarLocalSelecionado(local);
            }

            @Override
            public void onEditClick(Local local, int position) {
                editarLocal(local, position);
            }

            @Override
            public void onDeleteClick(Local local, int position) {
                confirmarExclusao(local, position);
            }
        });
        rvCriadosPorSi.setLayoutManager(new LinearLayoutManager(this));
        rvCriadosPorSi.setAdapter(adapterCriados);
    }

    private void carregarTodosLocais() {
        Call<List<Local>> call = RetrofitClient.getApiService(this).getTodosLocais();
        call.enqueue(new Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaTodosLocais.clear();
                    listaTodosLocais.addAll(response.body());
                    listaTodosFiltrada.clear();
                    listaTodosFiltrada.addAll(listaTodosLocais);
                    adapterTodos.notifyDataSetChanged();
                    atualizarVisibilidades();

                    Log.d("LOCAIS_DEBUG", "Carregados " + listaTodosLocais.size() + " locais do servidor");
                } else {
                    Toast.makeText(AdicionarLocalActivity.this, "Erro ao carregar locais: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("LOCAIS_DEBUG", "Erro response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                Toast.makeText(AdicionarLocalActivity.this, "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LOCAIS_DEBUG", "Falha: " + t.getMessage());
            }
        });
    }

    private void carregarLocaisDoUsuario() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);

        if (userId == -1L) {
            Toast.makeText(this, "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<Local>> call = RetrofitClient.getApiService(this).getLocaisDoUsuario(userId);
        call.enqueue(new Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCriadosPorSi.clear();
                    listaCriadosPorSi.addAll(response.body());
                    listaCriadosFiltrada.clear();
                    listaCriadosFiltrada.addAll(listaCriadosPorSi);
                    adapterCriados.notifyDataSetChanged();
                    atualizarVisibilidades();

                    Log.d("LOCAIS_DEBUG", "Carregados " + listaCriadosPorSi.size() + " locais do usuário");
                } else {
                    Toast.makeText(AdicionarLocalActivity.this, "Erro ao carregar seus locais: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                Toast.makeText(AdicionarLocalActivity.this, "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearchFilter() {
        etSearchLocais.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Pequeno delay para evitar muitas requisições
                etSearchLocais.postDelayed(() -> {
                    if (query.equals(etSearchLocais.getText().toString().trim())) {
                        if (isTabTodosAtiva) {
                            if (query.isEmpty()) {
                                // Se query vazia, mostra todos os locais
                                listaTodosFiltrada.clear();
                                listaTodosFiltrada.addAll(listaTodosLocais);
                                adapterTodos.notifyDataSetChanged();
                            } else {
                                // Faz pesquisa no backend
                                pesquisarLocais(query);
                            }
                        } else {
                            // Para "Criados Por Si", filtra localmente
                            filtrarLocaisCriadosLocalmente(query);
                        }
                        atualizarVisibilidades();
                    }
                }, 300); // 300ms delay
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void pesquisarLocais(String query) {
        Call<List<Local>> call = RetrofitClient.getApiService(this).searchLocais(query);
        call.enqueue(new Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaTodosFiltrada.clear();
                    listaTodosFiltrada.addAll(response.body());
                    adapterTodos.notifyDataSetChanged();
                    atualizarVisibilidades();

                    Log.d("LOCAIS_DEBUG", "Pesquisa '" + query + "' encontrou " + listaTodosFiltrada.size() + " resultados");
                } else {
                    Toast.makeText(AdicionarLocalActivity.this, "Erro na pesquisa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                Toast.makeText(AdicionarLocalActivity.this, "Falha na pesquisa: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarLocaisCriadosLocalmente(String query) {
        listaCriadosFiltrada.clear();
        if (query.isEmpty()) {
            listaCriadosFiltrada.addAll(listaCriadosPorSi);
        } else {
            for (Local local : listaCriadosPorSi) {
                if (local.getNome().toLowerCase().contains(query.toLowerCase())) {
                    listaCriadosFiltrada.add(local);
                }
            }
        }
        adapterCriados.notifyDataSetChanged();
    }




    private void retornarLocalSelecionado(Local local) {
        Intent result = new Intent();
        result.putExtra("localSelecionado", local.getNome());
        result.putExtra("latitude", local.getLatitude());
        result.putExtra("longitude", local.getLongitude());
        result.putExtra("raio", local.getRaio());
        result.putExtra("localId", local.getId());
        setResult(RESULT_OK, result);
        finish();
    }

    private void atualizarVisibilidades() {
        // Tab "Todos Locais"
        if (listaTodosFiltrada.isEmpty()) {
            rvTodosLocais.setVisibility(View.GONE);
            tvEmptyTodosLocais.setVisibility(View.VISIBLE);
            tvEmptyTodosLocais.setText(etSearchLocais.getText().toString().isEmpty()
                    ? "Nenhum local disponível"
                    : "Nenhum local encontrado");
        } else {
            rvTodosLocais.setVisibility(View.VISIBLE);
            tvEmptyTodosLocais.setVisibility(View.GONE);
        }

        // Tab "Criados Por Si"
        if (listaCriadosFiltrada.isEmpty()) {
            rvCriadosPorSi.setVisibility(View.GONE);
            tvEmptyCriadosPorSi.setVisibility(View.VISIBLE);
            tvEmptyCriadosPorSi.setText(etSearchLocais.getText().toString().isEmpty()
                    ? "Você ainda não criou nenhum local"
                    : "Nenhum local encontrado");
        } else {
            rvCriadosPorSi.setVisibility(View.VISIBLE);
            tvEmptyCriadosPorSi.setVisibility(View.GONE);
        }
    }

    private void setupAddButton() {
        btnAddLocalFixo.setOnClickListener(v -> {
            AdicionarGPSDialog gpsDialog = AdicionarGPSDialog.newInstance(this);
            gpsDialog.show(getSupportFragmentManager(), "AdicionarGPSDialog");
        });
    }

    // EDITAR LOCAL
    private void editarLocal(Local local, int position) {
        boolean isWifi = local.getWifiIds() != null && !local.getWifiIds().isEmpty();

        if (isWifi) {
            // Abre dialog WiFi em modo edição
            editarLocalWiFi(local, position);
        } else {
            // Abre dialog GPS em modo edição
            editarLocalGPS(local, position);
        }
    }

    private void editarLocalGPS(Local local, int position) {
        EditarGPSDialog dialog = EditarGPSDialog.newInstance(local, (nomeEditado, latEditada, lngEditada, raioEditado) -> {
            // Criar request para atualização
            LocalRequest request = new LocalRequest(
                    nomeEditado,
                    "GPS",
                    latEditada,
                    lngEditada,
                    raioEditado,
                    null
            );

            // Chamar API para atualizar
            Call<Local> call = RetrofitClient.getApiService(this).atualizarLocal(local.getId(), request);
            call.enqueue(new Callback<Local>() {
                @Override
                public void onResponse(Call<Local> call, Response<Local> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Atualiza o local na lista
                        local.setNome(nomeEditado);
                        local.setLatitude(latEditada);
                        local.setLongitude(lngEditada);
                        local.setRaio(raioEditado);

                        // Notifica os adapters
                        adapterCriados.notifyItemChanged(position);
                        int posicaoTodos = listaTodosLocais.indexOf(local);
                        if (posicaoTodos != -1) {
                            adapterTodos.notifyItemChanged(posicaoTodos);
                        }

                        Toast.makeText(AdicionarLocalActivity.this, "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdicionarLocalActivity.this, "Erro ao atualizar local", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Local> call, Throwable t) {
                    Toast.makeText(AdicionarLocalActivity.this, "Falha na rede", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show(getSupportFragmentManager(), "EditarGPSDialog");
    }

    private void editarLocalWiFi(Local local, int position) {
        EditarWiFiDialog dialog = EditarWiFiDialog.newInstance(local, (nomeEditado, ssidEditado) -> {
            // Criar request para atualização
            LocalRequest request = new LocalRequest(
                    nomeEditado,
                    "WIFI",
                    null,
                    null,
                    null,
                    Arrays.asList(ssidEditado)
            );

            // Chamar API para atualizar
            Call<Local> call = RetrofitClient.getApiService(this).atualizarLocal(local.getId(), request);
            call.enqueue(new Callback<Local>() {
                @Override
                public void onResponse(Call<Local> call, Response<Local> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Atualiza o local na lista
                        local.setNome(nomeEditado);
                        local.setWifiIds(Arrays.asList(ssidEditado));

                        // Notifica os adapters
                        adapterCriados.notifyItemChanged(position);
                        int posicaoTodos = listaTodosLocais.indexOf(local);
                        if (posicaoTodos != -1) {
                            adapterTodos.notifyItemChanged(posicaoTodos);
                        }

                        Toast.makeText(AdicionarLocalActivity.this, "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdicionarLocalActivity.this, "Erro ao atualizar local", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Local> call, Throwable t) {
                    Toast.makeText(AdicionarLocalActivity.this, "Falha na rede", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show(getSupportFragmentManager(), "EditarWiFiDialog");
    }


    // EXCLUIR LOCAL
    private void confirmarExclusao(Local local, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Local")
                .setMessage("Tem certeza que deseja excluir \"" + local.getNome() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    excluirLocal(local, position);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // EXCLUIR LOCAL - COM BACKEND
    private void excluirLocal(Local local, int position) {
        Call<Void> call = RetrofitClient.getApiService(this).excluirLocal(local.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Remove das listas locais
                    listaCriadosPorSi.remove(local);
                    listaCriadosFiltrada.remove(local);
                    adapterCriados.notifyItemRemoved(position);

                    // Remove também da lista de todos locais
                    int posicaoTodos = listaTodosLocais.indexOf(local);
                    if (posicaoTodos != -1) {
                        listaTodosLocais.remove(posicaoTodos);
                        listaTodosFiltrada.remove(local);
                        adapterTodos.notifyItemRemoved(posicaoTodos);
                    }

                    atualizarVisibilidades();
                    Toast.makeText(AdicionarLocalActivity.this, "Local excluído com sucesso!", Toast.LENGTH_SHORT).show();

                    Log.d("LOCAIS_DEBUG", "Local excluído: " + local.getNome());
                } else {
                    Toast.makeText(AdicionarLocalActivity.this, "Erro ao excluir local: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e("LOCAIS_DEBUG", "Erro ao excluir: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AdicionarLocalActivity.this, "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LOCAIS_DEBUG", "Falha ao excluir: " + t.getMessage());
            }
        });
    }

    // CALLBACKS DA INTERFACE OnLocalAddedListener
    @Override
    public void onLocalAddedGPS(String nome, double lat, double lng, int raio) {
        // Recarrega a lista de locais do usuário após adicionar
        carregarLocaisDoUsuario();
        // Também recarrega todos os locais para incluir o novo
        carregarTodosLocais();

        Toast.makeText(this, "Local GPS adicionado com sucesso!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onLocalAddedWiFi(String nome, List<String> ssids) {
        // Recarrega a lista de locais do usuário após adicionar
        carregarLocaisDoUsuario();
        // Também recarrega todos os locais para incluir o novo
        carregarTodosLocais();

        Toast.makeText(this, "Local WiFi adicionado com sucesso!", Toast.LENGTH_SHORT).show();
    }
}