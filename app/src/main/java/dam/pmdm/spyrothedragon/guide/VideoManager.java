package dam.pmdm.spyrothedragon.guide;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import dam.pmdm.spyrothedragon.R;

/**
 * La clase {@code VideoManager} es una utilidad para gestionar la reproducción de videos
 * en la aplicación. Proporciona métodos para reproducir, pausar, detener y restaurar
 * la reproducción de videos, así como para gestionar la visibilidad de la interfaz de usuario
 * durante la reproducción.
 *
 * <p>Esta clase utiliza {@link VideoView} para reproducir videos y gestiona la visibilidad
 * de la barra de herramientas y la barra de navegación durante la reproducción.</p>
 *
 * @author Fco José Rodríguez Ruiz
 * @version 1.0.0
 */
public class VideoManager {
    private final VideoView videoView;
    private final Context context;
    private int lastPosition = 0; // Guarda la posición del video
    private int currentVideoResId = 0; // Aquí se guarda el recurso de video (ID)
    private final Toolbar toolbar;

    /**
     * Constructor de la clase {@code VideoManager}.
     *
     * @param context   El contexto de la aplicación.
     * @param videoView El {@link VideoView} que se utilizará para reproducir los videos.
     * @param toolbar   La barra de herramientas que se ocultará durante la reproducción del video.
     */
    public VideoManager(Context context, VideoView videoView, Toolbar toolbar) {
        this.videoView = videoView;
        this.toolbar = toolbar;
        this.context = context;
    }

    /**
     * Reproduce un video especificado por su ID de recurso.
     *
     * @param videoResId El ID del recurso de video que se desea reproducir.
     * @param overlay    El {@link FrameLayout} que se utilizará como superposición durante la reproducción del video.
     */
    public void playVideo(int videoResId, FrameLayout overlay) {
        Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + videoResId);
        videoView.setVideoURI(videoUri);
        currentVideoResId = videoResId;

        toolbar.setVisibility(View.GONE);
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).findViewById(R.id.navView).setVisibility(View.GONE);
            ((AppCompatActivity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // Ocultar barra de estado
        }
        overlay.setVisibility(View.VISIBLE);
        overlay.bringToFront();
        videoView.setVisibility(View.VISIBLE);
        videoView.start();

        // Restaurar la posición si existía antes
        if (lastPosition > 0) {
            videoView.seekTo(lastPosition);
        }

        videoView.setOnPreparedListener(mp -> {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        });

        videoView.setOnCompletionListener(mp -> {
            stopVideo(overlay);
        });
    }

    /**
     * Detiene la reproducción del video y restaura la visibilidad de la interfaz de usuario.
     *
     * @param overlay El {@link FrameLayout} que se utilizó como superposición durante la reproducción del video.
     */
    private void stopVideo(FrameLayout overlay) {
        videoView.stopPlayback();
        videoView.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).findViewById(R.id.navView).setVisibility(View.VISIBLE); // Mostrar BottomNavigationView
            ((AppCompatActivity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // Ocultar barra de estado
        }
        lastPosition = 0; // Reiniciar posición y el ID
        currentVideoResId = 0;
    }

    /**
     * Guarda la posición actual del video para poder restaurarla más tarde.
     */
    public void savePosition() {
        lastPosition = videoView.getCurrentPosition(); // Guardar progreso antes de rotar
    }

    /**
     * Restaura la posición del video a la última posición guardada.
     */
    public void restorePosition() {
        if (lastPosition > 0) {
            videoView.seekTo(lastPosition);
            videoView.start();
        }
    }

    /**
     * Obtiene la posición actual del video que se está reproduciendo.
     *
     * @return La posición actual del video en milisegundos, o {@code 0} si no hay ningún video reproduciéndose.
     */
    public int getCurrentPosition() {
        return videoView != null ? videoView.getCurrentPosition() : 0;
    }

    /**
     * Verifica si un video se está reproduciendo actualmente.
     *
     * @return {@code true} si el video se está reproduciendo, {@code false} en caso contrario.
     */
    public boolean isPlaying() {
        return videoView != null && videoView.isPlaying();
    }

    /**
     * Obtiene el ID del recurso de video que se está reproduciendo actualmente.
     *
     * @return El ID del recurso de video, o {@code 0} si no hay ningún video reproduciéndose.
     */
    public int getCurrentVideoResId() {
        return currentVideoResId;
    }

    /**
     * Restaura el estado del video, incluyendo la posición y el estado de reproducción.
     *
     * @param context  El contexto de la aplicación.
     * @param position La posición del video que se desea restaurar.
     * @param isPlaying Indica si el video debe comenzar a reproducirse automáticamente.
     * @param overlay  El {@link FrameLayout} que se utilizará como superposición durante la reproducción del video.
     */
    public void restoreVideoState(Context context, int position, boolean isPlaying, FrameLayout overlay) {
        if (videoView != null && currentVideoResId != 0) {
            Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + currentVideoResId);
            videoView.setVideoURI(videoUri); // Reasignar el recurso del video
            videoView.seekTo(position);
            if (isPlaying) {
                overlay.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);
                videoView.start();
            }
        }
    }
}
