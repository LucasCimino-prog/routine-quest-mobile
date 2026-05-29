package com.example.routinequestmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // AÇÃO DO BOTÃO DE LOGOUT
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Volte para a sua Activity de Login/Main
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // AÇÕES DA NAVBAR
        android.view.ViewGroup navHome = findViewById(R.id.nav_home);
        android.view.ViewGroup navCharacter = findViewById(R.id.nav_character);
        android.view.ViewGroup navAttributes = findViewById(R.id.nav_attributes);
        android.view.ViewGroup navSettings = findViewById(R.id.nav_settings);
        android.widget.ImageView navAddTask = findViewById(R.id.nav_add_task);

        //Avisa que a tela ATUAL é a de AJUSTES!
        navSettings.post(() -> destacarAbaAtiva(navSettings));

        // 1. Missões (Volta para a Home)
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HomeActivity.class);
            // Isso evita criar dezenas de telas "Home" na memória do celular
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Fecha a tela de ajustes
        });

        // 2. Personagem
        navCharacter.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Tela do Personagem em breve!", Toast.LENGTH_SHORT).show();
        });

        // 3. Adicionar Missão
        navAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, CreateTaskActivity.class);
            startActivity(intent);
        });

        // 4. Atributos
        navAttributes.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Evolução e Atributos em breve!", Toast.LENGTH_SHORT).show();
        });

        // 5. Configurações
        navSettings.setOnClickListener(v -> {
            Toast.makeText(SettingsActivity.this, "Você já está nos Ajustes!", Toast.LENGTH_SHORT).show();
        });
    }

    // MÉTODO PARA DESTACAR A ABA ATIVA
    private void destacarAbaAtiva(android.view.ViewGroup abaAtiva) {
        android.view.ViewGroup navHome = findViewById(R.id.nav_home);
        android.view.ViewGroup navCharacter = findViewById(R.id.nav_character);
        android.view.ViewGroup navAttributes = findViewById(R.id.nav_attributes);
        android.view.ViewGroup navSettings = findViewById(R.id.nav_settings);

        android.view.ViewGroup[] abas = {navHome, navCharacter, navAttributes, navSettings};

        for (android.view.ViewGroup aba : abas) {
            android.widget.ImageView icone = (android.widget.ImageView) aba.getChildAt(0);
            android.widget.TextView texto = (android.widget.TextView) aba.getChildAt(1);

            icone.clearColorFilter();

            // 1. CORREÇÃO DE COR: Agora protege TANTO a logo de Missões QUANTO o ícone de Perfil!
            if (aba != navHome && aba != navCharacter) {
                icone.setColorFilter(android.graphics.Color.parseColor("#FFFFFF"), android.graphics.PorterDuff.Mode.SRC_ATOP);
            }

            texto.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));

            // Variável para sabermos se estamos lidando com as logos grandes ou ícones pequenos
            boolean isLogoPersonalizada = (aba == navHome || aba == navCharacter);

            if (aba == abaAtiva) {
                // ESTADO ATIVO (Selecionado)
                icone.setAlpha(1.0f);
                texto.setAlpha(1.0f);
                texto.setTypeface(null, android.graphics.Typeface.BOLD);

                // O SEGREDO DO SUPER ZOOM: Se for logo, cresce para 1.45x (Gigante!). Se for ícone normal, 1.35x.
                float zoomAtivo = isLogoPersonalizada ? 1.45f : 1.35f;
                icone.animate().scaleX(zoomAtivo).scaleY(zoomAtivo).setDuration(300).start();

            } else {
                // ESTADO INATIVO (Descanso)
                icone.setAlpha(0.4f);
                texto.setAlpha(0.4f);
                texto.setTypeface(null, android.graphics.Typeface.NORMAL);

                // O SEGREDO DO TAMANHO PADRÃO: As logos inativas não voltam para 1.0x, elas param em 1.25x (que é o tamanho que você gostou). Os ícones pequenos voltam para 1.0x.
                float zoomInativo = isLogoPersonalizada ? 1.25f : 1.0f;
                icone.animate().scaleX(zoomInativo).scaleY(zoomInativo).setDuration(300).start();
            }
        }
    }
}