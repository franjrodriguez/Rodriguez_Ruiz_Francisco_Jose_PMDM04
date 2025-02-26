package dam.pmdm.spyrothedragon.guide;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;

public class VideoManager {
    private final VideoView videoView;
    private int lastPosition = 0; // Guarda la posición del video
    private int currentVideoResId = 0; // Aquí se guarda el recurso de video (ID)

    public VideoManager(VideoView videoView) {
        this.videoView = videoView;
    }

    public void playVideo(Context context, int videoResId, FrameLayout overlay) {
        Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + videoResId);
        videoView.setVideoURI(videoUri);
        currentVideoResId = videoResId;

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
            videoView.stopPlayback();
            videoView.setVisibility(View.GONE);
            overlay.setVisibility(View.GONE);
            lastPosition = 0; // Reiniciar posición y el ID
            currentVideoResId = 0;
        });
    }

    public void savePosition() {
        lastPosition = videoView.getCurrentPosition(); // Guardar progreso antes de rotar
    }

    public void restorePosition() {
        if (lastPosition > 0) {
            videoView.seekTo(lastPosition);
            videoView.start();
        }
    }

    public int getCurrentPosition() {
        return videoView != null ? videoView.getCurrentPosition() : 0;
    }

    public boolean isPlaying() {
        return videoView != null && videoView.isPlaying();
    }

    public int getCurrentVideoResId() {
        return currentVideoResId;
    }

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
