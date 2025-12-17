package ao.co.isptec.aplm.projetoanuncioloc;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Arrays;

import android.content.SharedPreferences;

import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.Request.LocalRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import ao.co.isptec.aplm.projetoanuncioloc.Interface.OnLocalAddedListener;

public class AdicionarWIFIDialog extends DialogFragment {

    private static final String TAG = "AdicionarWIFIDialog";

    private EditText etNomeLocalWiFi, etSSID;
    private Button btnCancelarWiFi, btnAdicionarWiFi, btnGps;
    private ImageView btnFecharWiFi;

    // Interface comum para ambos diálogos (GPS e WiFi)
    private OnLocalAddedListener listener;

    // Factory method para newInstance
    public static AdicionarWIFIDialog newInstance(OnLocalAddedListener listener) {
        AdicionarWIFIDialog dialog = new AdicionarWIFIDialog();
        dialog.listener = listener;
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLocalAddedListener) {
            listener = (OnLocalAddedListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
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
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_wifi, container, false);
        initViews(view);
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        Log.d(TAG, "Inicializando views do WiFi Dialog");

        etNomeLocalWiFi = view.findViewById(R.id.etNomeLocalWiFi);
        etSSID = view.findViewById(R.id.etSSID);
        btnCancelarWiFi = view.findViewById(R.id.btnCancelarWiFi);
        btnAdicionarWiFi = view.findViewById(R.id.btnAdicionarWiFi);
        btnGps = view.findViewById(R.id.btnGpsToggle);
        btnFecharWiFi = view.findViewById(R.id.btnFecharWiFi);

        // Validação de views
        if (btnGps == null) {
            Log.e(TAG, "ERRO: btnGps não encontrado no layout! Verifique o ID no XML.");
        } else {
            Log.d(TAG, "btnGps encontrado com sucesso");
        }
    }

    private void setupClickListeners() {
        Log.d(TAG, "Configurando listeners");

        // Botão fechar
        if (btnFecharWiFi != null) {
            btnFecharWiFi.setOnClickListener(v -> {
                Log.d(TAG, "Botão fechar clicado");
                dismiss();
            });
        }

        // Botão cancelar
        if (btnCancelarWiFi != null) {
            btnCancelarWiFi.setOnClickListener(v -> {
                Log.d(TAG, "Botão cancelar clicado");
                dismiss();
            });
        }

        // CRÍTICO: Botão GPS - Volta para o GPS Dialog
        if (btnGps != null) {
            btnGps.setOnClickListener(v -> {
                Log.d(TAG, "Botão GPS clicado - Alternando para GPS Dialog");

                // Fecha este diálogo WiFi primeiro
                dismiss();

                // Usa postDelayed para garantir que o dismiss complete antes de abrir o novo
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            // Abre o GPS Dialog com o mesmo listener
                            AdicionarGPSDialog gpsDialog = AdicionarGPSDialog.newInstance(listener);
                            gpsDialog.show(getParentFragmentManager(), "AdicionarGPSDialog");
                            Log.d(TAG, "GPS Dialog aberto com sucesso");
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao abrir GPS Dialog: " + e.getMessage());
                            Toast.makeText(getContext(), "Erro ao alternar para GPS", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            Log.e(TAG, "btnGps é null - não pode configurar listener!");
        }

        // ✅ CORREÇÃO PRINCIPAL: Botão adicionar WiFi - AGORA COM CHAMADA À API
        if (btnAdicionarWiFi != null) {
            btnAdicionarWiFi.setOnClickListener(v -> {
                Log.d(TAG, "Botão adicionar WiFi clicado");

                String nome = etNomeLocalWiFi.getText().toString().trim();
                String ssid = etSSID.getText().toString().trim();

                // Validação
                if (nome.isEmpty() || ssid.isEmpty()) {
                    if (nome.isEmpty()) {
                        etNomeLocalWiFi.setError("Nome obrigatório");
                        etNomeLocalWiFi.requestFocus();
                    }
                    if (ssid.isEmpty()) {
                        etSSID.setError("SSID obrigatório");
                        if (nome.isEmpty()) etSSID.requestFocus();
                    }
                    Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ PEGA USERID DO SHARED PREFS (igual ao GPS)
                SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
                Long userId = prefs.getLong("userId", -1L);
                if (userId == -1L) {
                    Toast.makeText(requireContext(), "Erro: Faça login novamente", Toast.LENGTH_LONG).show();
                    return;
                }

                // ✅ CRIA OBJETO PARA ENVIO (tipo WIFI)
                LocalRequest request = new LocalRequest(
                        nome,
                        "WIFI",  // Tipo WIFI
                        null,    // latitude = null para WiFi
                        null,    // longitude = null para WiFi
                        null,    // raio = null para WiFi
                        Arrays.asList(ssid)  // wifiIds com o SSID
                );

                // ✅ ENVIA PARA O BACKEND (igual ao GPS)
                Call<Local> call = RetrofitClient.getApiService(requireContext()).criarLocal(request, userId);
                call.enqueue(new Callback<Local>() {
                    @Override
                    public void onResponse(Call<Local> call, Response<Local> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(requireContext(), "Local WiFi adicionado com sucesso!", Toast.LENGTH_LONG).show();

                            // ✅ AVISA A ACTIVITY QUE ADICIONOU (atualiza lista)
                            if (listener != null) {
                                listener.onLocalAddedWiFi(nome, Arrays.asList(ssid));
                            }
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Erro no servidor: " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Local> call, Throwable t) {
                        Toast.makeText(requireContext(), "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        Log.d(TAG, "Listeners configurados com sucesso");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "WiFi Dialog destruído");
    }
}