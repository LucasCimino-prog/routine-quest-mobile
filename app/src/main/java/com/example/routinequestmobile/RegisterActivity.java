package com.example.routinequestmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.Color;

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

        // Mapeando a CAIXA inteira das regras (Nova linha)
        LinearLayout llPasswordRules = findViewById(R.id.llPasswordRules);

        // Mapeando os textos individuais
        TextView tvRuleLength = findViewById(R.id.tvRuleLength);
        TextView tvRuleUpper = findViewById(R.id.tvRuleUpper);
        TextView tvRuleLower = findViewById(R.id.tvRuleLower);
        TextView tvRuleNumber = findViewById(R.id.tvRuleNumber);
        TextView tvRuleSpecial = findViewById(R.id.tvRuleSpecial);

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();

                // Se digitou pelo menos 1 caractere, mostra a caixa. Se apagou tudo, esconde.
                if (password.length() > 0) {
                    llPasswordRules.setVisibility(View.VISIBLE);
                } else {
                    llPasswordRules.setVisibility(View.GONE);
                }

                // Regra 1: 8 caracteres
                if (password.length() >= 8) {
                    tvRuleLength.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    tvRuleLength.setTextColor(Color.parseColor("#80FFFFFF"));
                }

                // Regra 2: Maiúscula
                if (password.matches(".*[A-Z].*")) {
                    tvRuleUpper.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    tvRuleUpper.setTextColor(Color.parseColor("#80FFFFFF"));
                }

                // Regra 3: Minúscula
                if (password.matches(".*[a-z].*")) {
                    tvRuleLower.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    tvRuleLower.setTextColor(Color.parseColor("#80FFFFFF"));
                }

                // Regra 4: Número
                if (password.matches(".*[0-9].*")) {
                    tvRuleNumber.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    tvRuleNumber.setTextColor(Color.parseColor("#80FFFFFF"));
                }

                // Regra 5: Símbolo Especial
                if (password.matches(".*[@#$%^&+=!].*")) {
                    tvRuleSpecial.setTextColor(Color.parseColor("#4CAF50"));
                } else {
                    tvRuleSpecial.setTextColor(Color.parseColor("#80FFFFFF"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeDigitado = etName.getText().toString().trim();
                String emailDigitado = etEmail.getText().toString().trim();
                String senhaDigitada = etPassword.getText().toString();
                String confirmarSenhaDigitada = etConfirmPassword.getText().toString();

                // 1. Verifica se tem algum campo vazio
                if (nomeDigitado.isEmpty() || emailDigitado.isEmpty()
                        || senhaDigitada.isEmpty() || confirmarSenhaDigitada.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2. Verifica se o e-mail tem um formato válido
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailDigitado).matches()) {
                    Toast.makeText(RegisterActivity.this, "Digite um e-mail válido!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 3. NOVA REGRA: Verifica se a senha é forte
                if (!isSenhaForte(senhaDigitada)) {
                    Toast.makeText(RegisterActivity.this, "A senha deve ter no mínimo 8 caracteres, incluindo maiúsculas, minúsculas, números e símbolos!", Toast.LENGTH_LONG).show();
                    return;
                }

                // 4. Verifica se as senhas batem
                if (!senhaDigitada.equals(confirmarSenhaDigitada)) {
                    Toast.makeText(RegisterActivity.this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Se passou por todas as barreiras, envia para a API!
                RegisterRequest registerRequest =
                        new RegisterRequest(nomeDigitado, emailDigitado, senhaDigitada);

                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                Call<Void> call = apiService.registerUser(registerRequest);

                call.enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Aventureiro criado com sucesso!", Toast.LENGTH_SHORT).show();
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

    private boolean isSenhaForte(String senha) {
        // Regra: Mínimo de 8 caracteres, 1 maiúscula, 1 minúscula, 1 número e 1 caractere especial
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
        return senha.matches(regex);
    }
}