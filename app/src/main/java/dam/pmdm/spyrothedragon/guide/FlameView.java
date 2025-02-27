package dam.pmdm.spyrothedragon.guide;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * La clase {@code FlameView} es una vista personalizada que simula el efecto de llamas
 * que salen de la boca de Spyro. Utiliza partículas para crear un efecto visual de fuego.
 * Las partículas se generan, actualizan y dibujan en un {@link Canvas} para crear la animación.
 *
 * <p>Esta clase es utilizada en la guía de usuario para mostrar un efecto visual interactivo
 * cuando el usuario realiza una acción específica, presión larga de un botón transparente que ocupa
 * la imagen de Spyro.</p>
 *
 * @author Fco José Rodríguez Ruiz
 * @version 1.0.0
 */
public class FlameView extends View {
    private Paint paint;
    private ArrayList<Particle> particles = new ArrayList<>();
    private Random random = new Random();
    private float mouthX = 250; // Posición X de la boca de Spyro
    private float mouthY = 400; // Posición Y (ajusta según tu pantalla)
    private boolean isFiring = false; // Controla si las llamas están activas

    /**
     * Constructor de la clase {@code FlameView}.
     *
     * @param context El contexto de la aplicación.
     * @param attrs   Los atributos de la vista.
     */
    public FlameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    /**
     * Clase interna que representa una partícula de fuego.
     * Cada partícula tiene una posición, tamaño, velocidad y vida útil.
     */
    private class Particle {
        /**
         * The X.
         */
        float x, /**
         * The Y.
         */
        y;
        /**
         * The Size.
         */
        float size;
        /**
         * The Speed x.
         */
        float speedX, /**
         * The Speed y.
         */
        speedY;
        /**
         * The Life.
         */
        float life;

        /**
         * Constructor de la clase {@code Particle}.
         *
         * @param x La posición inicial en el eje X.
         * @param y La posición inicial en el eje Y.
         */
        Particle(float x, float y) {
            this.x = x;
            this.y = y;
            this.size = random.nextFloat() * 20 + 15;   // Tamaño de las particulos
            this.speedX = random.nextFloat() * 5 - 2;   // Dispersion horizontal
            this.speedY = random.nextFloat() * -5 - 2;  // Dispersion hacia arriba (por eso negativo)
            this.life = random.nextFloat() * 40 + 30;   // Duracion de las particulas para que se vea
        }

        /**
         * Actualiza la posición y el estado de la partícula.
         * Reduce la vida útil y el tamaño de la partícula en cada actualización.
         */
        void update() {
            x += speedX;
            y += speedY;
            life--;
            size *= 0.95f;
        }

        /**
         * Dibuja la partícula en el {@link Canvas} proporcionado.
         *
         * @param canvas El lienzo donde se dibuja la partícula.
         */
        void draw(Canvas canvas) {
            int alpha = (int) (life / 70 * 255);
            paint.setColor(Color.argb(alpha, 255, random.nextInt(100) + 155, 0));
            canvas.drawCircle(x, y, size, paint);
        }
    }

    /**
     * Activa el efecto de llamas. Este método inicia la generación de partículas
     * y comienza a dibujar las llamas en la vista.
     */
    public void startFiring() {
        isFiring = true;
        invalidate(); // Inicia el dibujo
    }

    /**
     * Desactiva el efecto de llamas. Este método detiene la generación de partículas
     * y deja de dibujar las llamas en la vista.
     */
    public void stopFiring() {
        isFiring = false;
    }

    /**
     * Método que se llama para dibujar la vista. Aquí se generan, actualizan y dibujan
     * las partículas de fuego en el {@link Canvas}.
     *
     * @param canvas El lienzo donde se dibuja la vista.
     */
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

    /**
     * Ajusta la posición de la boca de Spyro desde donde se generan las partículas de fuego.
     * En el caso de que por código se requiera modificar la posición del origen de las particulas
     * se llevaría a cabo haciendo una llamada a este método. De ese modo, invalida los valores
     * por defecto de posición (x,y).
     *
     * @param x La nueva posición en el eje X.
     * @param y La nueva posición en el eje Y.
     */
    public void setMouthPosition(float x, float y) {
        this.mouthX = x;
        this.mouthY = y;
    }
}