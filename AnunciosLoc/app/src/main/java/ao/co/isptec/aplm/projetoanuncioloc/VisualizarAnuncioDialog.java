// VisualizarAnuncioDialog.java (VERSÃO ATUALIZADA)
package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Anuncio;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;


public class VisualizarAnuncioDialog extends DialogFragment {

    private static final String ARG_ANUNCIO = "anuncio";
    private static final String ARG_POSITION = "position";  // Novo: para posição

    // INTERFACE DE CALLBACK (nova)
    public interface OnBookmarkToggleListener {
        void onBookmarkToggled(int position, boolean saved);
    }

    private OnBookmarkToggleListener listener;  // Novo: listener
    private int position;  // Novo: posição do item na lista

    public static VisualizarAnuncioDialog newInstance(Anuncio anuncio, int position, OnBookmarkToggleListener listener) {
        VisualizarAnuncioDialog fragment = new VisualizarAnuncioDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANUNCIO, anuncio);
        args.putInt(ARG_POSITION, position);  // Novo
        fragment.setArguments(args);
        fragment.listener = listener;  // Novo
        return fragment;
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
        return inflater.inflate(R.layout.activity_visualizar_anuncio_e_guardar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Anuncio anuncio = getArguments() != null ? getArguments().getParcelable(ARG_ANUNCIO) : null;
        position = getArguments() != null ? getArguments().getInt(ARG_POSITION, -1) : -1;  // Novo: lê posição

        if (anuncio == null) {
            Toast.makeText(requireContext(), "Erro: Anúncio não encontrado", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        // === PREENCHE DADOS ===
        TextView title = view.findViewById(R.id.announcementTitle);
        TextView content = view.findViewById(R.id.announcementContent);
        ImageView img = view.findViewById(R.id.img_announcement);

        title.setText(anuncio.titulo);
        content.setText(anuncio.descricao);

        // === CARREGAMENTO DE IMAGEM - MESMA LÓGICA DO MAIN DIALOG ===
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
        } else {
            fullUrl = null;
        }

// REMOVER tint
        img.setImageTintList(null);
        img.setImageTintMode(null);
        img.clearColorFilter();

        if (fullUrl != null) {
            Glide.with(requireContext())
                    .load(fullUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(32)))
                    .placeholder(R.drawable.espaco_image)
                    .error(R.drawable.ic_close)
                    .into(img);
        } else {
            img.setImageResource(R.drawable.espaco_image);
        }

        // === BOTÃO VOLTAR (seta verde) ===
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> dismiss());

        // === BOTÃO GUARDAR (atualizado) ===
        ImageView icBookmark = view.findViewById(R.id.btn_save); // ou ic_bookmark_final
        CardView saveButton = view.findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> {
            anuncio.salvo = !anuncio.salvo;  // Altera na cópia local
            icBookmark.setImageResource(anuncio.salvo ?
                    R.drawable.ic_bookmark_salvo : R.drawable.ic_bookmark_nao_salvo);

            // NOVO: Notifica a lista original via callback
            if (listener != null && position != -1) {
                listener.onBookmarkToggled(position, anuncio.salvo);
            }

            Toast.makeText(requireContext(), anuncio.salvo ? "Guardado!" : "Removido!", Toast.LENGTH_LONG).show();
        });

        // Atualiza ícone ao abrir (já reflete o estado original)
        icBookmark.setImageResource(anuncio.salvo ?
                R.drawable.ic_bookmark_salvo : R.drawable.ic_bookmark_nao_salvo);
    }
}