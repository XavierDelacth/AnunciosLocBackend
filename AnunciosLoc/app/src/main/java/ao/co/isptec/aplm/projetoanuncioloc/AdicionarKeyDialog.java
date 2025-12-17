package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;


public class AdicionarKeyDialog extends DialogFragment {

    private List<ProfileKey> allKeys;
    private Map<String, List<String>> mySelectedKeys;
    private OnKeyAddedListener listener;

    public interface OnKeyAddedListener {
        void onKeyAdded(String keyName, List<String> values);
    }

    public static AdicionarKeyDialog newInstance(List<ProfileKey> allKeys, Map<String, List<String>> mySelectedKeys) {
        AdicionarKeyDialog fragment = new AdicionarKeyDialog();
        fragment.allKeys = new ArrayList<>(allKeys); // COPIA A LISTA!
        fragment.mySelectedKeys = new HashMap<>(mySelectedKeys);
        return fragment;
    }

    public void setOnKeyAddedListener(OnKeyAddedListener listener) {
        this.listener = listener;
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
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_add_chaves, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnClose = view.findViewById(R.id.btn_close_dialog);
        CardView cardAddToExisting = view.findViewById(R.id.card_add_to_existing);
        CardView cardCreateNew = view.findViewById(R.id.card_create_new_key);

        btnClose.setOnClickListener(v -> dismiss());

        cardAddToExisting.setOnClickListener(v -> {
            // Abrir diálogo de seleção de chave existente
            SelectExistingKeyDialog dialog = SelectExistingKeyDialog.newInstance(allKeys);
            dialog.setOnKeySelectedListener(selectedKey -> {
                // Abrir diálogo de adicionar valor
                AddValorChaveExistenteDialog addValueDialog = AddValorChaveExistenteDialog.newInstance(selectedKey);
                addValueDialog.setOnValueAddedListener(newValue -> {
                    if (listener != null) {
                        List<String> values = new java.util.ArrayList<>();
                        values.add(newValue);
                        listener.onKeyAdded(selectedKey.getName(), values);
                    }
                    dismiss();
                });
                addValueDialog.show(getParentFragmentManager(), "AddValueDialog");
            });
            dialog.show(getParentFragmentManager(), "SelectKeyDialog");
        });

        cardCreateNew.setOnClickListener(v -> {
            // Abrir diálogo de criar nova chave
            CriarNovaChaveDialog dialog = new CriarNovaChaveDialog();
            dialog.setOnKeyCreatedListener((keyName, values) -> {
                if (listener != null) {
                    listener.onKeyAdded(keyName, values);
                }
                dismiss();
            });
            dialog.show(getParentFragmentManager(), "CreateKeyDialog");
        });
    }
}