package com.example.routinequestmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. CORREÇÃO DO BOTÃO: Agora ele é tratado como uma View (TextView) genérica
        View btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Lida com o Logout limpando as credenciais (ajuste se a sua lógica for diferente)
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(SettingsActivity.this, "Pacto rompido. Até logo!", Toast.LENGTH_SHORT).show();

            // Volta para a tela de Login (MainActivity)
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 2. CORREÇÃO DA NAVBAR: Inicializa a barra modularizada
        configurarNavbar();
    }

    private void configurarNavbar() {
        View navBar = findViewById(R.id.nav_bar);
        if (navBar == null) return;

        View navHome = navBar.findViewById(R.id.nav_home);
        View navCharacter = navBar.findViewById(R.id.nav_character);
        View navAttributes = navBar.findViewById(R.id.nav_attributes);
        View navSettings = navBar.findViewById(R.id.nav_settings);
        View navAddTask = navBar.findViewById(R.id.nav_add_task);

        // Destaca a aba Câmara como a ativa atual
        navSettings.post(() -> destacarAbaAtiva((android.view.ViewGroup) navSettings));

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
            finish(); // Fecha as configurações ao voltar para a Home
        });

        navCharacter.setOnClickListener(v ->
                Toast.makeText(this, "Tela do Herói em breve!", Toast.LENGTH_SHORT).show());

        navAddTask.setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, CreateTaskActivity.class)));

        navAttributes.setOnClickListener(v ->
                Toast.makeText(this, "Evolução e Atributos em breve!", Toast.LENGTH_SHORT).show());

        navSettings.setOnClickListener(v ->
                Toast.makeText(this, "Você já está na Câmara!", Toast.LENGTH_SHORT).show());
    }

    private void destacarAbaAtiva(android.view.ViewGroup abaAtiva) {
        View navBar = findViewById(R.id.nav_bar);
        if (navBar == null) return;

        android.view.ViewGroup navHome = navBar.findViewById(R.id.nav_home);
        android.view.ViewGroup navCharacter = navBar.findViewById(R.id.nav_character);
        android.view.ViewGroup navAttributes = navBar.findViewById(R.id.nav_attributes);
        android.view.ViewGroup navSettings = navBar.findViewById(R.id.nav_settings);

        android.view.ViewGroup[] abas = {navHome, navCharacter, navAttributes, navSettings};

        for (android.view.ViewGroup aba : abas) {
            if (aba == null) continue;

            android.widget.ImageView icone = (android.widget.ImageView) aba.getChildAt(0);
            android.widget.TextView texto = (android.widget.TextView) aba.getChildAt(1);

            icone.clearColorFilter();
            if (aba != navHome && aba != navCharacter) {
                icone.setColorFilter(android.graphics.Color.parseColor("#FFFFFF"), android.graphics.PorterDuff.Mode.SRC_ATOP);
            }

            texto.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));
            boolean isLogoPersonalizada = (aba == navHome || aba == navCharacter);

            if (aba == abaAtiva) {
                icone.setAlpha(1.0f);
                texto.setAlpha(1.0f);
                texto.setTypeface(null, android.graphics.Typeface.BOLD);
                float zoomAtivo = isLogoPersonalizada ? 1.45f : 1.35f;
                icone.animate().scaleX(zoomAtivo).scaleY(zoomAtivo).setDuration(300).start();
            } else {
                icone.setAlpha(0.4f);
                texto.setAlpha(0.4f);
                texto.setTypeface(null, android.graphics.Typeface.NORMAL);
                float zoomInativo = isLogoPersonalizada ? 1.25f : 1.0f;
                icone.animate().scaleX(zoomInativo).scaleY(zoomInativo).setDuration(300).start();
            }
        }
    }
}