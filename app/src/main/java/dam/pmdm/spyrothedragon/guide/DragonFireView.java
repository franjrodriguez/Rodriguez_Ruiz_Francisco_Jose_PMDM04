package dam.pmdm.spyrothedragon.guide;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DragonFireView extends View {

    private List<FlameParticle> flameParticles;
    private Paint flamePaint;
    private ValueAnimator flameAnimator;
    private Random random;

    public DragonFireView(Context context) {
        super(context);
        init();
    }

    public DragonFireView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        flameParticles = new ArrayList<>();
        flamePaint = new Paint();
        flamePaint.setColor(Color.RED);
        flamePaint.setStyle(Paint.Style.FILL);
        random = new Random();

        // Configurar el animador para actualizar las partículas
        flameAnimator = ValueAnimator.ofFloat(0, 1);
        flameAnimator.setDuration(1000);
        flameAnimator.setRepeatCount(ValueAnimator.INFINITE);
        flameAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateFlameParticles();
                invalidate(); // Redibujar la vista
            }
        });
    }

    public void launchFire() {
        // Reiniciar las partículas
        flameParticles.clear();
        for (int i = 0; i < 100; i++) {
            flameParticles.add(new FlameParticle());
        }

        // Iniciar la animación
        flameAnimator.start();
    }

    public void stopFire() {
        if (flameAnimator != null && flameAnimator.isRunning()) {
            flameAnimator.cancel(); // Detener la animación
        }
    }

    private void updateFlameParticles() {
        for (FlameParticle particle : flameParticles) {
            particle.update();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Dibujar las partículas de llama
        for (FlameParticle particle : flameParticles) {
            canvas.drawCircle(particle.position.x, particle.position.y, particle.radius, flamePaint);
        }
    }

    private class FlameParticle {
        PointF position;
        float radius;
        float speed;
        float angle;

        FlameParticle() {
            radius = random.nextFloat() * 10 + 5; // Tamaño aleatorio
            speed = random.nextFloat() * 10 + 5; // Velocidad aleatoria
            angle = random.nextFloat() * 360; // Ángulo aleatorio

            // Posición inicial en la boca del dragón (ajusta según sea necesario)
            position = new PointF(getWidth() / 2, getHeight() / 2);
        }

        void update() {
            // Mover la partícula según el ángulo y la velocidad
            position.x += Math.cos(Math.toRadians(angle)) * speed;
            position.y += Math.sin(Math.toRadians(angle)) * speed;

            // Reducir el tamaño de la partícula para simular que se desvanece
            radius *= 0.95;

            // Si la partícula es muy pequeña, reiniciarla
            if (radius < 1) {
                radius = random.nextFloat() * 10 + 5;
                position.set(getWidth() / 2, getHeight() / 2);
            }
        }
    }
}