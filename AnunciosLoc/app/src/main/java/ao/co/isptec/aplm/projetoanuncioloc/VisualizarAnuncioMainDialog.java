package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;
import ao.co.isptec.aplm.projetoanuncioloc.Adapters.ProfileKeyAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;

public class VisualizarAnuncioMainDialog extends DialogFragment {

    private static final String TAG = "VisualizarAnuncioDialog";
    private static final String ARG_ANUNCIO = "anuncio";
    private static final String ARG_POSITION = "position";

    public interface BookmarkCallback {
        void onBookmarkChanged(int position, boolean saved);
    }

    private Anuncio anuncio;
    private int position;
    private BookmarkCallback callback;
    private List<ProfileKey> allKeys = new ArrayList<>();
    private List<ProfileKey> chavesFiltradas = new ArrayList<>();
    private ProfileKeyAdapter keyAdapter;

    // Views
    private ImageButton btnClose;
    private ImageView imgAnnouncement;
    private TextView tvTitle, tvContent, tvLocal, tvTipoRestricao, tvDataInicio, tvDataFim;
    private TextView tvHoraInicio, tvHoraFim, tvModoEntrega;
    private EditText etPesquisarChaves;
    private RecyclerView rvChavesRestricoes;
    private LinearLayout layoutEmptyChaves;
    private CardView cardChavesContainer;

