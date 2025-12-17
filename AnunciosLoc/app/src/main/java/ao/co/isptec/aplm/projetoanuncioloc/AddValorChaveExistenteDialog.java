package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;

public class AddValorChaveExistenteDialog extends DialogFragment {

    private ProfileKey selectedKey;
    private OnValueAddedListener listener;

    public interface OnValueAddedListener {
        void onValueAdded(String value);
    }

    public static AddValorChaveExistenteDialog newInstance(ProfileKey key) {
        AddValorChaveExistenteDialog fragment = new AddValorChaveExistenteDialog();
        fragment.selectedKey = key;
        return fragment;
    }

    public void setOnValueAddedListener(OnValueAddedListener listener) {
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
        return inflater.inflate(R.layout.activity_add_key_value, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        ImageButton btnClose = view.findViewById(R.id.btn_close_dialog);
        TextView tvKeyIcon = view.findViewById(R.id.tv_key_icon);
        TextView tvSelectedKeyName = view.findViewById(R.id.tv_selected_key);
        EditText etNewValue = view.findViewById(R.id.et_new);
        Button btnConfirm = view.findViewById(R.id.btn_confirm_add);

        // Set selected key info
        if (selectedKey != null) {
            tvKeyIcon.setText(selectedKey.getName().substring(0, 1).toUpperCase());
            tvSelectedKeyName.setText(selectedKey.getName());
        }

        btnBack.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(v -> {
            String newValue = etNewValue.getText().toString().trim();

            if (newValue.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, digite um valor", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if value already exists
            if (selectedKey != null && selectedKey.getAvailableValues().contains(newValue)) {
                Toast.makeText(getContext(), "Este valor já existe nesta chave", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onValueAdded(newValue);
            }

            dismiss();
        });
    }
}