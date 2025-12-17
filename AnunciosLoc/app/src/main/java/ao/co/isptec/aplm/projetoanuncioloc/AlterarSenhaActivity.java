package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ao.co.isptec.aplm.projetoanuncioloc.Request.AlterarSenhaRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.User;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlterarSenhaActivity extends AppCompatActivity {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnConfirmPassword;
    private ImageButton btnBack;  // ← SÓ ESTE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mudar_senha);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnConfirmPassword = findViewById(R.id.btnConfirmPassword);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());  // ← VOLTA COM A SETA

        btnConfirmPassword.setOnClickListener(v -> tentarMudarSenha());
    }

    private void tentarMudarSenha() {
        String atual = etCurrentPassword.getText().toString().trim();
        String nova = etNewPassword.getText().toString().trim();
        String confirmar = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(atual)) {
            etCurrentPassword.setError("Digite a senha atual");
            etCurrentPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nova)) {
            etNewPassword.setError("Digite a nova senha");
            etNewPassword.requestFocus();
            return;
        }

        if (nova.length() < 6) {
            etNewPassword.setError("A senha deve ter pelo menos 6 caracteres");
            etNewPassword.requestFocus();
            return;
        }

        if (!nova.equals(confirmar)) {
            etConfirmPassword.setError("As senhas não coincidem");
            etConfirmPassword.requestFocus();
            return;
        }

        // PEGA USERID DO SHARED PREFERENCES
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long userId = prefs.getLong("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Erro: Faça login novamente", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // CHAMA O BACKEND
        AlterarSenhaRequest req = new AlterarSenhaRequest(atual, nova);
        Call<User> call = RetrofitClient.getApiService(this).alterarSenha(userId, req);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AlterarSenhaActivity.this, "Senha alterada com sucesso!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AlterarSenhaActivity.this, "Erro: Senha atual incorreta ou servidor", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(AlterarSenhaActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}