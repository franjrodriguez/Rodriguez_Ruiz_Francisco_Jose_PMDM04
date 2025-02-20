package dam.pmdm.spyrothedragon;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import dam.pmdm.spyrothedragon.databinding.ActivityMainBinding;
import dam.pmdm.spyrothedragon.guide.UserGuideManager;

public class MainActivity extends AppCompatActivity {
    private static final String SETTING_VIEW_GUIDE = "isViewedGuide";

    private UserGuideManager userGuideManager;
    boolean isVisualizedUserGuide = false;
    private DrawerLayout drawerLayout;
    private ActivityMainBinding binding;
    NavController navController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
 */
        // Obtener la referencia al drawerLayout de la mainactivity
        drawerLayout = binding.drawerLayout;
        ActionBar actionBar = getSupportActionBar();

        // Inicializar la UserGuideManager
        View[] guideScreens = {
                findViewById(R.id.guide_screen_1),
                findViewById(R.id.guide_screen_2),
                findViewById(R.id.guide_screen_3),
                findViewById(R.id.guide_screen_4),
                findViewById(R.id.guide_screen_5),
                findViewById(R.id.guide_screen_6)
        };
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        userGuideManager = new UserGuideManager(this, sharedPreferences, guideScreens, navController, drawerLayout, actionBar);

        // Iniciar la guia (si es necesario... es la propia clase la que se encarga de gestionarlo.
        userGuideManager.startGuide();
    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Gestiona el clic en el ítem de información
        if (item.getItemId() == R.id.action_info) {
            showInfoDialog();  // Muestra el diálogo
            return true;
        } else if (item.getItemId() == R.id.action_set_guide) {
            userGuideManager.setGuideVisualized(false);      // Desactiva el estado de visualización de la guia para poder verla nuevamente.
            Toast.makeText(this, "Se ha restaurado la opción para poder visualizar la guia", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfoDialog() {
        // Crear un diálogo de información
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_about)
                .setMessage(R.string.text_about)
                .setPositiveButton(R.string.accept, null)
                .show();
    }
}