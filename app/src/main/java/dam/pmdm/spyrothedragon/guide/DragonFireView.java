package dam.pmdm.spyrothedragon.guide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import dam.pmdm.spyrothedragon.R;

public class DragonFireView extends View {

    private Bitmap spriteSheet;
    private int frameWidth;
    private int frameHeight;
    private int frameCount;
    private int currentFrame;
    private Paint paint;
    private Handler handler;
    private Runnable runnable;
    private int rows = 4; // Número de filas en el sprite sheet
    private int cols = 4; // Número de columnas en el sprite sheet
    private boolean isAnimating = false;

    public DragonFireView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        spriteSheet = BitmapFactory.decodeResource(getResources(), R.drawable.flame_of_dragon);
        frameWidth = spriteSheet.getWidth() / cols;
        frameHeight = spriteSheet.getHeight() / rows;
        frameCount = cols * rows;
        currentFrame = 0;
        paint = new Paint();
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isAnimating) {
                    invalidate();
                }
            }
        };
    }

    public void launchFlames() {
        isAnimating = true;
        handler.post(runnable);
    }

    public void stopAnimation() {
        isAnimating = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isAnimating) return;
        int srcX = (currentFrame % cols) * frameWidth;
        int srcY = (currentFrame / cols) * frameHeight;
        Bitmap frame = Bitmap.createBitmap(spriteSheet, srcX, srcY, frameWidth, frameHeight);
        canvas.drawBitmap(frame, 0, 0, paint);
        currentFrame = (currentFrame + 1) % frameCount;
        handler.postDelayed(runnable, 100); // Actualizar cada 100 ms
    }
}