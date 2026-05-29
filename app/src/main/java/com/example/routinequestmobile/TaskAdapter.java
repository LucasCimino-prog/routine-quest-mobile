package com.example.routinequestmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {

    private OnTaskDeleteListener deleteListener;
    private OnTaskCompleteListener completeListener;

    public interface OnTaskDeleteListener {
        void onDeleteClick(Task task);
    }

    public interface OnTaskCompleteListener {
        void onComplete(Task task);
    }

    public TaskAdapter(Context context, List<Task> tasks, OnTaskDeleteListener deleteListener, OnTaskCompleteListener completeListener) {
        super(context, 0, tasks);
        this.deleteListener = deleteListener;
        this.completeListener = completeListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_task, parent, false);
        }

        Task currentTask = getItem(position);

        TextView tvTaskTitle = convertView.findViewById(R.id.tvTaskTitle);
        TextView botaoDeleteTask = convertView.findViewById(R.id.botaoDeleteTask);

        View botaoHoldToComplete = convertView.findViewById(R.id.botaoHoldToComplete);
        View viewFill = convertView.findViewById(R.id.viewFill);

        // As 3 Estrelas Simétricas
        ImageView ivStarSmokeLeft = convertView.findViewById(R.id.ivStarSmokeLeft);
        ImageView ivStarSmokeCenter = convertView.findViewById(R.id.ivStarSmokeCenter);
        ImageView ivStarSmokeRight = convertView.findViewById(R.id.ivStarSmokeRight);

        if (currentTask != null) {
            tvTaskTitle.setText(currentTask.getName() + " (+ " + currentTask.getXpReward() + " XP)");

            viewFill.setScaleY(0f);

            ImageView[] stars = {ivStarSmokeLeft, ivStarSmokeCenter, ivStarSmokeRight};
            for (ImageView star : stars) {
                star.setAlpha(0f);
                star.setTranslationY(0f);
                star.setTranslationX(0f);
                star.setRotation(0f);
            }

            botaoDeleteTask.setOnClickListener(v -> {
                android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(v.getContext())
                        .setTitle("Abandonar Missão?")
                        .setMessage("Tem certeza que deseja excluir esta tarefa? Você não receberá XP por ela.")
                        .setPositiveButton("Sim, Excluir", (dialogInterface, which) -> deleteListener.onDeleteClick(currentTask))
                        .setNegativeButton("Cancelar", (dialogInterface, which) -> dialogInterface.dismiss())
                        .create();

                dialog.show();
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.parseColor("#E53935"));
                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.parseColor("#5A7FD4"));
            });

            // ========================================================
            // BOTÃO DE CONCLUIR + EXPLOSÃO SIMÉTRICA
            // ========================================================
            botaoHoldToComplete.setOnTouchListener(new View.OnTouchListener() {
                private boolean isFilling = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isFilling = true;
                            viewFill.animate()
                                    .scaleY(1f)
                                    .setDuration(1000)
                                    .withEndAction(() -> {
                                        isFilling = false;

                                        // As estrelas aparecem prontas para voar
                                        for (ImageView star : stars) {
                                            star.setAlpha(1f);
                                            star.setTranslationY(0f);
                                            star.setTranslationX(0f);
                                        }

                                        // Estrela Esquerda: sobe e abre para a esquerda rodando
                                        ivStarSmokeLeft.animate()
                                                .translationY(-160f).translationX(-50f).rotation(-45f).alpha(0f)
                                                .setDuration(600).start();

                                        // Estrela Centro: sobe muito alto e reto
                                        ivStarSmokeCenter.animate()
                                                .translationY(-220f).alpha(0f)
                                                .setDuration(700).start();

                                        // Estrela Direita: sobe e abre para a direita rodando
                                        ivStarSmokeRight.animate()
                                                .translationY(-160f).translationX(50f).rotation(45f).alpha(0f)
                                                .setDuration(600)
                                                .withEndAction(() -> {
                                                    completeListener.onComplete(currentTask);
                                                }).start();

                                    }).start();
                            return true;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            if (isFilling) {
                                viewFill.animate().cancel();
                                viewFill.animate().scaleY(0f).setDuration(200).start();
                                isFilling = false;
                            }
                            return true;
                    }
                    return false;
                }
            });
        }
        return convertView;
    }
}