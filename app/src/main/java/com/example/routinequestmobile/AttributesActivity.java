package com.example.routinequestmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttributesActivity extends AppCompatActivity {

    private LinearLayout containerCronicas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attributes);

        containerCronicas = findViewById(R.id.containerCronicas);
        configurarNavbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Toda a vez que a aba de atributos for aberta, ele vai no MySQL e atualiza a tela
        carregarCronicasRecentes();
        carregarTotaisAtributos();
    }

    private void carregarTotaisAtributos() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        if (userId == -1L) {
            android.util.Log.e("RPG_DEBUG", "ERRO: O celular não sabe quem está logado (ID = -1)");
            return;
        }

        android.util.Log.e("RPG_DEBUG", "Buscando atributos no banco para o User ID: " + userId);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserStats(userId).enqueue(new Callback<UserStats>() {
            @Override
            public void onResponse(Call<UserStats> call, Response<UserStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserStats stats = response.body();

                    android.util.Log.e("RPG_DEBUG", "Dados recebidos -> INT: " + stats.getIntelligence() + " | FOR: " + stats.getStrength());

                    TextView tvInt = findViewById(R.id.tvIntTotal);
                    TextView tvFor = findViewById(R.id.tvForTotal);
                    TextView tvAgi = findViewById(R.id.tvAgiTotal);
                    TextView tvRes = findViewById(R.id.tvResTotal);

                    tvInt.setText("Ψ " + stats.getIntelligence());
                    tvFor.setText("⧟ " + stats.getStrength());
                    tvAgi.setText("ϟ " + stats.getAgility());
                    tvRes.setText("Ω " + stats.getResistance());

                    RadarChartView radar = findViewById(R.id.radarChart);
                    if (radar != null) {
                        radar.setStats(
                                stats.getIntelligence(),
                                stats.getStrength(),
                                stats.getAgility(),
                                stats.getResistance()
                        );
                    } // <--- ESTA ERA A CHAVETA QUE ESTAVA A FALTAR!
                } else {
                    android.util.Log.e("RPG_DEBUG", "Erro na API. Código: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserStats> call, Throwable t) {
                android.util.Log.e("RPG_DEBUG", "A API não respondeu: " + t.getMessage());
            }
        });
    }

    private void carregarCronicasRecentes() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        if (userId == -1L) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Task>> call = apiService.getUserTasks(userId);

        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> todasMissoes = response.body();
                    List<Task> missoesConcluidas = new ArrayList<>();

                    for (Task task : todasMissoes) {
                        if ("COMPLETED".equals(task.getStatus())) {
                            missoesConcluidas.add(task);
                        }
                    }

                    Collections.reverse(missoesConcluidas);
                    int limite = Math.min(missoesConcluidas.size(), 4);
                    containerCronicas.removeAllViews();

                    for (int i = 0; i < limite; i++) {
                        adicionarCronicaNaTela(missoesConcluidas.get(i));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                // Erro silencioso tratado na outra chamada
            }
        });
    }

    private void adicionarCronicaNaTela(Task task) {
        View viewCronica = LayoutInflater.from(this).inflate(R.layout.item_cronica, containerCronicas, false);

        TextView tvIcone = viewCronica.findViewById(R.id.tvCronicaIcone);
        TextView tvTitulo = viewCronica.findViewById(R.id.tvCronicaTitulo);
        TextView tvRecompensa = viewCronica.findViewById(R.id.tvCronicaRecompensa);

        tvTitulo.setText(task.getName() != null ? task.getName() : "Missão Misteriosa");

        tvIcone.setTextColor(Color.parseColor("#FFB300"));
        tvRecompensa.setTextColor(Color.parseColor("#FFB300"));

        String atributo = task.getAttributeType();
        if (atributo == null) atributo = "UNKNOWN";

        switch (atributo) {
            case "INTELLIGENCE":
                tvIcone.setText("Ψ");
                tvRecompensa.setText("+1 INT");
                break;
            case "STRENGTH":
                tvIcone.setText("⧟");
                tvRecompensa.setText("+1 FOR");
                break;
            case "AGILITY":
                tvIcone.setText("ϟ");
                tvRecompensa.setText("+1 AGI");
                break;
            case "RESISTANCE":
                tvIcone.setText("Ω");
                tvRecompensa.setText("+1 RES");
                break;
            default:
                tvIcone.setText("◈");
                tvRecompensa.setText("+" + task.getXpReward() + " XP");
                break;
        }

        containerCronicas.addView(viewCronica);
    }

    private void configurarNavbar() {
        View navHome = findViewById(R.id.nav_home);
        View navCharacter = findViewById(R.id.nav_character);
        View navAttributes = findViewById(R.id.nav_attributes);
        View navSettings = findViewById(R.id.nav_settings);
        View navAddTask = findViewById(R.id.nav_add_task);

        if (navAttributes != null) {
            navAttributes.post(() -> destacarAbaAtiva((android.view.ViewGroup) navAttributes));
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(AttributesActivity.this, HomeActivity.class));
                finish();
            });
        }

        if (navCharacter != null) {
            navCharacter.setOnClickListener(v -> {
                startActivity(new Intent(AttributesActivity.this, CharacterActivity.class));
                finish();
                    });
        }

        if (navAddTask != null) {
            navAddTask.setOnClickListener(v ->
                    startActivity(new Intent(AttributesActivity.this, CreateTaskActivity.class)));
        }

        if (navAttributes != null) {
            navAttributes.setOnClickListener(v ->
                    Toast.makeText(this, "Você já está nos Atributos!", Toast.LENGTH_SHORT).show());
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v ->
                    startActivity(new Intent(AttributesActivity.this, SettingsActivity.class)));
        }
    }

    private void destacarAbaAtiva(android.view.ViewGroup abaAtiva) {
        android.view.ViewGroup navHome = findViewById(R.id.nav_home);
        android.view.ViewGroup navCharacter = findViewById(R.id.nav_character);
        android.view.ViewGroup navAttributes = findViewById(R.id.nav_attributes);
        android.view.ViewGroup navSettings = findViewById(R.id.nav_settings);

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