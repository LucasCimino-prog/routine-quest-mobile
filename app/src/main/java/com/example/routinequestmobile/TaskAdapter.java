package com.example.routinequestmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {

    // Criamos um "ouvinte" para avisar a HomeActivity quando o botão for clicado
    private OnTaskDeleteListener deleteListener;

    public interface OnTaskDeleteListener {
        void onDeleteClick(Task task);
    }

    public TaskAdapter(Context context, List<Task> tasks, OnTaskDeleteListener listener) {
        super(context, 0, tasks);
        this.deleteListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Carrega o nosso layout item_task.xml
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_task, parent, false);
        }

        // Pega a tarefa atual da lista
        Task currentTask = getItem(position);

        TextView tvTaskTitle = convertView.findViewById(R.id.tvTaskTitle);
        TextView btnDeleteTask = convertView.findViewById(R.id.btnDeleteTask);

        // Preenche o texto com o nome e o XP
        if (currentTask != null) {
            tvTaskTitle.setText(currentTask.getName() + " (+ " + currentTask.getXpReward() + " XP)");

            // O que acontece ao clicar no "X"
            btnDeleteTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 1. Cria a caixa de diálogo, mas guarda ela numa variável em vez de mostrar na hora
                    android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(v.getContext())
                            .setTitle("Abandonar Missão?")
                            .setMessage("Tem certeza que deseja excluir esta tarefa? Você não receberá XP por ela.")

                            .setPositiveButton("Sim, Excluir", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialogInterface, int which) {
                                    deleteListener.onDeleteClick(currentTask);
                                }
                            })

                            .setNegativeButton("Cancelar", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialogInterface, int which) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create(); // Cria o alerta em vez de mostrar direto

                    // 2. Exibe o alerta na tela primeiro
                    dialog.show();

                    // Botão Positivo (Sim, Excluir) -> Vermelho
                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(android.graphics.Color.parseColor("#E53935"));

                    // Botão Negativo (Cancelar) -> Azul
                    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(android.graphics.Color.parseColor("#5A7FD4"));
                }
            });
        }

        return convertView;
    }
}