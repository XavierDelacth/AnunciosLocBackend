package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.AdicionarAnunciosActivity;
import ao.co.isptec.aplm.projetoanuncioloc.MainActivity;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioMainDialog;

public class MainAnuncioAdapter extends RecyclerView.Adapter<MainAnuncioAdapter.ViewHolder> {

    private Context context;
    private List<Anuncio> lista;
    private boolean isMeusAnuncios;
    private OnActionClickListener onActionClickListener;

    public interface OnActionClickListener {
        void onEditClick(Anuncio anuncio, int position);
        void onDeleteClick(Anuncio anuncio, int position);
        void onSaveClick(Anuncio anuncio, int position);
    }

    public MainAnuncioAdapter(Context context, List<Anuncio> lista) {
        this.context = context;
        this.lista = lista;
        this.isMeusAnuncios = false;
        this.onActionClickListener = null;
    }

    public MainAnuncioAdapter(Context context, List<Anuncio> lista, boolean isMeusAnuncios, OnActionClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.isMeusAnuncios = isMeusAnuncios;
        this.onActionClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Anuncio a = lista.get(position);
        holder.tvTitulo.setText(a.titulo);
        holder.tvDescricao.setText(a.descricao);

        // === CARREGAMENTO DE IMAGEM COM LOGS ===
        String urlImagem = a.getImagemUrl();
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
            Log.d("MainAnuncioAdapter", "URL construída: " + fullUrl);
        } else {
            fullUrl = null;
        }

        Log.d("MainAnuncioAdapter", "=== CARREGAMENTO DE IMAGEM [Posição " + position + "] ===");
        Log.d("MainAnuncioAdapter", "Título: " + a.titulo);
        Log.d("MainAnuncioAdapter", "URL do banco: " + urlImagem);
        Log.d("MainAnuncioAdapter", "URL completa: " + fullUrl);
        Log.d("MainAnuncioAdapter", "BASE_URL: " + RetrofitClient.BASE_URL);

        if (fullUrl != null) {
            Glide.with(context)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            Log.e("MainAnuncioAdapter", "FALHA ao carregar: " + fullUrl);
                            if (e != null) {
                                Log.e("MainAnuncioAdapter", "Mensagem erro: " + e.getMessage());
                                e.logRootCauses("MainAnuncioAdapter");
                            }
                            return false; // false = mostrar drawable de erro
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            Log.d("MainAnuncioAdapter", "SUCESSO ao carregar: " + fullUrl);
                            Log.d("MainAnuncioAdapter", "DataSource: " + dataSource);
                            return false; // false = exibir a imagem
                        }
                    })
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                    .into(holder.imgAnuncio);
        } else {
            Log.w("MainAnuncioAdapter", "⚠️ URL da imagem é null ou vazia para: " + a.titulo);
            holder.imgAnuncio.setImageResource(R.drawable.ic_placeholder);
        }

        // === BOTÕES DE AÇÃO (MEUS ANÚNCIOS) ===
        if (isMeusAnuncios) {
            holder.layoutAcoes.setVisibility(View.VISIBLE);

            holder.btnEditar.setOnClickListener(v -> {
                // ✅ USAR getAdapterPosition() em vez de position
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(context, AdicionarAnunciosActivity.class);
                    intent.putExtra("MODO_EDICAO", true);
                    intent.putExtra("ANUNCIO_PARA_EDITAR", lista.get(pos));
                    intent.putExtra("POSICAO", pos);
                    ((AppCompatActivity) context).startActivityForResult(intent, MainActivity.REQUEST_CODE_EDITAR_ANUNCIO);
                }
            });

            holder.btnExcluir.setOnClickListener(v -> {
                // ✅ USAR getAdapterPosition() em vez de position
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && onActionClickListener != null) {
                    onActionClickListener.onDeleteClick(lista.get(pos), pos);
                }
            });
        } else {
            holder.layoutAcoes.setVisibility(View.GONE);
        }

        // === CLIQUE NO ITEM COMPLETO ===
        holder.itemView.setOnClickListener(v -> {
            // ✅ USAR getAdapterPosition() em vez de position armazenado
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Anuncio anuncio = lista.get(pos);
                VisualizarAnuncioMainDialog dialog = VisualizarAnuncioMainDialog.newInstance(
                        anuncio,
                        pos,
                        new VisualizarAnuncioMainDialog.BookmarkCallback() {
                            @Override
                            public void onBookmarkChanged(int position, boolean saved) {
                                // Callback vazio - MainActivity não usa bookmark
                            }
                        }
                );
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "VisualizarAnuncioMain");
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // === VIEWHOLDER ===
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescricao;
        ImageView btnEditar, btnExcluir, imgAnuncio;
        View layoutAcoes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            btnEditar = itemView.findViewById(R.id.btnEditarAnuncio);
            btnExcluir = itemView.findViewById(R.id.btnExcluirAnuncio);
            imgAnuncio = itemView.findViewById(R.id.img_anuncio);
            layoutAcoes = itemView.findViewById(R.id.layoutAcoes);
        }
    }
}