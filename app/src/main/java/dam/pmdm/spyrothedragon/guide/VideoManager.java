package dam.pmdm.spyrothedragon.guide;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.VideoView;

public class VideoManager {

    private final VideoView videoView;

    public VideoManager(VideoView videoView) {
        this.videoView = videoView;
    }

    public void playVideo(Context context, int videoResId) {
        // Configurar el video
        Uri videoUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + videoResId);
        videoView.setVideoURI(videoUri);

        // Mostrar el VideoView
        videoView.setVisibility(View.VISIBLE);

        // Reproducir el video
        videoView.start();

        // Ocultar el VideoView cuando el video termine
        videoView.setOnCompletionListener(mp -> videoView.setVisibility(View.GONE));
    }
}