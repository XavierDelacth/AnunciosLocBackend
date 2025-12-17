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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class CriarNovaChaveDialog extends DialogFragment {

    private EditText etKeyName;
    private LinearLayout containerValues;
    private Button btnAddValueField;
    private Button btnCreateKey;
    private List<EditText> valueFields;
    private int valueFieldCounter = 1;
    private OnKeyCreatedListener listener;
    private View rootView; // GUARDA A VIEW RAIZ

    public interface OnKeyCreatedListener {
        void onKeyCreated(String keyName, List<String> values);
    }

    public void setOnKeyCreatedListener(OnKeyCreatedListener listener) {
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
        return inflater.inflate(R.layout.activity_create_new_key, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view; // GUARDA A VIEW!

        etKeyName = view.findViewById(R.id.et_key_name);
        containerValues = view.findViewById(R.id.container_values);
        btnAddValueField = view.findViewById(R.id.btn_add_value_field);
        btnCreateKey = view.findViewById(R.id.btn_create_key);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        ImageButton btnClose = view.findViewById(R.id.btn_close_dialog);

        valueFields = new ArrayList<>();

        // PRIMEIRO CAMPO (OBRIGATÓRIO)
        EditText firstField = view.findViewById(R.id.et_value_1);
        if (firstField != null) {
            valueFields.add(firstField);
        }

        btnBack.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());

        btnAddValueField.setOnClickListener(v -> addNewValueField());

        btnCreateKey.setOnClickListener(v -> createKey());
    }

    private void addNewValueField() {
        valueFieldCounter++;

        LinearLayout fieldContainer = new LinearLayout(getContext());
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.bottomMargin = dpToPx(12);
        fieldContainer.setLayoutParams(containerParams);
        fieldContainer.setOrientation(LinearLayout.HORIZONTAL);
        fieldContainer.setGravity(android.view.Gravity.CENTER_VERTICAL);

        EditText etValue = new EditText(getContext());
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(0, dpToPx(56));
        etParams.weight = 1;
        etValue.setLayoutParams(etParams);
        etValue.setHint("Valor " + valueFieldCounter);
        etValue.setTextSize(16);
        etValue.setMaxLines(1);
        etValue.setPadding(dpToPx(16), 0, dpToPx(16), 0);
        etValue.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_input_field));
        etValue.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_900));
        etValue.setHintTextColor(ContextCompat.getColor(getContext(), R.color.gray_500));

        ImageButton btnRemove = new ImageButton(getContext());
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        btnParams.leftMargin = dpToPx(8);
        btnRemove.setLayoutParams(btnParams);
        btnRemove.setImageResource(R.drawable.ic_close);
        btnRemove.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_circle_green));
        btnRemove.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        btnRemove.setImageTintList(ContextCompat.getColorStateList(getContext(), R.color.white));
        btnRemove.setContentDescription("Remover");

        btnRemove.setOnClickListener(v -> {
            containerValues.removeView(fieldContainer);
            valueFields.remove(etValue);
        });

        fieldContainer.addView(etValue);
        fieldContainer.addView(btnRemove);
        containerValues.addView(fieldContainer);
        valueFields.add(etValue);

        // SCROLL AUTOMÁTICO
        if (rootView != null) {
            rootView.post(() -> {
                ScrollView scrollView = rootView.findViewById(R.id.scroll_view);
                if (scrollView != null) {
                    scrollView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }
    }

    private void createKey() {
        String keyName = etKeyName.getText().toString().trim();

        if (keyName.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, digite o nome da chave", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> values = new ArrayList<>();
        for (EditText field : valueFields) {
            String value = field.getText().toString().trim();
            if (!value.isEmpty() && !values.contains(value)) {
                values.add(value);
            }
        }

        if (values.isEmpty()) {
            Toast.makeText(getContext(), "Adicione pelo menos um valor", Toast.LENGTH_SHORT).show();
            return;
        }

        if (listener != null) {
            listener.onKeyCreated(keyName, values);
        }

        dismiss();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}