package ao.co.isptec.aplm.projetoanuncioloc;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.ProfileKeyAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;
import ao.co.isptec.aplm.projetoanuncioloc.Service.ApiService;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdicionarAnunciosActivity extends AppCompatActivity implements AdicionarKeyDialog.OnKeyAddedListener {

    private static final String TAG = "AActivity";
    public static final int REQUEST_CODE_EDIT = 1001;

    // Modo da activity (criar ou editar)
    private boolean modoEdicao = false;
    private Anuncio anuncioParaEditar = null;
    private int posicaoAnuncio = -1;

    // Views do layout
    private ImageView btnBack, btnAddLocation, btnAdicionarChave;
    private TextView tvDataInicio, tvDataFim, tvHoraInicio, tvHoraFim, tvTipoRestricao,
            tvModoEntrega, tvLocalSelecionado, tvTituloTela;
    private EditText etTitulo, etMensagem, etPesquisarChaves;
    private LinearLayout llLocal, llDataInicio, llDataFim, llHoraInicio, llHoraFim,
            llTipoRestricao, llModoEntrega, llImagem, layoutEmptyChaves;
    private Spinner spinnerRestricao, spinnerModoEntrega, spinnerLocais;
    private CardView cardChavesContainer;
    private Button btnPublicar;
    private RecyclerView rvChavesRestricoes;
    private ProfileKeyAdapter keyAdapter;
    private String localSelecionado = null;
    private String caminhoImagem = null;

    // Para restrições (chaves públicas)
    private Map<String, List<String>> restricoesPerfil = new HashMap<>();
    private List<ProfileKey> allKeys;
    private List<ProfileKey> chavesFiltradas = new ArrayList<>();

    // Launchers
    private final ActivityResultLauncher<Intent> localLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Local selecionado retornado");
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String nome = result.getData().getStringExtra("nome_local");
                    String tipo = result.getData().getStringExtra("tipo");
                    if (nome != null && !nome.isEmpty()) {
                        localSelecionado = nome;
                        tvLocalSelecionado.setText(nome);
                        tvLocalSelecionado.setTextColor(getColor(android.R.color.black));
                        Toast.makeText(this, tipo + " adicionado: " + nome, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Local atualizado: " + nome);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> imagemLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Resultado da galeria: " + result.getResultCode());

                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        Log.d(TAG, "URI da imagem: " + uri);

                        // Chamar método corrigido
                        carregarImagemComGlideCorrigido(uri);
                    }
                }
            });

    private String salvarImagemTemporaria(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Criar arquivo temporário
            File tempFile = File.createTempFile("temp_img_", ".jpg", getCacheDir());
            tempFile.deleteOnExit();

            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4 * 1024];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return tempFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar imagem temporária: " + e.getMessage());
            return null;
        }
    }

    private boolean imagemFoiAlterada = false;
    private void carregarImagemComGlideCorrigido(Uri uri) {
        ImageView ivPreview = findViewById(R.id.ivPreviewImagem);
        TextView tvHint = findViewById(R.id.tvImagemHint);

        if (ivPreview == null) {
            Log.e(TAG, "ImageView não encontrado!");
            return;
        }

        // Salvar arquivo temporário e guardar o caminho REAL
        String tempPath = salvarImagemTemporaria(uri);
        if (tempPath != null) {
            caminhoImagem = tempPath;
            Log.d(TAG, "Imagem salva em: " + caminhoImagem);
        } else {
            // Fallback: usar URI (não ideal para upload)
            caminhoImagem = uri.toString();
        }

        runOnUiThread(() -> {
            if (tvHint != null) {
                tvHint.setText("Imagem selecionada");
            }

            // Remover qualquer imagem/source anterior
            ivPreview.setImageTintList(null);
            ivPreview.setImageTintMode(null);
            ivPreview.clearColorFilter();
            ivPreview.setBackgroundColor(Color.TRANSPARENT);

            // Carregar com Glide
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_close)
                    .apply(RequestOptions.centerCropTransform())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                    .into(ivPreview);

            imagemFoiAlterada = true;
            Toast.makeText(this, "Imagem adicionada!", Toast.LENGTH_SHORT).show();
        });
    }

    private void carregarImagemSemGlide(Uri uri, ImageView imageView) {
        try {
            // Método 1: Usar BitmapFactory
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                Log.d(TAG, " Carregado com BitmapFactory");
            } else {
                // Método 2: Usar setImageURI (como fallback)
                imageView.setImageURI(uri);
                Log.d(TAG, "Carregado com setImageURI");
            }
            if (inputStream != null) inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, " Todos os métodos falharam: " + e.getMessage());
            imageView.setImageResource(R.drawable.ic_close);
        }
    }

    private void solicitarPermissaoImagem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ precisa de READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        100);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-12 precisa de READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        100);
            }
        }
    }

    // Adicionar este método para tratar a resposta da permissão
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permissão concedida para ler imagens");
            } else {
                Log.w(TAG, "Permissão negada para ler imagens");
                Toast.makeText(this, "Permissão necessária para selecionar imagens", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate chamado - Activity iniciada");
        setContentView(R.layout.activity_criar_anuncios);

        // LOG ADICIONAL para verificar imediatamente
        Log.d(TAG, "Após verificarModoEdicao(): modoEdicao=" + modoEdicao +
                ", anuncioParaEditar=" + (anuncioParaEditar != null ? "presente" : "null"));
        solicitarPermissaoImagem();

        initViews();
        if (!initViewsSucesso) {
            Toast.makeText(this, "Erro ao carregar layout. Verifica XML.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initRestricoes();
        setupSpinner();
        setupSpinnerModoEntrega();
        setupSpinnerLocais();
        setupClickListeners();
        atualizarVisibilidadeChaves();

        // Verifica se está em modo edição
        verificarModoEdicao();

        Log.d(TAG, "onCreate concluído");
    }

    private void verificarModoEdicao() {
        Intent intent = getIntent();

        Log.d(TAG, "=== VERIFICANDO MODO EDIÇÃO ===");

        // Verificar TODOS os extras disponíveis
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(TAG, "Quantidade de extras: " + extras.size());
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, "Extra [" + key + "]: " +
                        (value != null ? value.toString().substring(0, Math.min(50, value.toString().length())) : "null"));
            }
        }

        // Verificar modo edição
        if ((intent.hasExtra("modo_edicao") && intent.getBooleanExtra("modo_edicao", false)) ||
                (intent.hasExtra("MODO_EDICAO") && intent.getBooleanExtra("MODO_EDICAO", false))) {

            modoEdicao = true;
            posicaoAnuncio = intent.getIntExtra("position", -1);
            if (posicaoAnuncio == -1) {
                posicaoAnuncio = intent.getIntExtra("POSICAO", -1);  // ← ADICIONAR ESTA LINHA
            }

            Log.d(TAG, "Modo EDIÇÃO ativado");

            // ORREÇÃO: Tentar TODOS os nomes possíveis
            anuncioParaEditar = intent.getParcelableExtra("ANUNCIO_PARA_EDITAR");  // ← NOME CORRETO!

            if (anuncioParaEditar == null) {
                anuncioParaEditar = intent.getParcelableExtra("anuncio");
            }

            if (anuncioParaEditar == null) {
                anuncioParaEditar = intent.getParcelableExtra("ANUNCIO");
            }


            if (anuncioParaEditar == null) {
                Log.e(TAG, "ERRO CRÍTICO: Não foi possível obter dados do anúncio!");
                Toast.makeText(this, "Erro: Não foi possível carregar o anúncio para edição", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Log.d(TAG, "Anúncio carregado com sucesso!");
                Log.d(TAG, "  Título: " + anuncioParaEditar.titulo);
                Log.d(TAG, "  ID: " + anuncioParaEditar.id);
                Log.d(TAG, "  Descrição: " + (anuncioParaEditar.descricao != null ?
                        anuncioParaEditar.descricao.substring(0, Math.min(30, anuncioParaEditar.descricao.length())) : "null"));
                preencherDadosParaEdicao();
            }

        } else {
            modoEdicao = false;
            Log.d(TAG, "Modo CRIAÇÃO detectado");
        }
    }

    private void preencherDadosParaEdicao() {
        Log.d(TAG, "=== INICIANDO preencherDadosParaEdicao ===");

        try {
            // Proteção contra NULL
            if (anuncioParaEditar == null) {
                Log.e(TAG, "ERRO: anuncioParaEditar é NULL!");
                Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Muda título da tela e texto do botão
            tvTituloTela.setText("Editar Anúncio");
            btnPublicar.setText("Atualizar Anúncio");

            Log.d(TAG, "Anúncio não é null, continuando...");

            // Título da tela
            Log.d(TAG, "Configurando título da tela...");
            if (tvTituloTela != null) {
                tvTituloTela.setText("Editar Anúncio");
            }

            // Botão
            Log.d(TAG, "Configurando botão publicar...");
            if (btnPublicar != null) {
                btnPublicar.setText("Salvar Alterações");
            }

            // Campos básicos
            Log.d(TAG, "Preenchendo título: " + anuncioParaEditar.titulo);
            if (etTitulo != null && anuncioParaEditar.titulo != null) {
                etTitulo.setText(anuncioParaEditar.titulo);
            }

            Log.d(TAG, "Preenchendo descrição: " +
                    (anuncioParaEditar.descricao != null ?
                            anuncioParaEditar.descricao.substring(0, Math.min(20, anuncioParaEditar.descricao.length())) : "null"));
            if (etMensagem != null && anuncioParaEditar.descricao != null) {
                etMensagem.setText(anuncioParaEditar.descricao);
            }

            // Local
            Log.d(TAG, "Preenchendo local: " + anuncioParaEditar.local);
            if (anuncioParaEditar.local != null && !anuncioParaEditar.local.isEmpty()) {
                localSelecionado = anuncioParaEditar.local;
                if (tvLocalSelecionado != null) {
                    tvLocalSelecionado.setText(localSelecionado);
                    tvLocalSelecionado.setTextColor(getColor(android.R.color.black));
                }
                selecionarNoSpinnerLocais(localSelecionado);
            }

            // Imagem
            Log.d(TAG, "Processando imagem: " + anuncioParaEditar.imagem);
            // === CARREGAR IMAGEM NO MODO EDIÇÃO (igual ao AnuncioAdapter) ===
            ImageView ivPreview = findViewById(R.id.ivPreviewImagem);
            TextView tvHint = findViewById(R.id.tvImagemHint);

            if (anuncioParaEditar.imagem != null && !anuncioParaEditar.imagem.isEmpty()) {
                caminhoImagem = anuncioParaEditar.imagem; // Guarda para não perder ao atualizar

                String urlImagem = anuncioParaEditar.imagem;
                String fullUrl;

                // === MESMA LÓGICA DO AnuncioAdapter.java ===
                if (urlImagem.startsWith("http://") || urlImagem.startsWith("https://")) {
                    fullUrl = urlImagem;
                } else {
                    String baseUrl = RetrofitClient.BASE_URL;
                    if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                    String path = urlImagem.startsWith("/") ? urlImagem : "/" + urlImagem;
                    fullUrl = baseUrl + path;
                }

                Log.d(TAG, "Carregando imagem do anúncio (edição): " + fullUrl);

                // Remove o tint/cinza ANTES de carregar (essencial!)
                ivPreview.setImageTintList(null);
                ivPreview.setImageTintMode(null);
                ivPreview.clearColorFilter();
                ivPreview.setBackgroundColor(Color.TRANSPARENT);

                Glide.with(this)
                        .load(fullUrl)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.espaco_image)  // Usa o mesmo do AnuncioAdapter
                                .error(R.drawable.espaco_image)
                                .transform(new RoundedCorners(24)))
                        .into(ivPreview);

                tvHint.setText("Toque para alterar a imagem");

            } else {
                // Sem imagem
                ivPreview.setImageResource(R.drawable.espaco_image);
                ivPreview.setImageTintList(null);  // Remove tint mesmo no placeholder
                tvHint.setText("Toque para adicionar uma imagem");
            }

            // Datas
            Log.d(TAG, "Preenchendo data início: " + anuncioParaEditar.dataInicio);
            if (tvDataInicio != null && anuncioParaEditar.dataInicio != null) {
                tvDataInicio.setText(anuncioParaEditar.dataInicio);
                tvDataInicio.setTextColor(getColor(android.R.color.black));
            }

            Log.d(TAG, "Preenchendo data fim: " + anuncioParaEditar.dataFim);
            if (tvDataFim != null && anuncioParaEditar.dataFim != null) {
                tvDataFim.setText(anuncioParaEditar.dataFim);
                tvDataFim.setTextColor(getColor(android.R.color.black));
            }

            // Horas
            Log.d(TAG, "Preenchendo hora início: " + anuncioParaEditar.horaInicio);
            if (tvHoraInicio != null && anuncioParaEditar.horaInicio != null) {
                tvHoraInicio.setText(anuncioParaEditar.horaInicio);
                tvHoraInicio.setTextColor(getColor(android.R.color.black));
            }

            Log.d(TAG, "Prenchendo hora fim: " + anuncioParaEditar.horaFim);
            if (tvHoraFim != null && anuncioParaEditar.horaFim != null) {
                tvHoraFim.setText(anuncioParaEditar.horaFim);
                tvHoraFim.setTextColor(getColor(android.R.color.black));
            }

            // Tipo de restrição
            Log.d(TAG, "Preenchendo tipo restrição: " + anuncioParaEditar.tipoRestricao);
            if (anuncioParaEditar.tipoRestricao != null) {
                if (tvTipoRestricao != null) {
                    tvTipoRestricao.setText(anuncioParaEditar.tipoRestricao);
                }
                selecionarNoSpinnerRestricao(anuncioParaEditar.tipoRestricao);
            }

            // Modo de entrega
            Log.d(TAG, "Preenchendo modo entrega: " + anuncioParaEditar.modoEntrega);
            if (anuncioParaEditar.modoEntrega != null) {
                if (tvModoEntrega != null) {
                    tvModoEntrega.setText(anuncioParaEditar.modoEntrega);
                }
                selecionarNoSpinnerModoEntrega(anuncioParaEditar.modoEntrega);
            }

            // Chaves de restrição
            Log.d(TAG, "Processando chaves de perfil...");
            if (anuncioParaEditar.getChavesPerfil() != null && !anuncioParaEditar.getChavesPerfil().isEmpty()) {
                Log.d(TAG, "    Encontradas " + anuncioParaEditar.getChavesPerfil().size() + " chaves");

                restricoesPerfil.clear();
                restricoesPerfil.putAll(anuncioParaEditar.getChavesPerfil());

                // Marca os valores selecionados nas chaves
                for (Map.Entry<String, List<String>> entry : restricoesPerfil.entrySet()) {
                    String keyName = entry.getKey();
                    List<String> selectedValues = entry.getValue();
                    Log.d(TAG, "    Processando chave: " + keyName + " com " + selectedValues.size() + " valores");

                    for (ProfileKey key : allKeys) {
                        if (key.getName().equals(keyName)) {
                            for (String value : selectedValues) {
                                key.toggleValue(value);
                            }
                            break;
                        }
                    }
                }

                if (keyAdapter != null) {
                    keyAdapter.notifyDataSetChanged();
                }
            } else {
                Log.d(TAG, "    Nenhuma chave de perfil encontrada");
            }

            Log.d(TAG, "===  preencherDadosParaEdicao CONCLUÍDO COM SUCESSO ===");

        } catch (Exception e) {
            Log.e(TAG, "EXCEÇÃO em preencherDadosParaEdicao: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar dados: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void selecionarNoSpinnerLocais(String local) {
        if (spinnerLocais == null) return;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerLocais.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(local)) {
                    spinnerLocais.setSelection(i, false);
                    break;
                }
            }
        }
    }

    private void selecionarNoSpinnerRestricao(String restricao) {
        if (spinnerRestricao == null) return;

        String[] restricoes = {"Whitelist", "Blacklist"};
        for (int i = 0; i < restricoes.length; i++) {
            if (restricoes[i].equals(restricao)) {
                spinnerRestricao.setSelection(i, false);
                break;
            }
        }
    }

    private void selecionarNoSpinnerModoEntrega(String modo) {
        if (spinnerModoEntrega == null) return;

        String[] modos = {"Centralizado", "Descentralizado"};
        for (int i = 0; i < modos.length; i++) {
            if (modos[i].equals(modo)) {
                spinnerModoEntrega.setSelection(i, false);
                break;
            }
        }
    }

    private boolean initViewsSucesso = true;

    private void initViews() {
        Log.d(TAG, "initViews chamado - Inicializando views");
        initViewsSucesso = true;

        btnBack = findViewById(R.id.btnBack);
        if (btnBack == null) { Log.e(TAG, "btnBack não encontrado!"); initViewsSucesso = false; }

        // Título da tela
        tvTituloTela = findViewById(R.id.tvTituloTela);

        llLocal = findViewById(R.id.llLocal);
        if (llLocal == null) { Log.e(TAG, "llLocal não encontrado!"); initViewsSucesso = false; }

        btnAddLocation = findViewById(R.id.btnAddLocation);
        if (btnAddLocation == null) { Log.e(TAG, "btnAddLocation não encontrado!"); initViewsSucesso = false; }

        tvLocalSelecionado = findViewById(R.id.tvLocalSelecionado);
        if (tvLocalSelecionado == null) { Log.e(TAG, "tvLocalSelecionado não encontrado!"); initViewsSucesso = false; }

        etTitulo = findViewById(R.id.etTitulo);
        if (etTitulo == null) { Log.e(TAG, "etTitulo não encontrado!"); initViewsSucesso = false; }

        etMensagem = findViewById(R.id.etMensagem);
        if (etMensagem == null) { Log.e(TAG, "etMensagem não encontrado!"); initViewsSucesso = false; }

        llImagem = findViewById(R.id.llImagem);
        if (llImagem == null) { Log.e(TAG, "llImagem não encontrado!"); initViewsSucesso = false; }

        llDataInicio = findViewById(R.id.llDataInicio);
        llDataFim = findViewById(R.id.llDataFim);
        llHoraInicio = findViewById(R.id.llHoraInicio);
        llHoraFim = findViewById(R.id.llHoraFim);

        tvDataInicio = findViewById(R.id.tvDataInicio);
        tvDataFim = findViewById(R.id.tvDataFim);
        tvHoraInicio = findViewById(R.id.tvHoraInicio);
        tvHoraFim = findViewById(R.id.tvHoraFim);

        llTipoRestricao = findViewById(R.id.llTipoRestricao);
        tvTipoRestricao = findViewById(R.id.tvTipoRestricao);
        spinnerRestricao = findViewById(R.id.spinnerRestricao);

        llModoEntrega = findViewById(R.id.llModoEntrega);
        tvModoEntrega = findViewById(R.id.tvModoEntrega);
        spinnerModoEntrega = findViewById(R.id.spinnerModoEntrega);

        spinnerLocais = findViewById(R.id.spinnerLocais);
        btnPublicar = findViewById(R.id.btnPublicar);

        etPesquisarChaves = findViewById(R.id.etPesquisarChaves);
        btnAdicionarChave = findViewById(R.id.btnAdicionarChave);
        rvChavesRestricoes = findViewById(R.id.rv_chaves_restricoes);
        layoutEmptyChaves = findViewById(R.id.layout_empty_chaves);
        cardChavesContainer = findViewById(R.id.card_chaves_container);

        if (tvTipoRestricao != null) {
            tvTipoRestricao.setText("Whitelist");
        }

        if (tvModoEntrega != null) {
            tvModoEntrega.setText("Centralizado");
        }

        Log.d(TAG, "initViews concluído - Status: " + (initViewsSucesso ? "OK" : "FALHOU"));
    }

    private void initRestricoes() {
        allKeys = new ArrayList<>();
        chavesFiltradas = new ArrayList<>();

        // Carregar chaves do backend
        carregarChavesDoBackend();

        if (rvChavesRestricoes != null && keyAdapter == null) {
            rvChavesRestricoes.setLayoutManager(new LinearLayoutManager(this));
            keyAdapter = new ProfileKeyAdapter(this, chavesFiltradas, false);

            keyAdapter.setOnValueClickListener((keyName, value) -> {
                for (ProfileKey key : allKeys) {
                    if (key.getName().equals(keyName)) {
                        key.toggleValue(value);

                        if (key.hasSelectedValues()) {
                            restricoesPerfil.put(keyName, new ArrayList<>(key.getSelectedValues()));
                        } else {
                            restricoesPerfil.remove(keyName);
                        }
                        break;
                    }
                }
                keyAdapter.notifyDataSetChanged();
            });

            rvChavesRestricoes.setAdapter(keyAdapter);
        }

        if (etPesquisarChaves != null) {
            etPesquisarChaves.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { filtrarChaves(s.toString()); }
            });
        }

        atualizarVisibilidadeChaves();
        if (cardChavesContainer != null) {
            cardChavesContainer.setVisibility(View.VISIBLE);
        }
    }

    private void carregarChavesDoBackend() {
        Log.d(TAG, "Carregando chaves do backend...");

        Call<List<ProfileKey>> call = RetrofitClient.getApiService(this).getAllPerfis();
        call.enqueue(new Callback<List<ProfileKey>>() {
            @Override
            public void onResponse(Call<List<ProfileKey>> call, Response<List<ProfileKey>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allKeys.clear();
                    allKeys.addAll(response.body());

                    // Se estiver em modo edição, restaurar seleções do anúncio
                    if (modoEdicao && anuncioParaEditar != null) {
                        restaurarSelecoesDoAnuncio();
                    }

                    chavesFiltradas.clear();
                    chavesFiltradas.addAll(allKeys);

                    if (keyAdapter != null) {
                        keyAdapter.updateKeys(chavesFiltradas);
                    }

                    atualizarVisibilidadeChaves();
                    Log.d(TAG, "Chaves carregadas: " + allKeys.size());
                } else {
                    Log.e(TAG, "Erro ao carregar chaves: " + response.code());

                }
            }

            @Override
            public void onFailure(Call<List<ProfileKey>> call, Throwable t) {
                Log.e(TAG, "Falha ao carregar chaves: " + t.getMessage());

            }
        });
    }

    private void restaurarSelecoesDoAnuncio() {
        if (anuncioParaEditar.getChavesPerfil() == null) return;

        for (Map.Entry<String, List<String>> entry : anuncioParaEditar.getChavesPerfil().entrySet()) {
            String keyName = entry.getKey();
            List<String> selectedValues = entry.getValue();

            for (ProfileKey key : allKeys) {
                if (key.getName().equals(keyName)) {
                    // Limpar seleções anteriores e adicionar as do anúncio
                    key.getSelectedValues().clear();
                    for (String value : selectedValues) {
                        if (key.getAvailableValues().contains(value)) {
                            key.getSelectedValues().add(value);
                        }
                    }

                    // Atualizar mapa de restrições
                    if (!key.getSelectedValues().isEmpty()) {
                        restricoesPerfil.put(keyName, new ArrayList<>(key.getSelectedValues()));
                    }
                    break;
                }
            }
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners chamado");

        btnBack.setOnClickListener(v -> {
            if (modoEdicao) {
                setResult(RESULT_CANCELED);
            }
            finish();
        });

        if (btnAddLocation != null) {
            btnAddLocation.setOnClickListener(v -> {
                Intent intent = new Intent(AdicionarAnunciosActivity.this, AdicionarLocalActivity.class);
                localLauncher.launch(intent);
            });
        }

        llImagem.setOnClickListener(v -> abrirGaleriaImagem());
        llDataInicio.setOnClickListener(v -> showDatePicker(tvDataInicio));
        llDataFim.setOnClickListener(v -> showDatePicker(tvDataFim));
        llHoraInicio.setOnClickListener(v -> showTimePicker(tvHoraInicio));
        llHoraFim.setOnClickListener(v -> showTimePicker(tvHoraFim));

        btnPublicar.setOnClickListener(v -> {
            if (modoEdicao) {
                salvarAlteracoes();
            } else {
                publicarAnuncio();
            }
        });

        btnAdicionarChave.setOnClickListener(v -> abrirAdicionarKeyDialog());

        if (llTipoRestricao != null && spinnerRestricao != null) {
            llTipoRestricao.setOnClickListener(v -> spinnerRestricao.performClick());
        }

        if (llModoEntrega != null && spinnerModoEntrega != null) {
            llModoEntrega.setOnClickListener(v -> spinnerModoEntrega.performClick());
        }

        if (llLocal != null && spinnerLocais != null) {
            llLocal.setOnClickListener(v -> spinnerLocais.performClick());
        }

        spinnerLocais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    localSelecionado = (String) parent.getItemAtPosition(position);
                    tvLocalSelecionado.setText(localSelecionado);
                } else {
                    localSelecionado = null;
                    tvLocalSelecionado.setText("Selecionar local");
                    tvLocalSelecionado.setTextColor(getColor(android.R.color.darker_gray));
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void filtrarChaves(String query) {
        chavesFiltradas.clear();
        if (query.isEmpty()) {
            chavesFiltradas.addAll(allKeys);
        } else {
            for (ProfileKey key : allKeys) {
                if (key.getName().toLowerCase().contains(query.toLowerCase())) {
                    chavesFiltradas.add(key);
                }
            }
        }
        if (keyAdapter != null) {
            keyAdapter.updateKeys(chavesFiltradas);
        }
        atualizarVisibilidadeChaves();
    }

    private void atualizarVisibilidadeChaves() {
        if (chavesFiltradas.isEmpty()) {
            rvChavesRestricoes.setVisibility(View.GONE);
            layoutEmptyChaves.setVisibility(View.VISIBLE);
        } else {
            rvChavesRestricoes.setVisibility(View.VISIBLE);
            layoutEmptyChaves.setVisibility(View.GONE);
        }
    }

    private void abrirGaleriaImagem() {
        // Primeiro verificar permissão
        if (!temPermissaoImagem()) {
            solicitarPermissaoImagem();
            return;
        }

        // Usar ACTION_OPEN_DOCUMENT para melhor compatibilidade com Android 10+
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        // Adicionar flags para acesso persistente
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        // Usar chooser para melhor experiência
        Intent chooser = Intent.createChooser(intent, "Selecionar imagem");

        try {
            imagemLauncher.launch(chooser);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Nenhuma app de galeria encontrada: " + e.getMessage());
            Toast.makeText(this, "Instale uma app de galeria", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean temPermissaoImagem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void showDatePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            String date = String.format("%02d/%02d/%04d", day, month + 1, year);
            textView.setText(date);
            textView.setTextColor(getColor(android.R.color.black));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(TextView textView) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            String time = String.format("%02d:%02d", hour, minute);
            textView.setText(time);
            textView.setTextColor(getColor(android.R.color.black));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void setupSpinner() {
        if (spinnerRestricao != null && tvTipoRestricao != null) {
            String[] restricoes = {"Whitelist", "Blacklist"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, restricoes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRestricao.setAdapter(adapter);

            spinnerRestricao.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selected = restricoes[position];
                    tvTipoRestricao.setText(selected);

                    if ("Nenhuma".equals(selected)) {
                        if (cardChavesContainer != null) {
                            cardChavesContainer.setVisibility(View.GONE);
                        }
                    } else {
                        if (cardChavesContainer != null) {
                            cardChavesContainer.setVisibility(View.VISIBLE);
                        }
                        atualizarVisibilidadeChaves();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            spinnerRestricao.setSelection(0, false);
        }
    }

    private void setupSpinnerModoEntrega() {
        if (spinnerModoEntrega != null && tvModoEntrega != null) {
            String[] modos = {"Centralizado", "Descentralizado"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerModoEntrega.setAdapter(adapter);

            spinnerModoEntrega.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    tvModoEntrega.setText(modos[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

            spinnerModoEntrega.setSelection(0, false);
        }
    }

    private void setupSpinnerLocais() {
        ApiService apiService = RetrofitClient.getApiService(this);

        Call<List<Local>> call = apiService.getTodosLocais(); // ou getLocaisDoUsuario(userId) se quiseres só os do user

        call.enqueue(new Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Local> locais = response.body();

                    List<String> nomesLocais = new ArrayList<>();
                    nomesLocais.add("Selecionar local"); // posição 0 = nenhum selecionado

                    for (Local local : locais) {
                        nomesLocais.add(local.getNome());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AdicionarAnunciosActivity.this,
                            android.R.layout.simple_spinner_item,
                            nomesLocais
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLocais.setAdapter(adapter);

                    // Se já tinhas um local selecionado (edição), mantém
                    if (localSelecionado != null) {
                        int pos = nomesLocais.indexOf(localSelecionado);
                        if (pos > 0) spinnerLocais.setSelection(pos);
                    }
                } else {
                    Toast.makeText(AdicionarAnunciosActivity.this,
                            "Erro ao carregar locais", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                // Este método é OBRIGATÓRIO!
                Log.e("AActivity", "Falha ao carregar locais: " + t.getMessage());
                Toast.makeText(AdicionarAnunciosActivity.this,
                        "Sem ligação à internet. Verifique a rede.", Toast.LENGTH_LONG).show();

            }
        });

            // Não define seleção padrão se está em modo edição
            if (!modoEdicao) {
                spinnerLocais.setSelection(0, false);
            }
    }


    private void abrirAdicionarKeyDialog() {
        AdicionarKeyDialog dialog = AdicionarKeyDialog.newInstance(allKeys, restricoesPerfil);
        dialog.setOnKeyAddedListener(this);
        dialog.show(getSupportFragmentManager(), "AdicionarKeyDialog");
    }

    @Override
    public void onKeyAdded(String keyName, List<String> values) {
        restricoesPerfil.put(keyName, values);
        // Salva nova chave no backend
        ApiService apiService = RetrofitClient.getApiService(this);
        Map<String, Object> request = new HashMap<>();
        request.put("chave", keyName);
        request.put("valores", values);  // Lista de strings
        Call<ProfileKey> call = apiService.criarPerfil(request);
        call.enqueue(new Callback<ProfileKey>() {
            @Override
            public void onResponse(Call<ProfileKey> call, Response<ProfileKey> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Nova chave salva na BD: " + keyName + " com valores " + values);
                    Toast.makeText(AdicionarAnunciosActivity.this, "Chave criada e salva!", Toast.LENGTH_SHORT).show();
                    // Atualiza lista local se precisar
                    allKeys.add(new ProfileKey(keyName, values));
                    chavesFiltradas.add(new ProfileKey(keyName, values));
                    keyAdapter.notifyDataSetChanged();
                    atualizarVisibilidadeChaves();
                } else {
                    Log.e(TAG, "Erro ao salvar chave na BD: " + response.code() + " - " + response.message());
                    Toast.makeText(AdicionarAnunciosActivity.this, "Erro ao salvar chave: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileKey> call, Throwable t) {
                Log.e(TAG, "Falha na rede ao salvar chave: " + t.getMessage());
                Toast.makeText(AdicionarAnunciosActivity.this, "Falha: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        filtrarChaves(etPesquisarChaves.getText().toString());
        if (keyAdapter != null) {
            keyAdapter.notifyDataSetChanged();
        }
        atualizarVisibilidadeChaves();
        Toast.makeText(this, "Chave pública adicionada: " + keyName, Toast.LENGTH_SHORT).show();
    }


    // PUBLICAR ANÚNCIO (modo criação)
    private void publicarAnuncio() {
        Log.d(TAG, "publicarAnuncio chamado - Iniciando validações");
        String titulo = etTitulo.getText().toString().trim();
        String mensagem = etMensagem.getText().toString().trim();
        String dataInicio = tvDataInicio.getText().toString();
        String dataFim = tvDataFim.getText().toString();
        String horaInicio = tvHoraInicio.getText().toString();
        String horaFim = tvHoraFim.getText().toString();
        String restricao = tvTipoRestricao.getText().toString();
        String modoEntrega = tvModoEntrega.getText().toString();

        // Validações
        if (localSelecionado == null || localSelecionado.equals("Selecionar um local")) {
            Log.w(TAG, "Validação falhou: Local não selecionado");
            Toast.makeText(this, "Por favor, adicione um local de propagação", Toast.LENGTH_LONG).show();
            return;
        }
        if (titulo.isEmpty()) {
            Log.w(TAG, "Validação falhou: Título vazio");
            Toast.makeText(this, "Digite um título para o anúncio", Toast.LENGTH_LONG).show();
            return;
        }
        if (mensagem.isEmpty()) {
            Log.w(TAG, "Validação falhou: Mensagem vazia");
            Toast.makeText(this, "Escreva uma mensagem para o anúncio", Toast.LENGTH_LONG).show();
            return;
        }
        if (dataInicio.equals("dd/mm/aaaa") || dataFim.equals("dd/mm/aaaa")) {
            Log.w(TAG, "Validação falhou: Datas não selecionadas");
            Toast.makeText(this, "Selecione as datas de início e fim", Toast.LENGTH_LONG).show();
            return;
        }
        if (horaInicio.equals("hh:mm") || horaFim.equals("hh:mm")) {
            Log.w(TAG, "Validação falhou: Horas não selecionadas");
            Toast.makeText(this, "Selecione os horários", Toast.LENGTH_LONG).show();
            return;
        }
        if (!"Nenhuma".equals(restricao) && restricoesPerfil.isEmpty()) {
            Log.w(TAG, "Validação falhou: Chaves vazias para restrição " + restricao);
            Toast.makeText(this, "Adicione pelo menos uma chave pública de restrição", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Validações OK - Criando Anuncio");
        Anuncio novoAnuncio = new Anuncio(null, titulo, mensagem, localSelecionado, caminhoImagem,
                dataInicio, dataFim, horaInicio, horaFim, restricao, modoEntrega);
        for (Map.Entry<String, List<String>> entry : restricoesPerfil.entrySet()) {
            novoAnuncio.addChave(entry.getKey(), entry.getValue());
        }

        // Obter userId do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Toast.makeText(this, "Erro: Usuário não logado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chama diretamente a busca do local e envio (sem simulação)
        buscarLocalIdEEnviarAnuncio(novoAnuncio, userId);
    }

    private void buscarLocalIdEEnviarAnuncio(Anuncio anuncio, Long userId) {
        ApiService apiService = RetrofitClient.getApiService(this);

        Call<List<Local>> call = apiService.getTodosLocais();
        call.enqueue(new Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long localId = null;
                    for (Local local : response.body()) {
                        if (local.getNome().equals(anuncio.local)) {
                            localId = local.getId();
                            break;
                        }
                    }

                    if (localId != null) {
                        enviarAnuncioParaBackend(anuncio, userId, localId);
                    } else {
                        Toast.makeText(AdicionarAnunciosActivity.this,
                                "Local não encontrado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdicionarAnunciosActivity.this,
                            "Erro ao buscar local", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                Toast.makeText(AdicionarAnunciosActivity.this,
                        "Falha na rede ao buscar local", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enviarAnuncioParaBackend(Anuncio anuncio, Long userId, Long localId) {
        ApiService apiService = RetrofitClient.getApiService(this);

        // Preparar os dados para multipart - CORRIGIDO conforme AnuncioResponse
        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId.toString());
        RequestBody localIdBody = RequestBody.create(MediaType.parse("text/plain"), localId.toString());
        RequestBody tituloBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.titulo);
        RequestBody descricaoBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.descricao);
        RequestBody dataInicioBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.dataInicio);
        RequestBody dataFimBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.dataFim);
        RequestBody horaInicioBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.horaInicio);
        RequestBody horaFimBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.horaFim);
        RequestBody policyTypeBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.tipoRestricao.toUpperCase());
        RequestBody modoEntregaBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.modoEntrega.toUpperCase());

        // Preparar chaves e valores do perfil - CORRIGIDO
        List<MultipartBody.Part> perfilChaveParts = new ArrayList<>();
        List<MultipartBody.Part> perfilValorParts = new ArrayList<>();

        if (anuncio.getChavesPerfil() != null) {
            for (Map.Entry<String, List<String>> entry : anuncio.getChavesPerfil().entrySet()) {
                String chave = entry.getKey();
                for (String valor : entry.getValue()) {
                    perfilChaveParts.add(MultipartBody.Part.createFormData("perfilChave", chave));
                    perfilValorParts.add(MultipartBody.Part.createFormData("perfilValor", valor));
                }
            }
        }

        // Preparar imagem se existir
        MultipartBody.Part imagemPart = null;
        if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
            File file = new File(caminhoImagem); // Já é path local do cache
            if (file.exists()) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                imagemPart = MultipartBody.Part.createFormData("imagem", file.getName(), requestFile);
                Log.d(TAG, "Imagem anexada: " + file.getAbsolutePath() + " | Tamanho: " + file.length());
            } else {
                Log.w(TAG, "Arquivo de imagem não encontrado: " + caminhoImagem);
            }
        }

        Log.d(TAG, "Enviando anúncio para backend...");
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "Local ID: " + localId);
        Log.d(TAG, "Título: " + anuncio.titulo);
        Log.d(TAG, "Descrição tamanho: " + anuncio.descricao.length());
        Log.d(TAG, "Data Início: " + anuncio.dataInicio);
        Log.d(TAG, "Data Fim: " + anuncio.dataFim);
        Log.d(TAG, "Hora Início: " + anuncio.horaInicio);
        Log.d(TAG, "Hora Fim: " + anuncio.horaInicio);
        Log.d(TAG, "Tipo de Restricção: " + anuncio.tipoRestricao);
        Log.d(TAG, "Politica de entrega: " + anuncio.modoEntrega);
        Log.d(TAG, "Imagem path: " + (caminhoImagem != null ? caminhoImagem : "Sem imagem"));
        Log.d(TAG, "Chaves restrições: " + restricoesPerfil.size() + " itens");
        for (Map.Entry<String, List<String>> entry : restricoesPerfil.entrySet()) {
            Log.d(TAG, "Chave: " + entry.getKey() + " | Valores: " + entry.getValue());
        }

        Call<AnuncioResponse> call = apiService.criarAnuncio(
                userIdBody, localIdBody, tituloBody, descricaoBody,
                dataInicioBody, dataFimBody, horaInicioBody, horaFimBody,
                policyTypeBody, modoEntregaBody, perfilChaveParts, perfilValorParts, imagemPart
        );

        call.enqueue(new Callback<AnuncioResponse>() {
            @Override
            public void onResponse(Call<AnuncioResponse> call, Response<AnuncioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AnuncioResponse anuncioResponse = response.body();
                    Log.d(TAG, "Anúncio criado com sucesso! ID: " + anuncioResponse.getId());
                    Toast.makeText(AdicionarAnunciosActivity.this,
                            "Anúncio publicado com sucesso!", Toast.LENGTH_SHORT).show();

                    // Retornar resultado para atualizar a lista na MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("anuncio_criado", true);
                    setResult(RESULT_OK, resultIntent);
                    limparCampos();
                    finish();

                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao criar anúncio: " + response.code() + " - " + errorBody);
                        Toast.makeText(AdicionarAnunciosActivity.this,
                                "Erro ao publicar anúncio: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody: " + e.getMessage());
                        Toast.makeText(AdicionarAnunciosActivity.this,
                                "Erro ao publicar anúncio: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AnuncioResponse> call, Throwable t) {
                Log.e(TAG, "Falha ao criar anúncio: " + t.getMessage());
                Toast.makeText(AdicionarAnunciosActivity.this,
                        "Falha na rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // SALVAR ALTERAÇÕES (modo edição)
    private void salvarAlteracoes() {
        Log.d(TAG, "salvarAlteracoes chamado");

        // Validações (mantenha as mesmas)
        if (!validarCampos()) {
            return;
        }

        // Atualiza o anúncio local
        anuncioParaEditar.titulo = etTitulo.getText().toString().trim();
        anuncioParaEditar.descricao = etMensagem.getText().toString().trim();
        anuncioParaEditar.local = localSelecionado;
        anuncioParaEditar.dataInicio = tvDataInicio.getText().toString();
        anuncioParaEditar.dataFim = tvDataFim.getText().toString();
        anuncioParaEditar.horaInicio = tvHoraInicio.getText().toString();
        anuncioParaEditar.horaFim = tvHoraFim.getText().toString();
        anuncioParaEditar.tipoRestricao = tvTipoRestricao.getText().toString();
        anuncioParaEditar.modoEntrega = tvModoEntrega.getText().toString();

        // Atualiza imagem se foi alterada
        if (caminhoImagem != null && !caminhoImagem.equals(anuncioParaEditar.imagem)) {
            anuncioParaEditar.imagem = caminhoImagem;
        }

        // Atualiza chaves
        anuncioParaEditar.getChavesPerfil().clear();
        for (Map.Entry<String, List<String>> entry : restricoesPerfil.entrySet()) {
            anuncioParaEditar.addChave(entry.getKey(), entry.getValue());
        }

        // Agora enviar para o backend
        enviarAtualizacaoParaBackend();
    }

    private void enviarAtualizacaoParaBackend() {
        if (anuncioParaEditar == null || anuncioParaEditar.id == null) {
            Toast.makeText(this, "Erro: Anúncio sem ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obter userId
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1L);
        if (userId == -1L) {
            Toast.makeText(this, "Erro: Usuário não logado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buscar o ID do local
        buscarLocalIdEAtualizarAnuncio(anuncioParaEditar, userId);
    }

    private void buscarLocalIdEAtualizarAnuncio(Anuncio anuncio, Long userId) {
        ApiService apiService = RetrofitClient.getApiService(this);

        Call<List<Local>> call = apiService.getTodosLocais();
        call.enqueue(new Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long localId = null;
                    for (Local local : response.body()) {
                        if (local.getNome().equals(anuncio.local)) {
                            localId = local.getId();
                            break;
                        }
                    }

                    if (localId != null) {
                        atualizarAnuncioNoBackend(anuncio, userId, localId);
                    } else {
                        Toast.makeText(AdicionarAnunciosActivity.this,
                                "Local não encontrado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdicionarAnunciosActivity.this,
                            "Erro ao buscar local", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                Toast.makeText(AdicionarAnunciosActivity.this,
                        "Falha na rede ao buscar local", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarAnuncioNoBackend(Anuncio anuncio, Long userId, Long localId) {
        ApiService apiService = RetrofitClient.getApiService(this);

        // Preparar os dados para multipart - similar ao criar
        RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId.toString());
        RequestBody localIdBody = RequestBody.create(MediaType.parse("text/plain"), localId.toString());
        RequestBody tituloBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.titulo);
        RequestBody descricaoBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.descricao);
        RequestBody dataInicioBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.dataInicio);
        RequestBody dataFimBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.dataFim);
        RequestBody horaInicioBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.horaInicio);
        RequestBody horaFimBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.horaFim);
        RequestBody policyTypeBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.tipoRestricao.toUpperCase());
        RequestBody modoEntregaBody = RequestBody.create(MediaType.parse("text/plain"), anuncio.modoEntrega.toUpperCase());

        // Preparar chaves e valores do perfil
        List<MultipartBody.Part> perfilChaveParts = new ArrayList<>();
        List<MultipartBody.Part> perfilValorParts = new ArrayList<>();

        if (anuncio.getChavesPerfil() != null) {
            for (Map.Entry<String, List<String>> entry : anuncio.getChavesPerfil().entrySet()) {
                String chave = entry.getKey();
                for (String valor : entry.getValue()) {
                    perfilChaveParts.add(MultipartBody.Part.createFormData("perfilChave", chave));
                    perfilValorParts.add(MultipartBody.Part.createFormData("perfilValor", valor));
                }
            }
        }

        // Preparar imagem se existir e foi alterada
        MultipartBody.Part imagemPart = null;

        // ADICIONAR: Verificar se uma nova imagem foi selecionada
        if (imagemFoiAlterada && caminhoImagem != null && !caminhoImagem.isEmpty()) {
            File file = new File(caminhoImagem);

            // Verificar se é um caminho real de arquivo (não URI)
            if (file.exists() && !caminhoImagem.startsWith("content://") && !caminhoImagem.startsWith("file://")) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                imagemPart = MultipartBody.Part.createFormData("imagem", file.getName(), requestFile);
                Log.d(TAG, "Imagem atualizada: " + file.getAbsolutePath());
            } else if (caminhoImagem.startsWith("content://")) {
                // Converter URI para arquivo
                File tempFile = uriParaArquivo(Uri.parse(caminhoImagem));
                if (tempFile != null && tempFile.exists()) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
                    imagemPart = MultipartBody.Part.createFormData("imagem", tempFile.getName(), requestFile);
                    Log.d(TAG, "Imagem convertida de URI: " + tempFile.getAbsolutePath());
                }
            }
        } else {
            Log.d(TAG, "Imagem não foi alterada, mantendo a original");
        }

        // Chamar API de atualização - você precisa criar este endpoint no seu ApiService
        Call<AnuncioResponse> call = apiService.atualizarAnuncio(
                anuncio.id,  // ID do anúncio a ser atualizado
                userIdBody, localIdBody, tituloBody, descricaoBody,
                dataInicioBody, dataFimBody, horaInicioBody, horaFimBody,
                policyTypeBody, modoEntregaBody, perfilChaveParts, perfilValorParts, imagemPart
        );

        call.enqueue(new Callback<AnuncioResponse>() {
            @Override
            public void onResponse(Call<AnuncioResponse> call, Response<AnuncioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AnuncioResponse anuncioAtualizado = response.body();
                    Log.d(TAG, "Anúncio atualizado com sucesso! ID: " + anuncioAtualizado.getId());

                    // Atualizar o anúncio com os dados retornados
                    anuncioParaEditar = anuncioAtualizado.toAnuncio();

                    // Retornar resultado para MainActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("anuncio_editado", anuncioParaEditar);
                    resultIntent.putExtra("position", posicaoAnuncio);
                    setResult(RESULT_OK, resultIntent);

                    Toast.makeText(AdicionarAnunciosActivity.this,
                            "Anúncio atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "Erro ao atualizar anúncio: " + response.code() + " - " + errorBody);
                        Toast.makeText(AdicionarAnunciosActivity.this,
                                "Erro ao atualizar anúncio: " + response.code(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody: " + e.getMessage());
                        Toast.makeText(AdicionarAnunciosActivity.this,
                                "Erro ao atualizar anúncio", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AnuncioResponse> call, Throwable t) {
                Log.e(TAG, "Falha ao atualizar anúncio: " + t.getMessage());
                Toast.makeText(AdicionarAnunciosActivity.this,
                        "Falha na rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    // Método auxiliar para converter URI em File
    private File uriParaArquivo(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = File.createTempFile("upload_", ".jpg", getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter URI para arquivo: " + e.getMessage());
            return null;
        }
    }

    private boolean validarCampos() {
        String titulo = etTitulo.getText().toString().trim();
        String mensagem = etMensagem.getText().toString().trim();
        String dataInicio = tvDataInicio.getText().toString();
        String dataFim = tvDataFim.getText().toString();
        String horaInicio = tvHoraInicio.getText().toString();
        String horaFim = tvHoraFim.getText().toString();
        String restricao = tvTipoRestricao.getText().toString();

        if (localSelecionado == null || localSelecionado.equals("Selecionar um local")) {
            Toast.makeText(this, "Por favor, adicione um local de propagação", Toast.LENGTH_LONG).show();
            return false;
        }
        if (titulo.isEmpty()) {
            Toast.makeText(this, "Digite um título para o anúncio", Toast.LENGTH_LONG).show();
            return false;
        }
        if (mensagem.isEmpty()) {
            Toast.makeText(this, "Escreva uma mensagem para o anúncio", Toast.LENGTH_LONG).show();
            return false;
        }
        if (dataInicio.equals("dd/mm/aaaa") || dataFim.equals("dd/mm/aaaa")) {
            Toast.makeText(this, "Selecione as datas de início e fim", Toast.LENGTH_LONG).show();
            return false;
        }
        if (horaInicio.equals("hh:mm") || horaFim.equals("hh:mm")) {
            Toast.makeText(this, "Selecione os horários", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!"Nenhuma".equals(restricao) && restricoesPerfil.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos uma chave pública de restrição", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
    private void limparCampos() {
        Log.d(TAG, "limparCampos chamado - Limpando campos");
        etTitulo.setText("");
        etMensagem.setText("");
        resetarDataHora(tvDataInicio, "dd/mm/aaaa");
        resetarDataHora(tvDataFim, "dd/mm/aaaa");
        resetarDataHora(tvHoraInicio, "hh:mm");
        resetarDataHora(tvHoraFim, "hh:mm");
        tvTipoRestricao.setText("Whitelist");
        spinnerRestricao.setSelection(0);
        tvModoEntrega.setText("Centralizado");
        spinnerModoEntrega.setSelection(0);
        etPesquisarChaves.setText("");

        localSelecionado = null;
        tvLocalSelecionado.setText("Selecionar um local");
        tvLocalSelecionado.setTextColor(getColor(android.R.color.darker_gray));
        caminhoImagem = null;
        ImageView ivPreview = findViewById(R.id.ivPreviewImagem);
        ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        TextView tvHint = findViewById(R.id.tvImagemHint);
        tvHint.setText("Toque para adicionar uma imagem");

        restricoesPerfil.clear();
        for (ProfileKey key : allKeys) {
            key.getSelectedValues().clear();
        }
        chavesFiltradas.clear();
        chavesFiltradas.addAll(allKeys);
        if (keyAdapter != null) {
            keyAdapter.notifyDataSetChanged();
        }
        atualizarVisibilidadeChaves();
        Log.d(TAG, "Campos limpos - Chaves resetadas");
    }

    private void resetarDataHora(TextView textView, String textoPadrao) {
        if (textView != null) {
            textView.setText(textoPadrao);
            textView.setTextColor(getColor(android.R.color.darker_gray));
            Log.d(TAG, "Placeholder definido: " + textoPadrao);
        } else {
            Log.e(TAG, "TextView null - Não reseta data/hora");
        }
    }
}