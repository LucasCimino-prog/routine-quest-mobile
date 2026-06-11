package com.example.routinequestmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CharacterActivity extends AppCompatActivity {

    private TextView tvCharacterName;
    private TextView tvLevel;
    private TextView tvXp;
    private ProgressBar pbXpBar;
    private LinearLayout containerFeitos;
    private TextView tvSequencia;
    private TextView tvSelos;
    private TextView tvGloria;
    private TextView tvCharacterClass;
    private android.widget.ImageView ivAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character);

        tvCharacterName = findViewById(R.id.tvCharacterName);
        tvLevel = findViewById(R.id.tvLevel);
        tvXp = findViewById(R.id.tvXp);
        pbXpBar = findViewById(R.id.pbXpBar);
        containerFeitos = findViewById(R.id.containerFeitos);
        tvSequencia = findViewById(R.id.tvSequencia);
        tvSelos = findViewById(R.id.tvSelos);
        tvGloria = findViewById(R.id.tvGloria);
        tvCharacterClass = findViewById(R.id.tvCharacterClass);
        ivAvatar = findViewById(R.id.ivAvatar);

        configurarNavbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega os dados do perfil e a lista de feitos sempre que a tela abrir
        carregarDadosDoPersonagem();
        carregarFeitosDeHoje();
    }

    private void carregarDadosDoPersonagem() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        String savedName = prefs.getString("USER_NAME", "Lucas");
        tvCharacterName.setText(savedName);

        if (userId == -1L) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserStats(userId).enqueue(new Callback<UserStats>() {
            @Override
            public void onResponse(Call<UserStats> call, Response<UserStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserStats stats = response.body();

                    // Puxa diretamente o que o Spring Boot calculou
                    int xpAtual = stats.getXp() != null ? stats.getXp() : 0;
                    int nivelAtual = stats.getLevel() != null ? stats.getLevel() : 1;
                    int xpNecessario = stats.getExperienceRequired() != null ? stats.getExperienceRequired() : 100;

                    // Usando o número normal, mas mantendo a estética do espaçamento
                    tvLevel.setText("⊹  N Í V E L   " + nivelAtual + "  ⊹");
                    tvXp.setText(xpAtual + " / " + xpNecessario + " XP");

                    // LIGA A SEQUÊNCIA NA TELA
                    int diasSeguidos = stats.getStreakDays() != null ? stats.getStreakDays() : 0;
                    tvSequencia.setText(diasSeguidos + " DIAS");

                    // LIGA OS SELOS (MISSÕES HOJE)
                    int missoes = stats.getMissionsToday() != null ? stats.getMissionsToday() : 0;
                    tvSelos.setText(missoes + "/6"); // Mostra a fração baseada na regra de fadiga!

                    // LIGA O PODER TOTAL (GLÓRIA)
                    int poder = stats.getTotalPower() != null ? stats.getTotalPower() : 0;
                    tvGloria.setText("+" + poder);

                    // ATUALIZA O NOME DA CLASSE
                    String classe = stats.getClassName() != null ? stats.getClassName() : "NOVIÇO DA ROTINA";
                    tvCharacterClass.setText(classe.toUpperCase());

                    // MUDA O ÍCONE E A COR BASEADO NA CLASSE
                    atualizarBrasao(classe);

                    // A barra cresce dinamicamente baseada no banco de dados
                    pbXpBar.setMax(xpNecessario);
                    pbXpBar.setProgress(xpAtual);
                }
            }

            @Override
            public void onFailure(Call<UserStats> call, Throwable t) {
                android.util.Log.e("RPG_DEBUG", "Falha ao buscar dados do herói: " + t.getMessage());
            }
        });
    }

    private void carregarFeitosDeHoje() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        if (userId == -1L) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserTasks(userId).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> todasMissoes = response.body();

                    // Limpa o item estático/antigo antes de desenhar
                    containerFeitos.removeAllViews();

                    // Varre a lista de trás para frente para mostrar as conclusões mais recentes no topo
                    for (int i = todasMissoes.size() - 1; i >= 0; i--) {
                        Task task = todasMissoes.get(i);

                        // Como o seu backend já limpa as tarefas antigas automaticamente,
                        // tudo o que estiver "COMPLETED" aqui é garantidamente de hoje!
                        if ("COMPLETED".equals(task.getStatus())) {
                            adicionarFeitoNaTela(task);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                android.util.Log.e("RPG_DEBUG", "Erro ao buscar feitos do usuário: " + t.getMessage());
            }
        });
    }

    private void adicionarFeitoNaTela(Task task) {
        // Infla o arquivo xml individual que criamos no Passo 1
        View viewFeito = LayoutInflater.from(this).inflate(R.layout.item_feito, containerFeitos, false);

        TextView tvFeitoTitulo = viewFeito.findViewById(R.id.tvFeitoTitulo);
        TextView tvFeitoXp = viewFeito.findViewById(R.id.tvFeitoXp);

        // Preenche com os dados dinâmicos do banco
        tvFeitoTitulo.setText(task.getName() != null ? task.getName() : "Missão Cumprida");
        tvFeitoXp.setText("+" + task.getXpReward() + " XP");

        // Adiciona a linha de forma empilhada no LinearLayout principal
        containerFeitos.addView(viewFeito);
    }

    private void configurarNavbar() {
        View navHome = findViewById(R.id.nav_home);
        View navCharacter = findViewById(R.id.nav_character);
        View navAttributes = findViewById(R.id.nav_attributes);
        View navSettings = findViewById(R.id.nav_settings);
        View navAddTask = findViewById(R.id.nav_add_task);

        if (navCharacter != null) {
            navCharacter.post(() -> destacarAbaAtiva((android.view.ViewGroup) navCharacter));
        }

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                startActivity(new Intent(CharacterActivity.this, HomeActivity.class));
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (navCharacter != null) {
            navCharacter.setOnClickListener(v ->
                    Toast.makeText(this, "Você já está no Personagem!", Toast.LENGTH_SHORT).show());
        }

        if (navAddTask != null) {
            navAddTask.setOnClickListener(v ->
                    startActivity(new Intent(CharacterActivity.this, CreateTaskActivity.class)));
        }

        if (navAttributes != null) {
            navAttributes.setOnClickListener(v -> {
                startActivity(new Intent(CharacterActivity.this, AttributesActivity.class));
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                startActivity(new Intent(CharacterActivity.this, SettingsActivity.class));
                finish();
                overridePendingTransition(0, 0);
            });
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

    private void atualizarBrasao(String className) {
        if (className == null) return;

        String nomeMinusculo = className.toLowerCase();

        // Limpa qualquer filtro de cor anterior
        ivAvatar.clearColorFilter();

        if (nomeMinusculo.contains("mago") || nomeMinusculo.contains("sábio") || nomeMinusculo.contains("arcano")) {
            // Foco em Inteligência
            // ivAvatar.setImageResource(R.drawable.seu_icone_de_livro_ou_magia);
            ivAvatar.setColorFilter(android.graphics.Color.parseColor("#8A64C7")); // Roxo Místico

        } else if (nomeMinusculo.contains("guerreiro") || nomeMinusculo.contains("bárbaro") || nomeMinusculo.contains("força")) {
            // Foco em Força
            // ivAvatar.setImageResource(R.drawable.seu_icone_de_espada);
            ivAvatar.setColorFilter(android.graphics.Color.parseColor("#D32F2F")); // Vermelho Combate

        } else if (nomeMinusculo.contains("assassino") || nomeMinusculo.contains("caçador") || nomeMinusculo.contains("ágil")) {
            // Foco em Agilidade
            // ivAvatar.setImageResource(R.drawable.seu_icone_de_adaga_ou_raio);
            ivAvatar.setColorFilter(android.graphics.Color.parseColor("#388E3C")); // Verde Veneno/Vento

        } else if (nomeMinusculo.contains("guardião") || nomeMinusculo.contains("paladino") || nomeMinusculo.contains("tanque")) {
            // Foco em Resistência
            // ivAvatar.setImageResource(R.drawable.seu_icone_de_escudo);
            ivAvatar.setColorFilter(android.graphics.Color.parseColor("#1976D2")); // Azul Aço

        } else {
            // Classe Inicial ou Equilibrada
            ivAvatar.setColorFilter(android.graphics.Color.parseColor("#E6C57A")); // Dourado Padrão
        }
    }

}