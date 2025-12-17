package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.projetoanuncioloc.Adapters.SelectableKeyAdapter;
import ao.co.isptec.aplm.projetoanuncioloc.Model.ProfileKey;

public class SelectExistingKeyDialog extends DialogFragment {

    private List<ProfileKey> allKeys;
    private SelectableKeyAdapter adapter;
    private OnKeySelectedListener listener;
    private LinearLayout layoutEmpty;

    public interface OnKeySelectedListener {
        void onKeySelected(ProfileKey key);
    }

    public static SelectExistingKeyDialog newInstance(List<ProfileKey> allKeys) {
        SelectExistingKeyDialog fragment = new SelectExistingKeyDialog();
        fragment.allKeys = allKeys;
        return fragment;
    }

    public void setOnKeySelectedListener(OnKeySelectedListener listener) {
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
        return inflater.inflate(R.layout.activity_existente_key, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btn_back);
        ImageButton btnClose = view.findViewById(R.id.btn_close_dialog);
        EditText etSearch = view.findViewById(R.id.et_search_key);
        RecyclerView rvKeys = view.findViewById(R.id.rv_existing_keys);
        layoutEmpty = view.findViewById(R.id.layout_empty_keys);

        btnBack.setOnClickListener(v -> dismiss());
        btnClose.setOnClickListener(v -> dismiss());

        // Setup RecyclerView
        adapter = new SelectableKeyAdapter(getContext(), allKeys);
        adapter.setOnKeyClickListener(key -> {
            if (listener != null) {
                listener.onKeySelected(key);
            }
            dismiss();
        });

        rvKeys.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKeys.setAdapter(adapter);

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterKeys(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        updateEmptyState();
    }

    private void filterKeys(String query) {
        List<ProfileKey> filtered = new ArrayList<>();

        for (ProfileKey key : allKeys) {
            if (key.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(key);
            }
        }

        adapter.updateKeys(filtered);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}