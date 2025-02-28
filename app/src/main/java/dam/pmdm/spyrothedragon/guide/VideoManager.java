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
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

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
    private ExoPlayer exoPlayer;
    private final PlayerView playerView;
    private final Context context;
    private final Toolbar toolbar;

    private int lastPosition = 0; // Guarda la posición del video
    private int currentVideoResId = 0; // Aquí se guarda el recurso de video (ID)

    /**
     * Constructor de la clase {@code VideoManager}.
     *
     * @param context   El contexto de la aplicación.
     * @param playerView El {@link PlayerView} que se utilizará para reproducir los videos.
     * @param toolbar   La barra de herramientas que se ocultará durante la reproducción del video.
     */
    public VideoManager(Context context, PlayerView playerView, Toolbar toolbar) {
        this.playerView = playerView;
        this.toolbar = toolbar;
        this.context = context;
        initializePlayer();
    }

    public int getLastPosition() {
        return lastPosition;
    }

    private void initializePlayer() {
        exoPlayer = new ExoPlayer.Builder(context).build();
        playerView.setPlayer(exoPlayer);
    }

    /**
     * Reproduce un video especificado por su ID de recurso.
     *
     * @param videoResId El ID del recurso de video que se desea reproducir.
     * @param overlay    El {@link FrameLayout} que se utilizará como superposición durante la reproducción del video.
     */
    public void playVideo(int videoResId, FrameLayout overlay) {
        Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + videoResId);
        MediaItem mediaItem = new MediaItem.Builder().setUri(videoUri).build();

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.play();
        currentVideoResId = videoResId;

        // Ocultar la UI
        toolbar.setVisibility(View.GONE);
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).findViewById(R.id.navView).setVisibility(View.GONE);
            ((AppCompatActivity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        overlay.setVisibility(View.VISIBLE);
        overlay.bringToFront();
        playerView.setVisibility(View.VISIBLE);

        // Restaurar la posición si existía antes
        if (lastPosition > 0) {
            exoPlayer.seekTo(lastPosition);
        }

        // Listener para detectar el fin del video
        exoPlayer.addListener(new androidx.media3.common.Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == androidx.media3.common.Player.STATE_ENDED) {
                    stopVideo(overlay);
                }
            }
        });
    }

    public int getCurrentVideoResId() {
        return currentVideoResId;
    }

    /**
     * Detiene la reproducción del video y restaura la visibilidad de la interfaz de usuario.
     *
     * @param overlay El {@link FrameLayout} que se utilizó como superposición durante la reproducción del video.
     */
    public void stopVideo(FrameLayout overlay) {
        if (exoPlayer != null) {
            exoPlayer.stop();
            exoPlayer.clearMediaItems();
        }
        playerView.setVisibility(View.GONE);
        overlay.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);

        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).findViewById(R.id.navView).setVisibility(View.VISIBLE);
            ((AppCompatActivity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        lastPosition = 0;
        currentVideoResId = 0;
    }

    /**
     * Guarda la posición del video antes de pausar o rotar la pantalla.
     */
    public void savePosition() {
        if (exoPlayer != null) {
            lastPosition = (int) exoPlayer.getCurrentPosition();
        }
    }

    /**
     * Restaura la posición del video a la última posición guardada.
     */
    public void restorePosition() {
        if (exoPlayer != null && lastPosition > 0) {
            exoPlayer.seekTo(lastPosition);
            exoPlayer.play();
        }
    }

    /**
     * Verifica si el video está en reproducción.
     *
     * @return `true` si el video se está reproduciendo.
     */
    public boolean isPlaying() {
        return exoPlayer != null && exoPlayer.isPlaying();
    }

    /**
     * Libera los recursos de ExoPlayer.
     */
    public void releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public void restoreVideoState(Context context, int position, boolean isPlaying, FrameLayout overlay) {
        if (currentVideoResId != 0) {
            playVideo(currentVideoResId, overlay);
            exoPlayer.seekTo(position);
            if (isPlaying) {
                exoPlayer.play();
            } else {
                exoPlayer.pause();
            }
        }
    }
}
