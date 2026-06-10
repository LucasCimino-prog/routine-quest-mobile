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

        // Inicializa a animação e os cliques da barra inferior
        configurarNavbar();

        // Carrega as missões concluídas dinamicamente
        carregarCronicasRecentes();

        // [FUTURO] Aqui vamos mapear os textos (tvIntTotal, etc.)
        // para atualizar com os dados do banco de dados para os cálculos totais
    }

    private void carregarCronicasRecentes() {
        // Resgata o ID do utilizador logado da mesma forma que na HomeActivity
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        if (userId == -1L) {
            Toast.makeText(this, "Erro: Utilizador não identificado.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Faz a chamada passando o ID correto do utilizador
        Call<List<Task>> call = apiService.getUserTasks(userId);

        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> todasMissoes = response.body();
                    List<Task> missoesConcluidas = new ArrayList<>();

                    // 1. Filtra apenas as missões com status COMPLETED
                    for (Task task : todasMissoes) {
                        if ("COMPLETED".equals(task.getStatus())) {
                            missoesConcluidas.add(task);
                        }
                    }

                    // 2. Inverte a lista para que as mais recentes fiquem no topo
                    Collections.reverse(missoesConcluidas);

                    // 3. Pega no máximo as 4 últimas
                    int limite = Math.min(missoesConcluidas.size(), 4);

                    containerCronicas.removeAllViews(); // Limpa o contentor por segurança

                    for (int i = 0; i < limite; i++) {
                        adicionarCronicaNaTela(missoesConcluidas.get(i));
                    }
                } else {
                    Toast.makeText(AttributesActivity.this, "Erro ao carregar crônicas.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Toast.makeText(AttributesActivity.this, "Erro de conexão ao buscar crônicas.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adicionarCronicaNaTela(Task task) {
        // Infla (desenha) o nosso item_cronica.xml
        View viewCronica = LayoutInflater.from(this).inflate(R.layout.item_cronica, containerCronicas, false);

        TextView tvIcone = viewCronica.findViewById(R.id.tvCronicaIcone);
        TextView tvTitulo = viewCronica.findViewById(R.id.tvCronicaTitulo);
        TextView tvRecompensa = viewCronica.findViewById(R.id.tvCronicaRecompensa);

        // Define o título da missão
        tvTitulo.setText(task.getName() != null ? task.getName() : "Missão Misteriosa");

        // FIX: Define a cor do ícone e da recompensa SEMPRE para amarelo (#FFB300)
        tvIcone.setTextColor(Color.parseColor("#FFB300"));
        tvRecompensa.setTextColor(Color.parseColor("#FFB300"));

        // O switch agora serve APENAS para definir qual texto/símbolo vai aparecer
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
                tvIcone.setText("\uD83D\uDEE1\uFE0E");
                tvRecompensa.setText("+1 RES");
                break;
            default:
                tvIcone.setText("◈");
                tvRecompensa.setText("+" + task.getXpReward() + " XP");
                break;
        }

        // Adiciona a view montada ao LinearLayout da tela
        containerCronicas.addView(viewCronica);
    }

    private void carregarTotaisAtributos() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Supondo que você tenha um endpoint que retorna o "Score" total do usuário
        apiService.getUserStats(userId).enqueue(new Callback<UserStats>() {
            @Override
            public void onResponse(Call<UserStats> call, Response<UserStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserStats stats = response.body();

                    // Mapeia os textos com os novos símbolos + valores
                    TextView tvInt = findViewById(R.id.tvIntTotal);
                    TextView tvFor = findViewById(R.id.tvForTotal);
                    TextView tvAgi = findViewById(R.id.tvAgiTotal);
                    TextView tvRes = findViewById(R.id.tvResTotal);

                    tvInt.setText("Ψ " + stats.getIntelligence());
                    tvFor.setText("⧟ " + stats.getStrength());
                    tvAgi.setText("ϟ " + stats.getAgility());
                    tvRes.setText("\uD83D\uDEE1\uFE0E " + stats.getResistance());
                }
            }

            @Override
            public void onFailure(Call<UserStats> call, Throwable t) {
                // Silencioso ou log de erro
            }
        });
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
            navCharacter.setOnClickListener(v ->
                    Toast.makeText(this, "Tela do Personagem em breve!", Toast.LENGTH_SHORT).show());
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