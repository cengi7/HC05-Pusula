package com.cengiz.hc05pusula;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class PusulaView extends View {

    private final Paint paintKadran = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCizgi = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintYazi = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintIbre = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintIbreArka = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintMerkez = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path kuzeyPath = new Path();
    private final Path guneyPath = new Path();

    private float aci = 0f;

    public PusulaView(Context context) {
        super(context);
        init();
    }

    public PusulaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PusulaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintKadran.setStyle(Paint.Style.STROKE);
        paintKadran.setStrokeWidth(6f);
        paintKadran.setColor(Color.parseColor("#37474F"));

        paintCizgi.setStyle(Paint.Style.STROKE);
        paintCizgi.setColor(Color.parseColor("#90A4AE"));

        paintYazi.setColor(Color.parseColor("#263238"));
        paintYazi.setTextAlign(Paint.Align.CENTER);
        paintYazi.setFakeBoldText(true);

        paintIbre.setColor(Color.parseColor("#D32F2F"));
        paintIbre.setStyle(Paint.Style.FILL);

        paintIbreArka.setColor(Color.parseColor("#B0BEC5"));
        paintIbreArka.setStyle(Paint.Style.FILL);

        paintMerkez.setColor(Color.parseColor("#263238"));
        paintMerkez.setStyle(Paint.Style.FILL);
    }

    public void setAci(float aci) {
        if (Float.isNaN(aci) || Float.isInfinite(aci)) return;
        this.aci = ((aci % 360f) + 360f) % 360f;
        postInvalidate(); // Hem UI thread hem arka plan thread'inde güvenle ekranı yeniler
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = (int) (300 * getResources().getDisplayMetrics().density);
        int w = resolveSize(defaultSize, widthMeasureSpec);
        int h = resolveSize(defaultSize, heightMeasureSpec);
        int size = Math.min(w, h);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float yaricap = Math.min(w, h) / 2f - 24f;
        if (yaricap <= 0) return;

        // Dış kadran çemberi
        canvas.drawCircle(cx, cy, yaricap, paintKadran);

        // Derece çizgileri
        for (int d = 0; d < 360; d += 15) {
            float rad = (float) Math.toRadians(d);
            float disR = yaricap;
            float icR = (d % 90 == 0) ? yaricap - 28f
                    : (d % 45 == 0) ? yaricap - 18f
                    : yaricap - 10f;
            paintCizgi.setStrokeWidth(d % 90 == 0 ? 5f : 2f);
            float x1 = cx + (float) Math.sin(rad) * icR;
            float y1 = cy - (float) Math.cos(rad) * icR;
            float x2 = cx + (float) Math.sin(rad) * disR;
            float y2 = cy - (float) Math.cos(rad) * disR;
            canvas.drawLine(x1, y1, x2, y2, paintCizgi);
        }

        // Yön harfleri (K, D, G, B)
        String[] harfler = {"K", "D", "G", "B"};
        int[] aciDegerleri = {0, 90, 180, 270};
        paintYazi.setTextSize(yaricap * 0.18f);
        float yaziR = yaricap - 52f;
        for (int i = 0; i < 4; i++) {
            float rad = (float) Math.toRadians(aciDegerleri[i]);
            float x = cx + (float) Math.sin(rad) * yaziR;
            float y = cy - (float) Math.cos(rad) * yaziR + paintYazi.getTextSize() / 3f;
            paintYazi.setColor(i == 0 ? Color.parseColor("#D32F2F")
                    : Color.parseColor("#263238"));
            canvas.drawText(harfler[i], x, y, paintYazi);
        }

        // İbre çizimi
        canvas.save();
        canvas.rotate(aci, cx, cy);

        float ibreBoy = yaricap * 0.62f;
        float ibreGenis = yaricap * 0.09f;

        kuzeyPath.reset();
        kuzeyPath.moveTo(cx, cy - ibreBoy);
        kuzeyPath.lineTo(cx - ibreGenis, cy);
        kuzeyPath.lineTo(cx + ibreGenis, cy);
        kuzeyPath.close();
        canvas.drawPath(kuzeyPath, paintIbre);

        guneyPath.reset();
        guneyPath.moveTo(cx, cy + ibreBoy);
        guneyPath.lineTo(cx - ibreGenis, cy);
        guneyPath.lineTo(cx + ibreGenis, cy);
        guneyPath.close();
        canvas.drawPath(guneyPath, paintIbreArka);

        canvas.restore();

        // Merkez noktası
        canvas.drawCircle(cx, cy, yaricap * 0.05f, paintMerkez);
    }
}