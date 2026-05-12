package com.example.routinequestmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

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
        Button btnDeleteTask = convertView.findViewById(R.id.btnDeleteTask);

        // Preenche o texto com o nome e o XP
        if (currentTask != null) {
            tvTaskTitle.setText(currentTask.getName() + " (+ " + currentTask.getXpReward() + " XP)");

            // O que acontece ao clicar no botão vermelho "X"
            btnDeleteTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteListener.onDeleteClick(currentTask);
                }
            });
        }

        return convertView;
    }
}