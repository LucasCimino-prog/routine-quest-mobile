package com.example.routinequestmobile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTaskActivity extends AppCompatActivity {

    private EditText etTaskName, etTaskDesc, etTaskXp, etTaskDuration;
    private RadioGroup rgAttributes;
    private TextView btnSaveTask;

    // Variável para saber se estamos editando (se for null, estamos criando)
    private Long taskEditId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        etTaskName = findViewById(R.id.etTaskName);
        etTaskDesc = findViewById(R.id.etTaskDesc);
        etTaskXp = findViewById(R.id.etTaskXp);
        etTaskDuration = findViewById(R.id.etTaskDuration);
        rgAttributes = findViewById(R.id.rgAttributes);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // Verifica se a tela foi chamada com dados (do toque na lista)
        if (getIntent().hasExtra("TASK_ID")) {
            taskEditId = getIntent().getLongExtra("TASK_ID", -1L);

            if (taskEditId != -1L) {
                btnSaveTask.setText("Atualizar Missão");

                etTaskName.setText(getIntent().getStringExtra("TASK_NAME"));
                etTaskDesc.setText(getIntent().getStringExtra("TASK_DESC"));

                // Garantindo que números virem String para o EditText
                int xp = getIntent().getIntExtra("TASK_XP", 0);
                int duration = getIntent().getIntExtra("TASK_DURATION", 0);
                etTaskXp.setText(String.valueOf(xp));
                etTaskDuration.setText(String.valueOf(duration));

                String attr = getIntent().getStringExtra("TASK_ATTR");
                if ("INTELLIGENCE".equals(attr)) {
                    rgAttributes.check(R.id.rbIntelligence);
                } else if ("AGILITY".equals(attr)) {
                    rgAttributes.check(R.id.rbAgility);
                }
            } else {
                taskEditId = null; // Caso o ID venha inválido, volta para modo criação
            }
        }

        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarOuAtualizarMissao();
            }
        });
    }

    private void salvarOuAtualizarMissao() {
        int selectedId = rgAttributes.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Escolha um atributo (Inteligência ou Agilidade)!", Toast.LENGTH_SHORT).show();
            return;
        }

        String attributeType = (selectedId == R.id.rbIntelligence) ? "INTELLIGENCE" : "AGILITY";
        String name = etTaskName.getText().toString();
        String desc = etTaskDesc.getText().toString();
        String xpString = etTaskXp.getText().toString();
        String durationString = etTaskDuration.getText().toString();

        if (name.isEmpty() || xpString.isEmpty() || durationString.isEmpty()) {
            Toast.makeText(this, "Preencha o nome, XP e duração!", Toast.LENGTH_SHORT).show();
            return;
        }

        int xp = Integer.parseInt(xpString);
        int duration = Integer.parseInt(durationString);

        // Monta o objeto com os dados novos
        Task taskDetails = new Task(name, desc, xp, attributeType, 2, duration);
        taskDetails.setStatus("PENDING");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Task> call;

        // Se o ID for nulo, Rota de Criar. Se tiver ID, Rota de Atualizar!
        if (taskEditId == null) {
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            Long userId = prefs.getLong("USER_ID", -1L);

            if (userId == -1L) {
                Toast.makeText(this, "Usuário não identificado. Faça login novamente.", Toast.LENGTH_SHORT).show();
                return;
            }

            call = apiService.createTask(userId, taskDetails);
        } else {
            call = apiService.updateTask(taskEditId, taskDetails);
        }

        call.enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateTaskActivity.this, "Sucesso!", Toast.LENGTH_SHORT).show();
                    finish(); // Volta para a Home
                } else if (response.code() == 409) {
                    Toast.makeText(CreateTaskActivity.this, "Você já tem uma missão com este nome!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(CreateTaskActivity.this, "Erro no servidor: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                Toast.makeText(CreateTaskActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}