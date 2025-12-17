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

import ao.co.isptec.aplm.projetoanuncioloc.Model.Local;
import ao.co.isptec.aplm.projetoanuncioloc.Request.LocalRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditarGPSDialog extends DialogFragment {

    private Local local;
    private OnLocalEditadoListener listener;

    private EditText etNomeLocal, etLatitude, etLongitude, etRaio;
    private Button btnCancelar, btnSalvar;

    public interface OnLocalEditadoListener {
        void onLocalEditado(String nomeEditado, double latEditada, double lngEditada, int raioEditado);
    }

    public static EditarGPSDialog newInstance(Local local, OnLocalEditadoListener listener) {
        EditarGPSDialog dialog = new EditarGPSDialog();
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
        View view = inflater.inflate(R.layout.activity_add_gps, container, false);
        initViews(view);
        preencherDados();
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        etNomeLocal = view.findViewById(R.id.etNomeLocal);
        etLatitude = view.findViewById(R.id.etLatitude);
        etLongitude = view.findViewById(R.id.etLongitude);
        etRaio = view.findViewById(R.id.etRaio);
        btnCancelar = view.findViewById(R.id.btnCancelar);

        // CORREÇÃO: Usar o ID correto e renomear para btnSalvar
        btnSalvar = view.findViewById(R.id.btnAdicionar);

        // Mudar texto do botão para "Salvar"
        btnSalvar.setText("Salvar");

        // Esconder botão WiFi pois estamos editando GPS
        Button btnWifi = view.findViewById(R.id.btnWifi);
        if (btnWifi != null) {
            btnWifi.setVisibility(View.GONE);
        }

        // Esconder o switch de mapeamento no modo edição
        View switchMapearLayout = view.findViewById(R.id.switchMapear);
        if (switchMapearLayout != null) {
            switchMapearLayout.setVisibility(View.GONE);
        }

        // Esconder o mapa no modo edição (opcional)
        View fragmentoMapa = view.findViewById(R.id.fragmentoMapa);
        if (fragmentoMapa != null) {
            fragmentoMapa.setVisibility(View.GONE);
        }
    }

    private void preencherDados() {
        if (local != null) {
            etNomeLocal.setText(local.getNome());
            etLatitude.setText(String.valueOf(local.getLatitude()));
            etLongitude.setText(String.valueOf(local.getLongitude()));
            etRaio.setText(String.valueOf(local.getRaio()));
        }
    }

    private void setupClickListeners() {
        btnCancelar.setOnClickListener(v -> dismiss());

        // CORREÇÃO: Usar btnSalvar
        btnSalvar.setOnClickListener(v -> {
            String nome = etNomeLocal.getText().toString().trim();
            String latStr = etLatitude.getText().toString().trim();
            String lngStr = etLongitude.getText().toString().trim();
            String raioStr = etRaio.getText().toString().trim();

            if (nome.isEmpty() || latStr.isEmpty() || lngStr.isEmpty() || raioStr.isEmpty()) {
                Toast.makeText(requireContext(), "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);
                int raio = Integer.parseInt(raioStr);

                // Atualizar local via API
                atualizarLocalNoBackend(nome, lat, lng, raio);

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Verifique os valores numéricos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarLocalNoBackend(String nome, double lat, double lng, int raio) {
        LocalRequest request = new LocalRequest(nome, "GPS", lat, lng, raio, null);

        Call<Local> call = RetrofitClient.getApiService(requireContext()).atualizarLocal(local.getId(), request);
        call.enqueue(new Callback<Local>() {
            @Override
            public void onResponse(Call<Local> call, Response<Local> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Local atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onLocalEditado(nome, lat, lng, raio);
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