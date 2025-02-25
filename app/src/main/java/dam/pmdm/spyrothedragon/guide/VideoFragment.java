package dam.pmdm.spyrothedragon.guide;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dam.pmdm.spyrothedragon.R;

public class VideoFragment extends Fragment {

    private VideoView videoView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el diseño del fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        // Obtener la referencia del VideoView
        videoView = view.findViewById(R.id.video_view);

        // Configurar el video
        String videoPath = "android.resource://" + requireActivity().getPackageName() + "/" + R.raw.video_of_spyrothedragon;
        videoView.setVideoURI(Uri.parse(videoPath));

        // Reproducir el video
        videoView.start();

        // Escuchar cuando el video termine
        videoView.setOnCompletionListener(mp -> {
            // Ocultar el contenedor del Fragment
            if (container != null) {
                container.setVisibility(View.GONE);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Liberar recursos del VideoView
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}