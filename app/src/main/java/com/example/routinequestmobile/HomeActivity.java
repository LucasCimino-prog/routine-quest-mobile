package com.example.routinequestmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ListView lvTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Encontra a lista e a barra de pesquisa no XML
        lvTasks = findViewById(R.id.lvTasks);
        EditText etSearch = findViewById(R.id.etSearch);
        TextView btnSearch = findViewById(R.id.btnSearch);

        // Ação do botão de buscar
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String termoBusca = etSearch.getText().toString().trim();

                if (termoBusca.isEmpty()) {
                    carregarTarefas(); // Se estiver vazio, carrega tudo normal
                } else {
                    buscarTarefasNoServidor(termoBusca);
                }
            }
        });

        // Ação ao tocar em cima de uma tarefa na lista (Para Editar)
        lvTasks.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                // Descobre qual tarefa foi clicada
                Task tarefaClicada = (Task) parent.getItemAtPosition(position);

                // Chama a tela de criação, mas manda os dados da tarefa junto na "mochila" (Intent)
                Intent intent = new Intent(HomeActivity.this, CreateTaskActivity.class);
                intent.putExtra("TASK_ID", (long) tarefaClicada.getId());
                intent.putExtra("TASK_NAME", tarefaClicada.getName());
                intent.putExtra("TASK_DESC", tarefaClicada.getDescription());
                intent.putExtra("TASK_XP", tarefaClicada.getXpReward());
                intent.putExtra("TASK_DURATION", tarefaClicada.getDurationMinutes());
                intent.putExtra("TASK_ATTR", tarefaClicada.getAttributeType());

                startActivity(intent);
            }
        });

        // AÇÕES DA BARRA DE NAVEGAÇÃO CUSTOMIZADA
        // Mapeia os novos ícones do rodapé
        android.view.ViewGroup navHome = findViewById(R.id.nav_home);
        android.view.ViewGroup navCharacter = findViewById(R.id.nav_character);
        android.view.ViewGroup navAttributes = findViewById(R.id.nav_attributes);
        android.view.ViewGroup navSettings = findViewById(R.id.nav_settings);
        android.widget.ImageView navAddTask = findViewById(R.id.nav_add_task);
        // Avisa que a Home é a tela atual, para ela ficar branca e maior
        navHome.post(() -> destacarAbaAtiva(navHome));

        // 1. Missões
        navHome.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Você já está nas Missões!", Toast.LENGTH_SHORT).show();
        });

        // 2. Personagem (Para futuras implementações do TCC)
        navCharacter.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Tela do Personagem em breve!", Toast.LENGTH_SHORT).show();
        });

        // 3. O Octógono Gigante (+ Nova Missão)
        navAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateTaskActivity.class);
            startActivity(intent);
        });

        // 4. Atributos e Evolução
        navAttributes.setOnClickListener(v -> {
            Toast.makeText(HomeActivity.this, "Evolução e Atributos em breve!", Toast.LENGTH_SHORT).show();
        });

        // 5. Configurações (Aplica o Logout)
        navSettings.setOnClickListener(v -> {
            // Apaga os dados salvos da sessão do usuário
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Volta para a tela principal (Login)
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);

            // Limpa o histórico de telas (impede que o botão de voltar retorne pra cá)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish(); // Encerra a Home
        });
    }

    // O onResume roda toda vez que a tela da Home volta a ficar visível!
    @Override
    protected void onResume() {
        super.onResume();
        carregarTarefas();
    }

    private void carregarTarefas() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        Long userId = prefs.getLong("USER_ID", -1L);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Task>> call = apiService.getUserTasks(userId);

        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> listaDeTarefas = response.body();

                    // USA O NOSSO NOVO ADAPTADOR!
                    TaskAdapter adapter = new TaskAdapter(HomeActivity.this, listaDeTarefas, new TaskAdapter.OnTaskDeleteListener() {
                        @Override
                        public void onDeleteClick(Task task) {
                            // Quando clicar no X, chama o método de deletar passando o ID
                            deletarTarefa(task.getId());
                        }
                    });

                    lvTasks.setAdapter(adapter);

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
        Long userId = prefs.getLong("USER_ID", -1L); // Fixado por enquanto

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Task>> call = apiService.searchTasks(userId, termoBusca);

        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tarefasFiltradas = response.body();

                    if (tarefasFiltradas.isEmpty()) {
                        Toast.makeText(HomeActivity.this, "Nenhuma missão encontrada.", Toast.LENGTH_SHORT).show();
                    }

                    // Reutilizamos o adaptador que já foi criado antes!
                    TaskAdapter adapter = new TaskAdapter(HomeActivity.this, tarefasFiltradas, new TaskAdapter.OnTaskDeleteListener() {
                        @Override
                        public void onDeleteClick(Task task) {
                            deletarTarefa(task.getId());
                        }
                    });

                    lvTasks.setAdapter(adapter);

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

    // Método que apaga e recarrega a lista
    private void deletarTarefa(Long taskId) {
        if (taskId == null) {
            Toast.makeText(this, "Erro: Tarefa sem ID", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Void> call = apiService.deleteTask(taskId);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Missão deletada!", Toast.LENGTH_SHORT).show();
                    carregarTarefas(); // Recarrega a lista na tela para a tarefa sumir visualmente
                } else {
                    Toast.makeText(HomeActivity.this, "Erro ao deletar.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // MÉTODO PARA DESTACAR A ABA ATIVA NA NAVBAR
    private void destacarAbaAtiva(android.view.ViewGroup abaAtiva) {
        android.view.ViewGroup navHome = findViewById(R.id.nav_home);
        android.view.ViewGroup navCharacter = findViewById(R.id.nav_character);
        android.view.ViewGroup navAttributes = findViewById(R.id.nav_attributes);
        android.view.ViewGroup navSettings = findViewById(R.id.nav_settings);

        // Coloca todas as abas numa lista genérica
        android.view.ViewGroup[] abas = {navHome, navCharacter, navAttributes, navSettings};

        for (android.view.ViewGroup aba : abas) {
            // Pega no ícone e no texto
            android.widget.ImageView icone = (android.widget.ImageView) aba.getChildAt(0);
            android.widget.TextView texto = (android.widget.TextView) aba.getChildAt(1);

            // Limpa filtros anteriores
            icone.clearColorFilter();

            // O SEGREDO ESTÁ AQUI: Só aplica a tinta branca se a aba NÃO for a das Missões
            if (aba != navHome) {
                icone.setColorFilter(android.graphics.Color.parseColor("#FFFFFF"), android.graphics.PorterDuff.Mode.SRC_ATOP);
            }

            // O texto fica sempre branco
            texto.setTextColor(android.graphics.Color.parseColor("#FFFFFF"));

            if (aba == abaAtiva) {
                // ABA ATIVA: 100% visível, texto em negrito e ícone maior
                icone.setAlpha(1.0f);
                texto.setAlpha(1.0f);
                texto.setTypeface(null, android.graphics.Typeface.BOLD);

                icone.animate().scaleX(1.35f).scaleY(1.35f).setDuration(300).start();
            } else {
                // ABA INATIVA: 40% visível (dá o efeito apagado) e tamanho normal
                icone.setAlpha(0.4f);
                texto.setAlpha(0.4f);
                texto.setTypeface(null, android.graphics.Typeface.NORMAL);

                icone.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).start();
            }
        }
    }
}