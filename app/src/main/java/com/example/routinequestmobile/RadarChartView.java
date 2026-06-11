package com.example.routinequestmobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class RadarChartView extends View {
    private int intVal = 0, forVal = 0, agiVal = 0, resVal = 0;
    private int maxVal = 10;

    private Paint paintPoly;
    private Paint paintBorder;

    public RadarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Inicializa o preenchimento (A cor em degradê será colocada no onSizeChanged)
        paintPoly = new Paint();
        paintPoly.setStyle(Paint.Style.FILL);
        paintPoly.setAntiAlias(true);

        // A borda continua dourada sólida para delimitar bem o formato do gráfico
        paintBorder = new Paint();
        paintBorder.setColor(Color.parseColor("#E6C57A"));
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(6f);
        paintBorder.setAntiAlias(true);
    }

    // Este método é acionado automaticamente pelo Android assim que a tela define o tamanho (140dp) do gráfico
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Cria o efeito de Degradê de Cima (0) para Baixo (h)
        // Mantemos o "80" na frente dos hexadecimais para ter 50% de transparência
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, h,
                Color.parseColor("#80E6C57A"), // Início: Dourado Translúcido
                Color.parseColor("#808A64C7"), // Fim: Roxo Translúcido
                Shader.TileMode.CLAMP
        );

        paintPoly.setShader(gradient);
    }

    public void setStats(int intelligence, int strength, int agility, int resistance) {
        this.intVal = intelligence;
        this.forVal = strength;
        this.agiVal = agility;
        this.resVal = resistance;

        int maiorAtributo = Math.max(intVal, Math.max(forVal, Math.max(agiVal, resVal)));
        this.maxVal = Math.max(10, maiorAtributo);

        // Pede ao Android para redesenhar a tela imediatamente com os novos valores
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int maxRadius = Math.min(centerX, centerY) - 10;

        // TRUQUE: Garante que o gráfico nunca suma. Se o atributo for 0, ele mostra 15% do eixo.
        float scaleInt = Math.max(0.15f, (float) intVal / maxVal);
        float scaleFor = Math.max(0.15f, (float) forVal / maxVal);
        float scaleAgi = Math.max(0.15f, (float) agiVal / maxVal);
        float scaleRes = Math.max(0.15f, (float) resVal / maxVal);

        float intY = centerY - (scaleInt * maxRadius);
        float forX = centerX + (scaleFor * maxRadius);
        float agiY = centerY + (scaleAgi * maxRadius);
        float resX = centerX - (scaleRes * maxRadius);

        Path path = new Path();
        path.moveTo(centerX, intY);
        path.lineTo(forX, centerY);
        path.lineTo(centerX, agiY);
        path.lineTo(resX, centerY);
        path.close();

        // Desenha o gráfico na tela usando o pincel de Degradê
        canvas.drawPath(path, paintPoly);
        canvas.drawPath(path, paintBorder);
    }
}