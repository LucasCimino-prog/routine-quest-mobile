package com.example.routinequestmobile;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FocusActivity extends AppCompatActivity {

    private CountDownTimer cronometro;
    private boolean missaoConcluida = false;
    private TextView tvTempoRestante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus); // Imagine uma tela preta com apenas o tempo no meio

        tvTempoRestante = findViewById(R.id.tvTempoRestante);

        // Exemplo: 25 minutos em milissegundos
        long tempoPomodoro = 25 * 60 * 1000;

        cronometro = new CountDownTimer(tempoPomodoro, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Atualiza o texto na tela a cada segundo
                int minutos = (int) (millisUntilFinished / 1000) / 60;
                int segundos = (int) (millisUntilFinished / 1000) % 60;
                tvTempoRestante.setText(String.format("%02d:%02d", minutos, segundos));
            }

            @Override
            public void onFinish() {
                // O TEMPO ACABOU COM A TELA ABERTA! SUCESSO!
                missaoConcluida = true;
                Toast.makeText(FocusActivity.this, "🧠 Foco Impecável! Missão Concluída!", Toast.LENGTH_LONG).show();

                // Aqui você chama a API: "/api/tasks/{id}/complete"

                finish(); // Volta para a tela inicial
            }
        }.start();
    }

    // Para o caso do utilizador minimizar o aplicativo antes do tempo acabar.
    @Override
    protected void onStop() {
        super.onStop();

        // Se a tela sumir da visão do utilizador e a missão ainda NÃO estiver concluída
        if (!missaoConcluida) {
            cronometro.cancel(); // Para o cronómetro

            // PUNIÇÃO: Aqui você chama a API apontando para "/api/tasks/{id}/fail"
            Toast.makeText(this, "Foco Quebrado! Missão Falhou.", Toast.LENGTH_LONG).show();

            finish(); // Fecha a tela de foco de vez
        }
    }
}