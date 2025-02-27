package dam.pmdm.spyrothedragon.guide;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * La clase {@code SoundManager} es una utilidad para gestionar la reproducción de sonidos
 * en la aplicación. Proporciona métodos para reproducir, obtener la duración de un sonido
 * y liberar los recursos asociados al reproductor de medios.
 *
 * <p>Esta clase utiliza {@link MediaPlayer} para reproducir sonidos y gestiona
 * la liberación de recursos automáticamente cuando un sonido termina de reproducirse.</p>
 *
 * @author Fco José Rodríguez Ruiz
 * @version 1.0.0
 */
public class SoundManager {
    private static MediaPlayer mediaPlayer;

    /**
     * Reproduce un sonido especificado por su ID de recurso.
     * Si ya hay un sonido reproduciéndose, lo detiene y libera los recursos antes de
     * reproducir el nuevo sonido.
     *
     * @param context    El contexto de la aplicación.
     * @param soundResId El ID del recurso de sonido que se desea reproducir.
     */
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

    /**
     * Obtiene la duración del sonido que se está reproduciendo actualmente.
     *
     * @return La duración del sonido en milisegundos, o {@code -1} si no hay ningún sonido         reproduciéndose o si no se puede obtener la duración.
     */
    public static int getSoundDuration() {
        int duration = -1;
        if (mediaPlayer != null) {
            duration = mediaPlayer.getDuration();
        }
        return duration;
    }

    /**
     * Libera los recursos asociados al reproductor de medios si está en uso.
     * Este método debe llamarse cuando ya no se necesite reproducir sonidos para
     * liberar la memoria utilizada por el {@link MediaPlayer}.
     */
    public static void freeMemoryPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
