package dam.pmdm.spyrothedragon.guide;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;

import java.util.HashMap;
import java.util.Map;

import dam.pmdm.spyrothedragon.R;

public class UserGuideManager {
    private static final String SETTING_VIEW_GUIDE = "isViewedGuide";
    private static final int ANIMATION_REPEAT_COUNT = -1; // Número de repeticiones de la animación. -1 = infinito
    private final ActionBar actionBar;

    private MediaPlayer mediaPlayer;
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private View[] guideScreens;
    private int currentScreen = 0;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private AnimationDrawable elora1Animation;
    private AnimationDrawable elora2Animation;

    // Llevo a cabo un mapeo en el que relaciono cada pantalla de la guia con su animación y sonido
    private final Map<Integer, Integer> animationMap = new HashMap<Integer, Integer>() {{
        put(1, R.drawable.animation_elora_3);
        put(2, R.drawable.animation_elora_1);
        put(3, R.drawable.animation_elora_2);
        put(4, R.drawable.animation_elora_3);
        put(5, R.drawable.animation_elora_3);
        put(6, R.drawable.animation_elora_3);
    }};
    // Ahora hago lo mismo con los archivos de reproduccion de sonido de narración de Elora
    private final Map<Integer, Integer> soundMap = new HashMap<Integer, Integer>() {{
        put(1, R.raw.sonido_elora_1);
        put(2, R.raw.sonido_elora_2);
        put(3, R.raw.sonido_elora_3);
        put(4, R.raw.sonido_elora_4);
        put(5, R.raw.sonido_elora_5);
        put(6, R.raw.sonido_elora_6);
    }};

    public UserGuideManager(Activity activity, SharedPreferences sharedPreferences, View[] guideScreens, NavController navController, DrawerLayout drawerLayout, ActionBar actionBar) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
        this.guideScreens = guideScreens;
        this.navController = navController;
        this.drawerLayout = drawerLayout;
        this.actionBar = actionBar;
        //
        //
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        // Asignar OnClickListener a los botones de continuar y saltar
        for (int i = 0; i < guideScreens.length; i++) {
            View guideScreen = guideScreens[i];

            if (i != 5) {
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

                // Asignacion de onClickListener a continuar y gestion de la animación
                Button continueButton = guideScreen.findViewById(buttonIdContinue);
                if (continueButton != null) {
                    if (i > 0 && i < 5) {
                        setupContinueButtonAnimation(continueButton);
                    }
                    continueButton.setOnClickListener(v -> {
                        nextScreen();
                    });
                }

                Button exitButton = guideScreen.findViewById(buttonIdExit);
                if (exitButton != null) {
                    exitButton.setOnClickListener(v -> cancelGuide());
                }
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

            // se hace la llamada a la animacion según la pantalla en la que nos encontramos
            for (int j = 0; j < guideScreens.length; j++) {
                View guideScreenAnimation = guideScreens[j];

                // Identificamos el ImageView correcto según la pantalla
                int eloraImageViewId;
                if (j == 0 || j == 5) {
                    eloraImageViewId = R.id.elora_3;
                } else if (j == 1 || j == 3) {
                    eloraImageViewId = R.id.elora_1;
                } else if (j == 2 || j == 4) {
                    eloraImageViewId = R.id.elora_2;
                } else {
                    continue; // Si no hay una animación asociada, pasamos a la siguiente iteración
                }

                ImageView eloraImageView = guideScreen.findViewById(eloraImageViewId);
                if (eloraImageView != null) {
                    setupEloraAnimation(j + 1, eloraImageView); // Llamamos al método con el número de pantalla correcto
                }
            }
        }
    }

    private void setupEloraAnimation(int screenNumber, ImageView imageView) {
        // Detener cualquier animación o sonido anterior
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        imageView.clearAnimation();
        imageView.setBackgroundResource(0);

        // Configurar animación si existe
        if (animationMap.containsKey(screenNumber)) {
            int animationResource = animationMap.get(screenNumber);
            imageView.setBackgroundResource(animationResource);

            imageView.postDelayed(() -> {
                AnimationDrawable frameAnimation = (AnimationDrawable) imageView.getBackground();
                if (frameAnimation != null) {
                    frameAnimation.setOneShot(false);
                    frameAnimation.start();

                    // Iniciar sonido si existe
                    if (soundMap.containsKey(screenNumber)) {
                        mediaPlayer = MediaPlayer.create(activity, soundMap.get(screenNumber));
                        if (mediaPlayer != null) {
                            mediaPlayer.setOnPreparedListener(mp -> mp.start());
                        }

                        // Detener sonido y animación después del tiempo adecuado
                        int duration = calcularDuracionTotal(frameAnimation) * 2; // 2 repeticiones
                        new Handler().postDelayed(() -> {
                            frameAnimation.stop();
                            if (mediaPlayer != null) {
                                mediaPlayer.stop();
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                        }, duration);
                    }
                }
            }, 100); // Pequeño retardo para asegurar que AnimationDrawable se inicia correctamente
        }
    }

    // Método para calcular la duración total de la animación
    private int calcularDuracionTotal(AnimationDrawable animation) {
        int duration = 0;
        for (int i = 0; i < animation.getNumberOfFrames(); i++) {
            duration += animation.getDuration(i);
        }
        return duration;
    }
    private void setupContinueButtonAnimation(Button button) {
        // Primero limpiamos cualquier animación previa
        button.clearAnimation();

        // Creamos la animación
        Animation pulseAnimation = AnimationUtils.loadAnimation(activity, R.anim.pulse);

        // Iniciamos la animación
        button.startAnimation(pulseAnimation);

        // Detenemos la animación cuando se hace clic
        // ... y lanzamos la siguiente pantalla
        button.setOnClickListener(v -> {
            button.clearAnimation();
            nextScreen();
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

            // Aplicando animaciones según la pantalla en la que me encuentro
            // guide_screen_1_to_5 -> fade_in en @+id/hamburguesa
            if (screen >=1 && screen <= 4) {
                View hamburguesa = guideScreens[screen].findViewById(R.id.hamburguesa);
                if (hamburguesa != null) {
                    Animation fadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                    hamburguesa.startAnimation(fadeInAnimation);
                }
            }
            // guide_screen_6 -> fade_in en @+id/linearLayoutFadeIn
            if (screen == 5) {
                View linearLayoutFadeIn = guideScreens[screen].findViewById(R.id.guide_screen_6);
                if (linearLayoutFadeIn != null) {
                    Animation fadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                    linearLayoutFadeIn.startAnimation(fadeInAnimation);
                }
            }
        } else {
            finishGuide();
        }
    }

    public void nextScreen() {
        guideScreens[currentScreen].setVisibility(View.GONE);
        currentScreen++;

        // Gestiono el cambio de fragment desde aquí según la pantalla de la guia en la que me encuentre
        // Si estoy en la pantalla 2 cambia al fragmentWorld
        // Si estoy en la pantalla 3 cambia al fragmentCollectibles
        // Mientras tanto, sigo donde esté
        if (currentScreen == 2) {
            navController.navigate(R.id.action_navigation_characters_to_navigation_worlds);
        } else if (currentScreen == 3) {
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
        // Restaura el menu
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

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
