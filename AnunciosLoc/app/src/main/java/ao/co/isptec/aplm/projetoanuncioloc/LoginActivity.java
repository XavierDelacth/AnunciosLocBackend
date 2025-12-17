package ao.co.isptec.aplm.projetoanuncioloc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import ao.co.isptec.aplm.projetoanuncioloc.Request.LoginRequest;
import ao.co.isptec.aplm.projetoanuncioloc.Model.User;
import ao.co.isptec.aplm.projetoanuncioloc.Service.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvTabRegister;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvTabRegister = findViewById(R.id.tv_tab_register);
        loadingDialog = new LoadingDialog(this);

        // Se já existe sessão (jwt + userId) no SharedPreferences, considera o utilizador como logado
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        Long savedUserId = prefs.getLong("userId", -1L);
        String savedJwt = prefs.getString("jwt", null);
        if (savedJwt != null && savedUserId != -1L) {
            // Redireciona diretamente para MainActivity — se o token não for válido, as chamadas em Main tratarão 401 e farão logout.
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        tvTabRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // MOSTRA O LOADING
            loadingDialog.setMessage("A autenticar...");
            loadingDialog.show();

            // CHAMA API
            LoginRequest request = new LoginRequest(username, password);
            Call<User> call = RetrofitClient.getApiService(this).login(request);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    loadingDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        String jwt = user.getSessionId();
                        Long userId = user.getId();
                        String username = user.getUsername();

                        // SALVA NO SHARED PREFERENCES
                        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                        prefs.edit()
                                .putLong("userId", userId)
                                .putString("jwt", jwt)
                                .putString("username", username)
                                .apply();
                        Log.d("LoginActivity", "JWT salvo: " + prefs.getString("jwt", "Nenhum"));

                            // BUSCA OS PERFIS DO UTILIZADOR NO SERVIDOR e salva localmente
                            RetrofitClient.getApiService(LoginActivity.this).getUserPerfis(userId)
                                    .enqueue(new Callback<Map<String, String>>() {
                                        @Override
                                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> resp) {
                                            if (resp.isSuccessful() && resp.body() != null) {
                                                Map<String, String> serverMap = resp.body();
                                                // converte Map<String, String(comma-separated)> para Map<String, List<String>>
                                                Map<String, List<String>> selections = new HashMap<>();
                                                for (Map.Entry<String, String> e : serverMap.entrySet()) {
                                                    String k = e.getKey();
                                                    String v = e.getValue();
                                                    if (v == null || v.isEmpty()) continue;
                                                    String[] parts = v.split(",");
                                                    List<String> vals = new ArrayList<>();
                                                    for (String p : parts) {
                                                        String trimmed = p.trim();
                                                        if (!trimmed.isEmpty()) vals.add(trimmed);
                                                    }
                                                    if (!vals.isEmpty()) selections.put(k, vals);
                                                }
                                                // grava em SharedPreferences no mesmo formato usado por PerfilActivity
                                                SharedPreferences selPrefs = getSharedPreferences("my_profile_selections_" + userId, MODE_PRIVATE);
                                                Gson g = new Gson();
                                                String json = g.toJson(selections);
                                                selPrefs.edit().putString("selections", json).apply();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                                            // não impedimos o login; apenas logamos
                                        }
                                    });

                        Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_LONG).show();

                        // VAI PARA MAIN
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("username", user.getUsername());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Usuário ou senha incorretos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    loadingDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();

                }
            });
        });
    }
}