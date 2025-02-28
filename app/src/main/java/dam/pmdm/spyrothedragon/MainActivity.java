package dam.pmdm.spyrothedragon;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.media3.ui.PlayerView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import dam.pmdm.spyrothedragon.databinding.ActivityMainBinding;
import dam.pmdm.spyrothedragon.guide.UserGuideManager;
import dam.pmdm.spyrothedragon.guide.VideoManager;

/**
 * Actividad principal de la aplicación Spyro The Dragon.
 * Gestiona la navegación entre fragmentos, la barra de acción, el menú inferior y la guía de usuario.
 *
 * @author Fco José Rodríguez Ruiz (Programa la parte de la interfaz de la guia de usuario)
 * @version 1.0.0
 */
public class MainActivity extends AppCompatActivity {
    /**
     * Instancia de UserGuideManager para gestionar la guía de usuario.
     */
    public UserGuideManager guideManager;

    /** Clave para almacenar el estado de visualización de la guía en las SharedPreferences. */
    private static final String SETTING_VIEW_GUIDE = "isViewedGuide";

    /**
     * Indica si la guía de usuario ha sido visualizada. (Por si acaso, aunqeu se gestiona desde ShPref se inicializa a false)
     */
    boolean isVisualizedUserGuide = false;

    /**
     * Controlador de navegación para gestionar los destinos de los fragmentos.
     */
    NavController navController = null;

    private ConstraintLayout constraintLayout;
    private ActivityMainBinding binding;
    private VideoManager videoManager;

    /**
     * Método llamado cuando se crea la actividad. Configura la interfaz, la navegación y la guía de usuario.
     *
     * @param savedInstanceState Permite almacenar el estado de guardado de la actividad, si existe.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configuro la ToolBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            navController = NavHostFragment.findNavController(navHostFragment);
            NavigationUI.setupWithNavController(binding.navView, navController);
            NavigationUI.setupActionBarWithNavController(this, navController);
        }

        binding.navView.setOnItemSelectedListener(this::selectedBottomMenu);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_characters ||
                    destination.getId() == R.id.navigation_worlds ||
                    destination.getId() == R.id.navigation_collectibles) {
                // Para las pantallas de los tabs, no queremos que aparezca la flecha de atrás
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            else {
                // Si se navega a una pantalla donde se desea mostrar la flecha de atrás, habilítala
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        });
/*
*   Aquí comienza el código de MainActivity relacionado con la visualización
*   de la Guía de Usuario. Tan solo cargo lo que necesito (las pantallas de la guia
*   y le cedo el control a UserGuideManager que gestiona cualquier aspecto de la misma.
*   El video se gestiona desde VideoManager
*   El audio se gestion desde SoundManager
*   El canvas para la llama se gestiona en FlameView
 */

        // Busco el VideoView
        PlayerView videoView = findViewById(R.id.video_view);
        if (videoView == null) {
            Log.e("MainActivity", "PLayerView no encontrado en activity_main.xml");
        } else {
            videoManager = new VideoManager(this, videoView, toolbar);
        }

        // Obtener la referencia al Layout principal de la mainactivity
        constraintLayout = binding.mainLayout;
        ActionBar actionBar = getSupportActionBar();

        // Inicializar la UserGuideManager. Desde aquí se gestiona la totalidad de la Guia de Usuario.
        View[] guideScreens = new View[] {
                findViewById(R.id.guide_screen_1),
                findViewById(R.id.guide_screen_2),
                findViewById(R.id.guide_screen_3),
                findViewById(R.id.guide_screen_4),
                findViewById(R.id.guide_screen_5),
                findViewById(R.id.guide_screen_6)
        };
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        guideManager = new UserGuideManager(this, sharedPreferences, guideScreens,
                navController, constraintLayout, toolbar);
        guideManager.startGuide();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (videoManager != null) {
            videoManager.savePosition();
            outState.putInt("video_position", videoManager.getLastPosition());
            outState.putBoolean("video_playing", videoManager.isPlaying());
            outState.putInt("video_res_id", videoManager.getCurrentVideoResId()); // Guardar el recurso
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (videoManager != null) {
            int position = savedInstanceState.getInt("video_position");
            boolean isPlaying = savedInstanceState.getBoolean("video_playing");
            int videoResId = savedInstanceState.getInt("video_res_id");

            // Pasar el FrameLayout y el contexto para restaurar completamente
            FrameLayout overlay = findViewById(R.id.overlay_video);
            if (videoResId != 0) {
                videoManager.restoreVideoState(this, position, isPlaying, overlay);
            }
        }
    }

    /**
     * Infla el menú de opciones en la barra de acción.
     *
     * @param menuItem El menú a inflar.
     * @return true si el menú se infló correctamente.
     */
    private boolean selectedBottomMenu(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_characters)
            navController.navigate(R.id.navigation_characters);
        else
        if (menuItem.getItemId() == R.id.nav_worlds)
            navController.navigate(R.id.navigation_worlds);
        else
            navController.navigate(R.id.navigation_collectibles);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla el menú
        getMenuInflater().inflate(R.menu.about_menu, menu);
        return true;
    }

    /**
     * Maneja la selección de elementos en el menú de opciones.
     *
     * @param item El elemento del menú seleccionado.
     * @return true si la selección fue manejada, false para delegar al comportamiento predeterminado.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gestiona el clic en el ítem de información
        if (item.getItemId() == R.id.action_info) {
            showInfoDialog();  // Muestra el diálogo
            return true;
        } else if (item.getItemId() == R.id.action_set_guide) {
            guideManager.setGuideVisualized(false);      // Desactiva el estado de visualización de la guia para poder verla nuevamente.
            Toast.makeText(this, "Se ha restaurado la opción para poder visualizar la guia", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Muestra un diálogo con información sobre la aplicación.
     */
    private void showInfoDialog() {
        // Crear un diálogo de información
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_about)
                .setMessage(R.string.text_about)
                .setPositiveButton(R.string.accept, null)
                .show();
    }
}