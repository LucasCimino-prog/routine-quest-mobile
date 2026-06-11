package com.example.routinequestmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ListView lvTasks;
    // 1. O ADAPTER AGORA É GLOBAL PARA ATUALIZAÇÕES EM TEMPO REAL
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        lvTasks = findViewById(R.id.lvTasks);
        EditText etSearch = findViewById(R.id.etSearch);
        TextView btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> {
            String termoBusca = etSearch.getText().toString().trim();
            if (termoBusca.isEmpty()) {
                carregarTarefas();
            } else {
                buscarTarefasNoServidor(termoBusca);
            }
        });

        // 2. PROTEÇÃO DE CLIQUE (Bloqueia edição de missões concluídas)
        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Task tarefaClicada = (Task) parent.getItemAtPosition(position);

            if ("COMPLETED".equals(tarefaClicada.getStatus())) {
                Toast.makeText(HomeActivity.this, "Aguarde o nascer do sol para forjar esta missão novamente.", Toast.LENGTH_SHORT).show();
                return; // Impede que abra a tela de edição!
            }

            Intent intent = new Intent(HomeActivity.this, CreateTaskActivity.class);
            intent.putExtra("TASK_ID", (long) tarefaClicada.getId());
            intent.putExtra("TASK_NAME", tarefaClicada.getName());
            intent.putExtra("TASK_DESC", tarefaClicada.getDescription());
            intent.putExtra("TASK_XP", tarefaClicada.getXpReward());
            intent.putExtra("TASK_DURATION", tarefaClicada.getDurationMinutes());
            intent.putExtra("TASK_ATTR", tarefaClicada.getAttributeType());
            startActivity(intent);
        });

        configurarNavbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTarefas();
    }

    private void configurarNavbar() {
        View navHome = findViewById(R.id.nav_home);
        View navCharacter = findViewById(R.id.nav_character);
        View navAttributes = findViewById(R.id.nav_attributes);
        View navSettings = findViewById(R.id.nav_settings);
        View navAddTask = findViewById(R.id.nav_add_task);

        if (navHome != null) {
            navHome.post(() -> destacarAbaAtiva((android.view.ViewGroup) navHome));

            navHome.setOnClickListener(v ->
                    Toast.makeText(HomeActivity.this, "Você já está nas Missões!", Toast.LENGTH_SHORT).show());
        }

        if (navCharacter != null) {
            navCharacter.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, CharacterActivity.class));
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (navAddTask != null) {
            navAddTask.setOnClickListener(v ->
                    startActivity(new Intent(HomeActivity.this, CreateTaskActivity.class)));
        }

        if (navAttributes != null) {
            navAttributes.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, AttributesActivity.class));
                finish();
                overridePendingTransition(0, 0);
            });
        }

        if (navSettings != null) {
            navSettings.setOnClickListener(v -> {
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                finish(); // Adicionado para não acumular telas na memória
                overridePendingTransition(0, 0);
            });
        }
    }

    private void carregarTarefas() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getUserTasks(userId).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Armazena no adapter global
                    taskAdapter = new TaskAdapter(HomeActivity.this, response.body(),
                            task -> deletarTarefa(task.getId()),
                            task -> concluirTarefaNaApi(task)
                    );
                    lvTasks.setAdapter(taskAdapter);
                } else {
                    Toast.makeText(HomeActivity.this, "Erro ao carregar missões.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarTarefasNoServidor(String termoBusca) {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.searchTasks(userId, termoBusca).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tarefasFiltradas = response.body();
                    if (tarefasFiltradas.isEmpty()) {
                        Toast.makeText(HomeActivity.this, "Nenhuma missão encontrada.", Toast.LENGTH_SHORT).show();
                    }

                    taskAdapter = new TaskAdapter(HomeActivity.this, tarefasFiltradas,
                            task -> deletarTarefa(task.getId()),
                            task -> concluirTarefaNaApi(task)
                    );
                    lvTasks.setAdapter(taskAdapter);
                } else {
                    Toast.makeText(HomeActivity.this, "Erro na busca.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Erro de conexão.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletarTarefa(Long taskId) {
        if (taskId == null) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Missão deletada!", Toast.LENGTH_SHORT).show();
                    carregarTarefas(); // Como excluiu, é bom recarregar tudo para reordenar a lista
                } else {
                    Toast.makeText(HomeActivity.this, "Erro ao deletar.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Erro de conexão.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 3. ATUALIZAÇÃO VISUAL INSTANTÂNEA APÓS CONCLUIR
    private void concluirTarefaNaApi(Task task) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.completeTask(task.getId()).enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(HomeActivity.this, "⚔️ Glória conquistada!\n+" + response.body().getXpReward() + " XP", Toast.LENGTH_LONG).show();

                    // Bloqueia a missão instantaneamente sem piscar a tela
                    task.setStatus("COMPLETED");
                    if (taskAdapter != null) {
                        taskAdapter.notifyDataSetChanged();
                    }

                } else if (response.code() == 400) {
                    Toast.makeText(HomeActivity.this, "Esta missão já foi forjada hoje! Retorne amanhã.", Toast.LENGTH_LONG).show();

                    // Força a UI a assumir estado bloqueado caso o jogador tenha tentado burlar
                    task.setStatus("COMPLETED");
                    if (taskAdapter != null) {
                        taskAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Erro de conexão.", Toast.LENGTH_SHORT).show();

                // Se falhar a internet, recarrega para resetar o octógono que ficou cheio visualmente
                if (taskAdapter != null) {
                    taskAdapter.notifyDataSetChanged();
                }
            }
        });
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

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN) {
            android.view.View v = getCurrentFocus();
            if (v instanceof android.widget.EditText) {
                android.graphics.Rect outRect = new android.graphics.Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    v.clearFocus();
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}