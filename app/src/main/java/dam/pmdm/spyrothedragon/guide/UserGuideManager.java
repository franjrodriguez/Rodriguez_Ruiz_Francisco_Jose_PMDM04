package dam.pmdm.spyrothedragon.guide;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;

import java.util.Arrays;

import dam.pmdm.spyrothedragon.R;

public class UserGuideManager {
    private static final String SETTING_VIEW_GUIDE = "isViewedGuide";

    private Activity activity;
    private SharedPreferences sharedPreferences;
    private View[] guideScreens;
    private int currentScreen = 0;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private AnimationDrawable elora1Animation;
    private AnimationDrawable elora2Animation;

    public UserGuideManager(Activity activity, SharedPreferences sharedPreferences, View[] guideScreens, NavController navController, DrawerLayout drawerLayout) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
        this.guideScreens = guideScreens;
        this.navController = navController;
        this.drawerLayout = drawerLayout;
        //
        // Asignar OnClickListener a los botones de continuar y saltar
        for (int i = 0; i < guideScreens.length; i++) {
            View guideScreen = guideScreens[i];

            int buttonIdContinue = activity.getResources().getIdentifier(
                    "continue_button_" + (i + 1), // Nombre del ID
                    "id", // Tipo de recurso
                    activity.getPackageName()
            );
            int buttonIdExit = activity.getResources().getIdentifier(
                    "exit_guide_" + (i + 1), // Nombre del ID
                    "id", // Tipo de recurso
                    activity.getPackageName()
            );
            Button exitButton = guideScreen.findViewById(buttonIdExit);
            if (exitButton != null) {
                exitButton.setOnClickListener(v -> cancelGuide());
            }

            // Añado los botones específicos de la guide_screen_6 para repetir la guia o finalizarla actualizando
            // las sharedpreferences a true (guia visualizada)
            if (i == 5) {
                int buttonIdClosed = activity.getResources().getIdentifier(
                        "button_close_guide_6",
                        "id",
                        activity.getPackageName());
                Button finishButton = guideScreen.findViewById(buttonIdClosed);

                int buttonIdRepeat = activity.getResources().getIdentifier(
                        "button_comenzar_6",
                        "id",
                        activity.getPackageName());
                Button repeatButton = guideScreen.findViewById(buttonIdRepeat);

                if (finishButton != null) {
                    finishButton.setOnClickListener(v -> finishGuide());
                }
                if (repeatButton != null) {
                    repeatButton.setOnClickListener(v -> repeatGuide());
                }
            }

            // Cofniguracion del efecto ripple para el boton continue de las pantallas 2, 3, 4 y 5
            if (i >= 1 && i <= 5) {
                Button continueButton = guideScreen.findViewById(buttonIdContinue);
                if (continueButton != null) {
                    TypedValue outValue = new TypedValue();
                    activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    continueButton.setBackgroundResource(outValue.resourceId);
                    continueButton.setClickable(true);
                }
            }

            // Configuracion de la animación de Elora en las pantallas 2 y 4 (elora_1)
            if (i == 1 || i == 3) {
                ImageView elora1ImageView = guideScreen.findViewById(R.id.elora_1);
                if (elora1ImageView != null) {
                    setupElora1Animation(elora1ImageView);
                }
            }
            // Configuracion de la animación de Elora en las pantallas 3 y 5 (elora_2)
            if (i == 2 || i == 4) {
                ImageView elora2ImageView = guideScreen.findViewById(R.id.elora_2);
                if (elora2ImageView != null) {
                    setupElora2Animation(elora2ImageView);
                }
            }
        }
    }

    private void setupElora1Animation(ImageView imageView) {
        imageView.setBackgroundResource(R.drawable.animation_elora_1); // Asegúrate de tener este recurso
        elora1Animation = (AnimationDrawable) imageView.getBackground();
        imageView.post(() -> {
            if (elora1Animation != null) {
                elora1Animation.start();
            }
        });
    }

    private void setupElora2Animation(ImageView imageView) {
        imageView.setBackgroundResource(R.drawable.animation_elora_2); // Asegúrate de tener este recurso
        elora2Animation = (AnimationDrawable) imageView.getBackground();
        imageView.post(() -> {
            if (elora2Animation != null) {
                elora2Animation.start();
            }
        });
    }

    public void setGuideVisualized(boolean isVisualized) {
        sharedPreferences.edit().putBoolean(SETTING_VIEW_GUIDE, isVisualized).apply();
    }

    public void startGuide() {
        if (!sharedPreferences.getBoolean(SETTING_VIEW_GUIDE, false)) {
            lockDrawerLayout();
            showScreen(currentScreen);
        }
    }

    private void showScreen(int screen) {
        if (screen < guideScreens.length) {
            guideScreens[screen].setVisibility(View.VISIBLE);
        } else {
            finishGuide();
        }
    }

    public void nextScreen() {
        guideScreens[currentScreen].setVisibility(View.GONE);
        currentScreen++;

        // Gestiono el cambio de fragment desde aquí según la pantalla de la guia en la que me encuentre
        // Si estoy en la pantalla 1 cambia al fragmentWorld
        // Si estoy en la pantalla 2 cambia al fragmentCollectibles
        // Mientras tanto, sigo donde esté
        if (currentScreen == 2) {
            navController.navigate(R.id.action_navigation_characters_to_navigation_worlds);
        } else if (currentScreen == 4) {
            navController.navigate(R.id.action_navigation_worlds_to_navigation_collectibles);
        }
        // Muestra la pantalla de la guia
        showScreen(currentScreen);
    }

    public void cancelGuide() {
        deactivateScreens();
        unlockDrawerLayout();
        sharedPreferences.edit().putBoolean(SETTING_VIEW_GUIDE, false).apply();
    }

    public void finishGuide() {
        deactivateScreens();
        unlockDrawerLayout();
        sharedPreferences.edit().putBoolean(SETTING_VIEW_GUIDE, true).apply();
    }

    public void deactivateScreens() {
        for (View screen: guideScreens) {
            screen.setVisibility(View.GONE);
        }
    }

    private void lockDrawerLayout() {
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void unlockDrawerLayout() {
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void repeatGuide() {
        currentScreen = 0;
        showScreen(currentScreen);
    }
}
