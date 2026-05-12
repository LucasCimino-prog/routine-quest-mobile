package com.example.routinequestmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Preencha tudo!", Toast.LENGTH_SHORT).show();
                    return;
                }

                LoginRequest loginRequest = new LoginRequest(email, password);
                ApiService apiService = ApiClient.getClient().create(ApiService.class);

                // Chamada espera um LoginResponse
                apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String tokenPuro = response.body().getToken();
                            Long userId = response.body().getUserId();

                            getSharedPreferences("USER_PREFS", MODE_PRIVATE)
                                    .edit()
                                    .putString("token", tokenPuro)
                                    .putLong("USER_ID", userId)
                                    .apply();

                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Falha no Login!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }
}