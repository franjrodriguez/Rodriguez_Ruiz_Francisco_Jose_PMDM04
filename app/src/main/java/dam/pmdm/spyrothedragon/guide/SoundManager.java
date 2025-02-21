package dam.pmdm.spyrothedragon.guide;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundManager {
    private static MediaPlayer mediaPlayer;

    public static void playSound(Context context, int soundResId) {
        try {
            // Si se está reproduciendo "algo", lo detiene
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            // Le damos caña al sonido cuya ID hemos recibido
            mediaPlayer = MediaPlayer.create(context, soundResId);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void freeMemoryPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
