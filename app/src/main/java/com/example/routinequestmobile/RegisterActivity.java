package com.example.routinequestmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etRegisterName);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeDigitado = etName.getText().toString().trim();
                String emailDigitado = etEmail.getText().toString().trim();
                String senhaDigitada = etPassword.getText().toString();
                String confirmarSenhaDigitada = etConfirmPassword.getText().toString();

                if (nomeDigitado.isEmpty() || emailDigitado.isEmpty()
                        || senhaDigitada.isEmpty() || confirmarSenhaDigitada.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailDigitado).matches()) {
                    Toast.makeText(RegisterActivity.this, "Digite um e-mail válido!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!senhaDigitada.equals(confirmarSenhaDigitada)) {
                    Toast.makeText(RegisterActivity.this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
                    return;
                }

                RegisterRequest registerRequest =
                        new RegisterRequest(nomeDigitado, emailDigitado, senhaDigitada);

                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                Call<Void> call = apiService.registerUser(registerRequest);

                call.enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Personagem criado com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erro ao criar conta. Email já existe?", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        tvBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Fecha a tela de cadastro e volta para a anterior (Login)
            }
        });
    }
}