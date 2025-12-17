package ao.co.isptec.aplm.projetoanuncioloc.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Notificacao;
import ao.co.isptec.aplm.projetoanuncioloc.R;
import ao.co.isptec.aplm.projetoanuncioloc.VisualizarAnuncioDialog;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {

    private Context context;
    private List<Notificacao> lista;
    private OnSaveClickListener onSaveClickListener;

    public interface OnSaveClickListener {
        void onSaveClick(Notificacao notificacao, int position);
    }

    public NotificacaoAdapter(Context context, List<Notificacao> lista, OnSaveClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.onSaveClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anuncio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacao notificacao = lista.get(position);
        holder.tvTitulo.setText(notificacao.getTitulo());
        holder.tvDescricao.setText(notificacao.getDescricao());

        // CARREGAR IMAGEM DO ANÚNCIO
        if (notificacao.getAnuncioId() != null) {
            carregarImagemAnuncio(notificacao.getAnuncioId(), holder.imgAnuncio);
        } else {
            holder.imgAnuncio.setImageResource(R.drawable.espaco_image);
        }

        // BOOKMARK - sempre mostra como não salvo inicialmente
        holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_nao_salvo);

        // CLIQUE NO ITEM → busca anúncio completo e abre VisualizarAnuncioDialog
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Notificacao notif = lista.get(pos);
                
                // Buscar anúncio completo do backend para ter a imagem
                buscarAnuncioCompleto(notif.getAnuncioId(), pos, holder);
            }
        });

        // CLIQUE DIRETO NO BOOKMARK → guarda o anúncio
        holder.btnSalvar.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                // Muda o ícone imediatamente para feedback visual
                holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_salvo);

                // Notifica o listener para guardar o anúncio
                onSaveClickListener.onSaveClick(lista.get(pos), pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    // Método para atualizar o estado de um item específico
    public void updateItemSavedState(int position, boolean saved) {
        if (position >= 0 && position < lista.size()) {
            notifyItemChanged(position);
        }
    }

    // Método para carregar imagem do anúncio
    private void carregarImagemAnuncio(Long anuncioId, ImageView imageView) {
        if (anuncioId == null) {
            imageView.setImageResource(R.drawable.espaco_image);
            return;
        }

        // Buscar anúncio completo para obter a imagem
        RetrofitClient.getApiService(context).getAnuncioPorId(anuncioId).enqueue(new Callback<ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse>() {
            @Override
            public void onResponse(Call<ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse> call, 
                                 Response<ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String urlImagem = response.body().getImagemUrl();
                    String fullUrl;

                    if (urlImagem != null && !urlImagem.isEmpty()) {
                        String baseUrl = RetrofitClient.BASE_URL;
                        if (baseUrl.endsWith("/")) {
                            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
                        }

                        String path = urlImagem;
                        if (!path.startsWith("/") && !path.startsWith("http://") && !path.startsWith("https://")) {
                            path = "/" + path;
                        }

                        if (path.startsWith("http://") || path.startsWith("https://")) {
                            fullUrl = path;
                        } else {
                            fullUrl = baseUrl + path;
                        }

                        imageView.setImageTintList(null);
                        imageView.setImageTintMode(null);
                        imageView.clearColorFilter();
                        imageView.setBackgroundColor(Color.TRANSPARENT);

                        Glide.with(context)
                                .load(fullUrl)
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(24)))
                                .placeholder(R.drawable.espaco_image)
                                .error(R.drawable.espaco_image)
                                .into(imageView);
                    } else {
                        imageView.setImageResource(R.drawable.espaco_image);
                    }
                } else {
                    imageView.setImageResource(R.drawable.espaco_image);
                }
            }

            @Override
            public void onFailure(Call<ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse> call, Throwable t) {
                imageView.setImageResource(R.drawable.espaco_image);
            }
        });
    }

    // Método para buscar anúncio completo e abrir dialog
    private void buscarAnuncioCompleto(Long anuncioId, int position, ViewHolder holder) {
        if (anuncioId == null) {
            // Se não tiver anuncioId, usar conversão básica
            Notificacao notif = lista.get(position);
            Anuncio anuncio = notif.toAnuncio();
            abrirDialog(anuncio, position, holder);
            return;
        }

        RetrofitClient.getApiService(context).getAnuncioPorId(anuncioId).enqueue(new Callback<ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse>() {
            @Override
            public void onResponse(Call<ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse> call, 
                                 Response<ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Converter AnuncioResponse para Anuncio
                    ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse anuncioResponse = response.body();
                    Anuncio anuncio = anuncioResponse.toAnuncio();
                    abrirDialog(anuncio, position, holder);
                } else {
                    // Fallback: usar conversão básica
                    Notificacao notif = lista.get(position);
                    Anuncio anuncio = notif.toAnuncio();
                    abrirDialog(anuncio, position, holder);
                }
            }

            @Override
            public void onFailure(Call<ao.co.isptec.aplm.projetoanuncioloc.Model.AnuncioResponse> call, Throwable t) {
                // Fallback: usar conversão básica
                Notificacao notif = lista.get(position);
                Anuncio anuncio = notif.toAnuncio();
                abrirDialog(anuncio, position, holder);
            }
        });
    }

    private void abrirDialog(Anuncio anuncio, int position, ViewHolder holder) {
        VisualizarAnuncioDialog dialog = VisualizarAnuncioDialog.newInstance(
                anuncio,
                position,
                (positionCallback, saved) -> {
                    if (saved) {
                        holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_salvo);
                        onSaveClickListener.onSaveClick(lista.get(positionCallback), positionCallback);
                    } else {
                        holder.btnSalvar.setImageResource(R.drawable.ic_bookmark_nao_salvo);
                    }
                }
        );
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "VisualizarAnuncio");
    }

    // VIEWHOLDER
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvDescricao;
        ImageView btnSalvar, imgAnuncio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tv_titulo);
            tvDescricao = itemView.findViewById(R.id.tv_descricao);
            btnSalvar = itemView.findViewById(R.id.btn_salvar);
            imgAnuncio = itemView.findViewById(R.id.img_anuncio);
        }
    }
}