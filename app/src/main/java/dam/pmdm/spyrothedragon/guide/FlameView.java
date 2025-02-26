package dam.pmdm.spyrothedragon.guide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class FlameView extends View {
    private Paint paint;
    private ArrayList<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private float mouthX = 250; // Posición X de la boca de Spyro
    private float mouthY = 400; // Posición Y (ajusta según tu pantalla)
    private boolean isFiring = false; // Controla si las llamas están activas

    public FlameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    // Clase para las partículas de fuego
    private class Particle {
        float x, y;
        float size;
        float speedX, speedY;
        float life;

        Particle(float x, float y) {
            this.x = x;
            this.y = y;
            this.size = random.nextFloat() * 20 + 15;   // Tamaño de las particulos
            this.speedX = random.nextFloat() * 5 - 2;   // Dispersion horizontal
            this.speedY = random.nextFloat() * -5 - 2;  // Dispersion hacia arriba (por eso negativo)
            this.life = random.nextFloat() * 40 + 30;   // Duracion de las particulas para que se vea
        }

        void update() {
            x += speedX;
            y += speedY;
            life--;
            size *= 0.95f;
        }

        void draw(Canvas canvas) {
            int alpha = (int) (life / 70 * 255);
            paint.setColor(Color.argb(alpha, 255, random.nextInt(100) + 155, 0));
            canvas.drawCircle(x, y, size, paint);
        }
    }

    // Método para activar las llamas
    public void startFiring() {
        isFiring = true;
        invalidate(); // Inicia el dibujo
    }

    // Método para desactivar las llamas
    public void stopFiring() {
        isFiring = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Crear nuevas partículas solo si está activo
        if (isFiring) {
            for (int i = 0; i < 10; i++) {
                particles.add(new Particle(mouthX, mouthY));
            }
        }

        // Actualizar y dibujar partículas
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            p.draw(canvas);

            // Eliminar partículas muertas
            if (p.life <= 0) {
                particles.remove(i);
            }
        }

        // Seguir refrescando si hay partículas o está activo
        if (isFiring || !particles.isEmpty()) {
            invalidate();
        }
    }

    // Método para ajustar la posición de la boca si necesitas precisión
    public void setMouthPosition(float x, float y) {
        this.mouthX = x;
        this.mouthY = y;
    }
}