    public static VisualizarAnuncioMainDialog newInstance(Anuncio anuncio, int position, BookmarkCallback listener) {
        VisualizarAnuncioMainDialog dialog = new VisualizarAnuncioMainDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANUNCIO, anuncio);
        args.putInt(ARG_POSITION, position);
        dialog.setArguments(args);
        dialog.callback = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            anuncio = getArguments().getParcelable(ARG_ANUNCIO);
            position = getArguments().getInt(ARG_POSITION);
        }
        if (anuncio == null) {
            dismiss();
            return;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_visualizar_anuncio, container, false);
        initViews(view);
        setupClickListeners();
        preencherDados();
        setupChaves();
        return view;
    }

    private void initViews(View view) {
        Log.d(TAG, "Inicializando views no diálogo");
        btnClose = view.findViewById(R.id.btn_close);
        imgAnnouncement = view.findViewById(R.id.img_announcement);
        tvTitle = view.findViewById(R.id.announcementTitle);
        tvContent = view.findViewById(R.id.announcementContent);
        tvLocal = view.findViewById(R.id.tvLocal);
        tvTipoRestricao = view.findViewById(R.id.tvTipoRestricao);
        tvDataInicio = view.findViewById(R.id.tvDataInicio);
        tvDataFim = view.findViewById(R.id.tvDataFim);
        tvHoraInicio = view.findViewById(R.id.tvHoraInicio);
        tvHoraFim = view.findViewById(R.id.tvHoraFim);
        tvModoEntrega = view.findViewById(R.id.tvModoEntrega);
        //etPesquisarChaves = view.findViewById(R.id.etPesquisarChaves);
        rvChavesRestricoes = view.findViewById(R.id.rv_chaves_restricoes);
        layoutEmptyChaves = view.findViewById(R.id.layout_empty_chaves);
        cardChavesContainer = view.findViewById(R.id.card_chaves_container);
    }

    private void setupClickListeners() {
        Log.d(TAG, "Configurando listeners no diálogo");
        btnClose.setOnClickListener(v -> dismiss());


        if (etPesquisarChaves != null) {
            etPesquisarChaves.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filtrarChaves(s.toString());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void preencherDados() {
        if (anuncio == null) return;
        Log.d(TAG, "Preenchendo dados no diálogo");

        tvTitle.setText(anuncio.titulo);
        tvContent.setText(anuncio.descricao);

        // === CARREGAMENTO DE IMAGEM - MESMA LÓGICA DO ADAPTER ===
        String urlImagem = anuncio.getImagemUrl();
        String fullUrl;

        if (urlImagem != null && !urlImagem.isEmpty()) {
            String baseUrl = RetrofitClient.BASE_URL;
            // Garante que baseUrl não termine com /
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }

            // Garante que urlImagem comece com /
            String path = urlImagem;
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            fullUrl = baseUrl + path;
            Log.d(TAG, "URL construída igual ao adapter: " + fullUrl);
        } else {
            fullUrl = null;
        }

// REMOVER tint
        imgAnnouncement.setImageTintList(null);
        imgAnnouncement.setImageTintMode(null);
        imgAnnouncement.clearColorFilter();

        if (fullUrl != null) {
            Glide.with(requireContext())
                    .load(fullUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(32)))
                    .placeholder(R.drawable.espaco_image)
                    .error(R.drawable.ic_placeholder)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "FALHA: " + fullUrl);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d(TAG, "SUCESSO: " + fullUrl);
                            return false;
                        }
                    })
                    .into(imgAnnouncement);
        } else {
            imgAnnouncement.setImageResource(R.drawable.espaco_image);
        }

        // Configurar cor do tipo de restrição
        if ("Whitelist".equals(anuncio.getTipoRestricao())) {
            tvTipoRestricao.setBackgroundColor(getResources().getColor(R.color.verde_principal));
            tvTipoRestricao.setTextColor(getResources().getColor(R.color.white));
        } else if ("Blacklist".equals(anuncio.getTipoRestricao())) {
            tvTipoRestricao.setBackgroundColor(getResources().getColor(R.color.vermelho_cancelar));
            tvTipoRestricao.setTextColor(getResources().getColor(R.color.white));
        }

        // Informações
        tvLocal.setText(anuncio.local != null ? anuncio.local : "Não especificado");
        tvTipoRestricao.setText(anuncio.getTipoRestricao() != null ? anuncio.getTipoRestricao() : "Nenhuma");
        tvDataInicio.setText(anuncio.getDataInicio() != null ? anuncio.getDataInicio() : "--/--/----");
        tvDataFim.setText(anuncio.getDataFim() != null ? anuncio.getDataFim() : "--/--/----");
        tvHoraInicio.setText(anuncio.getHoraInicio() != null ? anuncio.getHoraInicio() : "--:--");
        tvHoraFim.setText(anuncio.getHoraFim() != null ? anuncio.getHoraFim() : "--:--");
        tvModoEntrega.setText(anuncio.getModoEntrega() != null ? anuncio.getModoEntrega() : "Não especificado");
    }

    private void setupChaves() {
        Log.d(TAG, "=== INICIANDO SETUP DE RESTRIÇÕES ===");

        if (anuncio == null) {
            Log.e(TAG, "ERRO: Anúncio é NULL!");
            mostrarEmptyState();
            return;
        }

        // LOG 1: Verifica se o mapa chavesPerfil existe
        Log.d(TAG, "chavesPerfil do anúncio: " + anuncio.getChavesPerfil());

        Map<String, List<String>> chavesMap = anuncio.getChavesPerfil();

        if (chavesMap == null) {
            Log.e(TAG, "chavesPerfil é NULL! O backend não enviou nada.");
            mostrarEmptyState();
            return;
        }

        if (chavesMap.isEmpty()) {
            Log.w(TAG, "chavesPerfil está VAZIO! Backend enviou mapa vazio.");
            mostrarEmptyState();
            return;
        }

        Log.d(TAG, "SUCESSO! Recebidas " + chavesMap.size() + " chaves do servidor:");

        allKeys.clear();
        chavesFiltradas.clear();

        for (Map.Entry<String, List<String>> entry : chavesMap.entrySet()) {
            String chave = entry.getKey();
            List<String> valores = entry.getValue();

            Log.d(TAG, "   → Chave: '" + chave + "' | Valores: " + valores);

            if (valores == null || valores.isEmpty()) {
                Log.w(TAG, "   Valores nulos ou vazios para a chave: " + chave);
                continue;
            }

            ProfileKey key = new ProfileKey(chave, new ArrayList<>(valores));
            key.getSelectedValues().addAll(valores);

            allKeys.add(key);
            Log.d(TAG, "   Adicionada ao dialog: " + chave + " → " + valores);
        }

        if (allKeys.isEmpty()) {
            Log.e(TAG, "NENHUMA chave válida após processamento! Empty state será mostrado.");
            mostrarEmptyState();
        } else {
            Log.d(TAG, "TOTAL DE " + allKeys.size() + " chaves carregadas com sucesso! Mostrando no RecyclerView...");
            chavesFiltradas.addAll(allKeys);
            setupRecyclerView();
        }
    }

    private List<ProfileKey> carregarChavesPublicas() {
        List<ProfileKey> chaves = new ArrayList<>();

        // IMPORTANTE: Usa Arrays.asList() + ArrayList para compatibilidade
        chaves.add(new ProfileKey("Gênero", new ArrayList<>(Arrays.asList("Masculino", "Feminino", "Outro"))));
        chaves.add(new ProfileKey("Idade", new ArrayList<>(Arrays.asList("18-24", "25-34", "35+"))));
        chaves.add(new ProfileKey("Clube Favorito", new ArrayList<>(Arrays.asList("Real Madrid", "Barcelona", "Benfica"))));

        Log.d(TAG, "Chaves públicas carregadas: " + chaves.size());
        return chaves;
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Configurando RecyclerView com " + chavesFiltradas.size() + " chaves");

        rvChavesRestricoes.setLayoutManager(new LinearLayoutManager(getContext()));

        // IMPORTANTE: showOnlySelected = true para modo VISUALIZAÇÃO (somente leitura)
        // Mostra apenas os valores selecionados como texto, SEM botões interativos
        keyAdapter = new ProfileKeyAdapter(getContext(), chavesFiltradas, true);

        // NÃO define listener - modo somente leitura

        rvChavesRestricoes.setAdapter(keyAdapter);

        rvChavesRestricoes.setVisibility(View.VISIBLE);
        layoutEmptyChaves.setVisibility(View.GONE);

        Log.d(TAG, "RecyclerView configurado em modo SOMENTE LEITURA");
    }

    private void mostrarEmptyState() {
        Log.d(TAG, "Mostrando empty state");
        if (rvChavesRestricoes != null) {
            rvChavesRestricoes.setVisibility(View.GONE);
        }
        if (layoutEmptyChaves != null) {
            layoutEmptyChaves.setVisibility(View.VISIBLE);
        }
    }

    private void filtrarChaves(String query) {
        Log.d(TAG, "Filtrando chaves com query: '" + query + "'");
        chavesFiltradas.clear();

        if (query.isEmpty()) {
            chavesFiltradas.addAll(allKeys);
            Log.d(TAG, "Query vazia - mostrando todas as " + allKeys.size() + " chaves");
        } else {
            String queryLower = query.toLowerCase();
            for (ProfileKey key : allKeys) {
                if (key.getName().toLowerCase().contains(queryLower)) {
                    chavesFiltradas.add(key);
                }
            }
            Log.d(TAG, "Filtro resultou em " + chavesFiltradas.size() + " chaves");
        }

        if (keyAdapter != null) {
            keyAdapter.updateKeys(chavesFiltradas);
        }

        boolean hasResults = !chavesFiltradas.isEmpty();
        rvChavesRestricoes.setVisibility(hasResults ? View.VISIBLE : View.GONE);
        layoutEmptyChaves.setVisibility(hasResults ? View.GONE : View.VISIBLE);
    }
}