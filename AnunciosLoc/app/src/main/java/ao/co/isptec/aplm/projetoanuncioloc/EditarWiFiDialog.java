package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Arrays;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.Request.LocalRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarWiFiDialog extends DialogFragment {

    private Local local;
    private OnLocalEditadoListener listener;

    private EditText etNomeLocalWiFi, etSSID;
    private Button btnCancelar, btnSalvar;

    public interface OnLocalEditadoListener {
        void onLocalEditado(String nomeEditado, String ssidEditado);
    }

    // MUDANÇA: Receber Local em vez de strings separadas
    public static EditarWiFiDialog newInstance(Local local, OnLocalEditadoListener listener) {
        EditarWiFiDialog dialog = new EditarWiFiDialog();
        Bundle args = new Bundle();
        args.putSerializable("local", local);
        dialog.setArguments(args);
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            local = (Local) getArguments().getSerializable("local");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_wifi, container, false);
        initViews(view);
        preencherDados();
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        etNomeLocalWiFi = view.findViewById(R.id.etNomeLocalWiFi);
        etSSID = view.findViewById(R.id.etSSID);
        btnCancelar = view.findViewById(R.id.btnCancelarWiFi);

        // CORREÇÃO: Usar ID correto
        btnSalvar = view.findViewById(R.id.btnAdicionarWiFi);

        // Mudar texto do botão para "Salvar"
        btnSalvar.setText("Salvar");

        // Esconder botão GPS pois estamos editando WiFi
        Button btnGps = view.findViewById(R.id.btnGpsToggle);
        if (btnGps != null) {
            btnGps.setVisibility(View.GONE);
        }

        // Esconder botão WiFi toggle
        Button btnWifiToggle = view.findViewById(R.id.btnWifiToggle);
        if (btnWifiToggle != null) {
            btnWifiToggle.setVisibility(View.GONE);
        }
    }

    private void preencherDados() {
        if (local != null) {
            etNomeLocalWiFi.setText(local.getNome());
            // Obter o primeiro SSID (assumindo que WiFi tem pelo menos um)
            if (local.getWifiIds() != null && !local.getWifiIds().isEmpty()) {
                etSSID.setText(local.getWifiIds().get(0));
            }
        }
    }

    private void setupClickListeners() {
        btnCancelar.setOnClickListener(v -> dismiss());

        btnSalvar.setOnClickListener(v -> {
            String nome = etNomeLocalWiFi.getText().toString().trim();
            String ssid = etSSID.getText().toString().trim();

            if (nome.isEmpty() || ssid.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Atualizar local via API
            atualizarLocalNoBackend(nome, ssid);
        });
    }

    private void atualizarLocalNoBackend(String nome, String ssid) {
        LocalRequest request = new LocalRequest(nome, "WIFI", null, null, null, Arrays.asList(ssid));

        Call<Local> call = RetrofitClient.getApiService(requireContext()).atualizarLocal(local.getId(), request);
        call.enqueue(new Callback<Local>() {
            @Override
            public void onResponse(Call<Local> call, Response<Local> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Local WiFi atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onLocalEditado(nome, ssid);
                    }
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Erro ao atualizar local: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Local> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha na rede: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}