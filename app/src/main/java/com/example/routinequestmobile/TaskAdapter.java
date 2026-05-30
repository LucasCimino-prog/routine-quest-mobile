package com.example.routinequestmobile;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
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

        // ORDENA AS MISSÕES POR HORÁRIO AUTOMATICAMENTE!
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                String time1 = t1.getStartTime() != null ? t1.getStartTime() : "00:00";
                String time2 = t2.getStartTime() != null ? t2.getStartTime() : "00:00";
                return time1.compareTo(time2);
            }
        });

        this.deleteListener = deleteListener;
        this.completeListener = completeListener;
    }

    // MÉTODO AUXILIAR PARA DESCOBRIR A QUAL PERÍODO A HORA PERTENCE
    private String obterPeriodo(String time) {
        if (time == null || time.isEmpty() || !time.contains(":")) return "AURORA";
        try {
            int hora = Integer.parseInt(time.split(":")[0]);
            if (hora < 12) return "AURORA";             // 00:00 às 11:59
            if (hora >= 12 && hora < 18) return "ZÊNITE"; // 12:00 às 17:59
            return "CREPÚSCULO";                        // 18:00 às 23:59
        } catch (Exception e) {
            return "AURORA";
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_task, parent, false);
        }

        Task currentTask = getItem(position);

        if (currentTask != null) {
            // Mapeia o Cabeçalho de Tempo
            View layoutPeriodHeader = convertView.findViewById(R.id.layoutPeriodHeader);
            TextView tvPeriodName = convertView.findViewById(R.id.tvPeriodName);

            // ========================================================
            // LÓGICA DO CABEÇALHO (AGRUPAMENTO)
            // ========================================================
            String currentPeriod = obterPeriodo(currentTask.getStartTime());
            boolean shouldShowHeader = false;

            if (position == 0) {
                // Se for a primeira missão da lista, mostra sempre
                shouldShowHeader = true;
            } else {
                // Se o período da missão for diferente da missão de cima, mostra o cabeçalho!
                Task previousTask = getItem(position - 1);
                String previousPeriod = obterPeriodo(previousTask.getStartTime());
                if (!currentPeriod.equals(previousPeriod)) {
                    shouldShowHeader = true;
                }
            }

            if (shouldShowHeader) {
                layoutPeriodHeader.setVisibility(View.VISIBLE);
                if (currentPeriod.equals("AURORA")) {
                    tvPeriodName.setText("☆  AURORA");
                } else if (currentPeriod.equals("ZÊNITE")) {
                    tvPeriodName.setText("✸  ZÊNITE"); // Ícone de Sol
                } else {
                    tvPeriodName.setText("☾  CREPÚSCULO"); // Ícone de Lua
                }
            } else {
                layoutPeriodHeader.setVisibility(View.GONE);
            }

            // MAPEAR OS CAMPOS DA MISSÃO
            TextView tvTaskTitle = convertView.findViewById(R.id.tvTaskTitle);
            TextView tvTaskXp = convertView.findViewById(R.id.tvTaskXp);
            TextView tvTaskTime = convertView.findViewById(R.id.tvTaskTime);
            TextView tvTaskAttr = convertView.findViewById(R.id.tvTaskAttr);
            View viewAttributeColor = convertView.findViewById(R.id.viewAttributeColor);
            TextView tvCheckMark = convertView.findViewById(R.id.tvCheckMark);

            TextView botaoDeleteTask = convertView.findViewById(R.id.botaoDeleteTask);
            View botaoHoldToComplete = convertView.findViewById(R.id.botaoHoldToComplete);
            ImageView viewFill = convertView.findViewById(R.id.viewFill);

            ImageView ivStarSmokeLeft = convertView.findViewById(R.id.ivStarSmokeLeft);
            ImageView ivStarSmokeCenter = convertView.findViewById(R.id.ivStarSmokeCenter);
            ImageView ivStarSmokeRight = convertView.findViewById(R.id.ivStarSmokeRight);

            // PREENCHE TEXTOS
            tvTaskTitle.setText(currentTask.getName());
            tvTaskXp.setText("☆ +" + currentTask.getXpReward() + " XP");

            String startTime = currentTask.getStartTime();
            if (startTime != null && !startTime.isEmpty()) {
                tvTaskTime.setText("🕒 " + startTime);
            } else {
                tvTaskTime.setText("🕒 00:00");
            }

            String attr = currentTask.getAttributeType();
            if (attr != null) {
                switch (attr) {
                    case "INTELLIGENCE": tvTaskAttr.setText("① +1 INT"); break;
                    case "AGILITY":      tvTaskAttr.setText("① +1 AGI"); break;
                    case "STRENGTH":     tvTaskAttr.setText("① +1 FOR"); break;
                    case "RESISTANCE":   tvTaskAttr.setText("① +1 RES"); break;
                    default:             tvTaskAttr.setText("① ???"); break;
                }
            } else {
                tvTaskAttr.setText("① ???");
            }

            // CORES E DEGRADÊ
            viewAttributeColor.setBackgroundResource(R.drawable.bg_bar_degrade);
            int corOuro = Color.parseColor("#E6C57A");
            tvTaskXp.getPaint().setShader(null);
            tvTaskAttr.getPaint().setShader(null);
            tvTaskXp.setTextColor(corOuro);
            tvTaskAttr.setTextColor(corOuro);

            // ========================================================
            // LÓGICA DE MISSÃO CONCLUÍDA VS PENDENTE
            // ========================================================
            boolean isCompleted = "COMPLETED".equals(currentTask.getStatus());

            // A caixa interna do card (precisamos pegar o RelativeLayout para aplicar o Alpha)
            View cardMissao = ((ViewGroup) convertView).getChildAt(1);

            if (isCompleted) {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                cardMissao.setAlpha(0.6f); // Aumentei um pouquinho o Alpha para 0.6f para ficar mais legível

                if (viewFill.getDrawable() instanceof android.graphics.drawable.ClipDrawable) {
                    viewFill.getDrawable().setLevel(10000);
                }

                tvCheckMark.setTextColor(Color.parseColor("#E6C57A"));

                botaoDeleteTask.setAlpha(0.3f);
                botaoDeleteTask.setOnClickListener(null);
                botaoHoldToComplete.setOnTouchListener(null);

                ImageView[] stars = {ivStarSmokeLeft, ivStarSmokeCenter, ivStarSmokeRight};
                for (ImageView star : stars) {
                    star.setAlpha(0f);
                }

            } else {
                tvTaskTitle.setPaintFlags(tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                cardMissao.setAlpha(1.0f);

                if (viewFill.getDrawable() instanceof android.graphics.drawable.ClipDrawable) {
                    viewFill.getDrawable().setLevel(0);
                }

                tvCheckMark.setTextColor(Color.parseColor("#9E8BB3"));

                botaoDeleteTask.setAlpha(1.0f);

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
                            .setMessage("Tem certeza que deseja excluir esta tarefa?")
                            .setPositiveButton("Sim, Excluir", (dialogInterface, which) -> deleteListener.onDeleteClick(currentTask))
                            .setNegativeButton("Cancelar", (dialogInterface, which) -> dialogInterface.dismiss())
                            .create();
                    dialog.show();
                    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#E53935"));
                    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#E6C57A"));
                });

                botaoHoldToComplete.setOnTouchListener(new View.OnTouchListener() {
                    private android.animation.ValueAnimator fillAnimator;
                    private boolean isFilling = false;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!(viewFill.getDrawable() instanceof android.graphics.drawable.ClipDrawable)) return false;

                        android.graphics.drawable.ClipDrawable clipDrawable = (android.graphics.drawable.ClipDrawable) viewFill.getDrawable();

                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                isFilling = true;
                                if (fillAnimator != null) fillAnimator.cancel();

                                fillAnimator = android.animation.ValueAnimator.ofInt(clipDrawable.getLevel(), 10000);
                                fillAnimator.setDuration(1000);
                                fillAnimator.addUpdateListener(anim -> clipDrawable.setLevel((int) anim.getAnimatedValue()));

                                fillAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(android.animation.Animator animation) {
                                        if (isFilling && clipDrawable.getLevel() >= 9900) {
                                            isFilling = false;

                                            for (ImageView star : stars) {
                                                star.setAlpha(1f);
                                                star.setTranslationY(0f);
                                                star.setTranslationX(0f);
                                            }

                                            ivStarSmokeLeft.animate().translationY(-160f).translationX(-50f).rotation(-45f).alpha(0f).setDuration(600).start();
                                            ivStarSmokeCenter.animate().translationY(-220f).alpha(0f).setDuration(700).start();
                                            ivStarSmokeRight.animate().translationY(-160f).translationX(50f).rotation(45f).alpha(0f).setDuration(600)
                                                    .withEndAction(() -> completeListener.onComplete(currentTask)).start();
                                        }
                                    }
                                });
                                fillAnimator.start();
                                return true;

                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                isFilling = false;
                                if (fillAnimator != null) fillAnimator.cancel();

                                android.animation.ValueAnimator emptyAnimator = android.animation.ValueAnimator.ofInt(clipDrawable.getLevel(), 0);
                                emptyAnimator.setDuration(200);
                                emptyAnimator.addUpdateListener(anim -> clipDrawable.setLevel((int) anim.getAnimatedValue()));
                                emptyAnimator.start();
                                return true;
                        }
                        return false;
                    }
                });
            }
        }
        return convertView;
    }
}