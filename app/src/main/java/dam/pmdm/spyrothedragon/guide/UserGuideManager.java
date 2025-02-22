package dam.pmdm.spyrothedragon.guide;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;

import dam.pmdm.spyrothedragon.R;

public class UserGuideManager {
    private static final String TAG = "FRANTAG -->";
    private static final String SETTING_VIEW_GUIDE = "isViewedGuide";
    private static final int TIME_ANIMATIONS = 2500;
    private static final long FADE_DURATION = 1000;
    private static final long ELORA_DELAY = 1000;
    private final ActionBar actionBar;

    private MediaPlayer mediaPlayer;
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private View[] guideScreens;
    private int currentScreen = 0;
    private NavController navController;
    private DrawerLayout drawerLayout;

    private int buttonClickCount = 0;

    // Mapeo de pantallas a IDs de recursos
    private static final int[] SCREEN_IDS = {
            R.id.guide_screen_1, // screen == 0
            R.id.guide_screen_2, // screen == 1
            R.id.guide_screen_3, // screen == 2
            R.id.guide_screen_4, // screen == 3
            R.id.guide_screen_5,  // screen == 4
            R.id.guide_screen_6     // screen == 5
    };

    public UserGuideManager(Activity activity, SharedPreferences sharedPreferences,
                            View[] guideScreens, NavController navController,
                            DrawerLayout drawerLayout, ActionBar actionBar) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
        this.guideScreens = guideScreens;
        this.navController = navController;
        this.drawerLayout = drawerLayout;
        this.actionBar = actionBar;
        //
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
        // Inicializar los botones de toda la guia
        setupGuideButtons();
        currentScreen = 0;
    }

    public void startGuide() {
        if (!sharedPreferences.getBoolean(SETTING_VIEW_GUIDE, false)) {
            lockDrawerLayout();
            showScreen(currentScreen);
        }
    }

    private void setupGuideButtons() {
        for (int i = 0; i < guideScreens.length; i++) {
            View screen = guideScreens[i];
            if (i < 5) { // Pantallas 1 a 5
                int continueButtonId = activity.getResources().getIdentifier(
                        "continue_button_" + (i + 1), "id", activity.getPackageName());
                int exitButtonId = activity.getResources().getIdentifier(
                        "exit_guide_" + (i + 1), "id", activity.getPackageName());

                Button continueButton = screen.findViewById(continueButtonId);
                if (continueButton != null) {
                    continueButton.setOnClickListener(v -> nextScreen());
                }

                Button exitButton = screen.findViewById(exitButtonId);
                if (exitButton != null) {
                    exitButton.setOnClickListener(v -> cancelGuide());
                }
            } else { // Pantalla 6
                Button closeButton = screen.findViewById(R.id.button_close_guide_6);
                if (closeButton != null) {
                    closeButton.setOnClickListener(v -> finishGuide());
                }

                Button repeatButton = screen.findViewById(R.id.button_comenzar_6);
                if (repeatButton != null) {
                    repeatButton.setOnClickListener(v -> repeatGuide());
                }
            }
        }
    }

    private void showScreen(int screen) {
        if (screen < guideScreens.length) {
            View screenView = guideScreens[screen];

            // Cargamos las pantallas con fadeIn
            screenView.setAlpha(0f);
            screenView.setVisibility(View.VISIBLE);
            Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
            fadeIn.setDuration(FADE_DURATION);
            screenView.startAnimation(fadeIn);

            // Aplicando animaciones según la pantalla en la que me encuentro
            animateScreenElements(screen);

            // Configurar Elora
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                int eloraId = activity.getResources().getIdentifier(
                        "elora_" + (screen + 1), "id", activity.getPackageName());
                ImageView eloraView = screenView.findViewById(eloraId);
                if (eloraView != null) {
                    setupEloraAnimation(screen, eloraView);
                }
            }, ELORA_DELAY);
        } else {
            finishGuide();
        }
    }

    private void animateScreenElements(int screen) {
        // Animar hamburguesa para pantallas 2 a 5
        if (screen >= 1 && screen <= 4) {
            int hamburguesaId = activity.getResources().getIdentifier(
                    "hambunguesa_" + (screen + 1), "id", activity.getPackageName());
            View hamburguesa = guideScreens[screen].findViewById(hamburguesaId);
            if (hamburguesa != null) {
                hamburguesa.setAlpha(0f);
                hamburguesa.setVisibility(View.VISIBLE);
                Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                fadeIn.setDuration(FADE_DURATION);
            }

            // Animar el continue_button de las pantallas 2 a 5 con pulse
            int continueButtonId = activity.getResources().getIdentifier(
                    "continue_button_" + (screen + 1), "id", activity.getPackageName());
            Button continueButton = guideScreens[screen].findViewById(continueButtonId);
            if (continueButton != null) {
                Animation pulse = AnimationUtils.loadAnimation(activity, R.anim.pulse);
                continueButton.startAnimation(pulse);
            }
        }
    }

    private void startFadeInAnimation(View view, Activity activity) {
        if (view != null) {
            Animation fadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
            view.startAnimation(fadeInAnimation);
        }
    }

    public void nextScreen() {
        guideScreens[currentScreen].setVisibility(View.GONE);
        currentScreen++;

        // Navegacion de fragment entre las pantallas 2 y 3 unicamente
        if (currentScreen == 2) {
            navController.navigate(R.id.action_navigation_characters_to_navigation_worlds);
            new Handler(Looper.getMainLooper()).postDelayed(() -> showScreen(currentScreen), 300);
        } else if (currentScreen == 3) {
            navController.navigate(R.id.action_navigation_worlds_to_navigation_collectibles);
            new Handler(Looper.getMainLooper()).postDelayed(() -> showScreen(currentScreen), 300);
        } else {
            // Muestra la pantalla de la guia
            showScreen(currentScreen);
        }
    }

    public void cancelGuide() {
        finishGuide();
        unlockDrawerLayout();
        SoundManager.freeMemoryPlayer();
        sharedPreferences.edit().putBoolean(SETTING_VIEW_GUIDE, false).apply();
    }

    public void finishGuide() {
        hideAllScreens();
        unlockDrawerLayout();
        SoundManager.freeMemoryPlayer();
        sharedPreferences.edit().putBoolean(SETTING_VIEW_GUIDE, true).apply();
    }

    private void setupEloraAnimation(int screen, ImageView eloraImageView) {
        // Obtener la animación y el sonido correspondientes
        int animationRes = getAnimationResourceFromScreen(screen);
        int soundRes = getSoundResourceFromScreen(screen);

        // Configurar la animación
        if (animationRes != -1) {
            AnimationDrawable animation = (AnimationDrawable) activity.getResources().getDrawable(animationRes);
            eloraImageView.setImageDrawable(animation);
            animation.start();
        }

        // Reproducir el sonido
        if (soundRes != -1) {
            SoundManager.playSound(activity, soundRes);
        }
    }

    private void setupFireButton(View guideScreen) {
        Button buttonFireDragon = guideScreen.findViewById(R.id.button_fire_dragon);
        if (buttonFireDragon != null) {
            buttonFireDragon.setOnLongClickListener(v -> {
                DragonFireView dragonFireView = guideScreen.findViewById(R.id.dragonFireView);
                if (dragonFireView != null) {
                    dragonFireView.setVisibility(View.VISIBLE);
                    dragonFireView.launchFire();
                    SoundManager.playSound(v.getContext(), R.raw.roar);

                    // Detener la animacion despues de 2 segundos
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        dragonFireView.stopFire();
                        dragonFireView.setVisibility(View.INVISIBLE);
                    }, TIME_ANIMATIONS);
                }
                return true;    // Esto es para indicar que el evento fue manejado
            });
        }
    }

    private void setupContinueButtonAnimation(Button button) {
        // Primero limpiamos cualquier animación previa
        button.clearAnimation();

        // Creamos la animación tipo Pulsar sobre el recuadro (Button) que indica el boton de la guia
        Animation pulseAnimation = AnimationUtils.loadAnimation(activity, R.anim.pulse);

        // Iniciamos la animación
        button.startAnimation(pulseAnimation);

        // Detenemos la animación cuando se hace clic
        // ... y lanzamos la siguiente pantalla
        button.setOnClickListener(v -> {
            SoundManager.playSound(v.getContext(), R.raw.menu);
            button.clearAnimation();
            nextScreen();
        });
    }

    public void setGuideVisualized(boolean isVisualized) {
        sharedPreferences.edit().putBoolean(SETTING_VIEW_GUIDE, isVisualized).apply();
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

    private void repeatGuide() {
        hideAllScreens();
        currentScreen = 0;
        showScreen(currentScreen);
    }

    private void hideAllScreens() {
        for (View screen : guideScreens) {
            screen.clearAnimation();
            screen.setVisibility(View.GONE);
        }
    }

    private ImageView getEloraImageViewForScreen(View screenView, int screen) {
        switch (screen) {
            case 0: return screenView.findViewById(R.id.elora_1);
            case 1: return screenView.findViewById(R.id.elora_2);
            case 2: return screenView.findViewById(R.id.elora_3);
            case 3: return screenView.findViewById(R.id.elora_4);
            case 4: return screenView.findViewById(R.id.elora_5);
            case 5: return screenView.findViewById(R.id.elora_6);
            default: return null;
        }
    }

    private int getAnimationResourceFromScreen(int screen) {
        switch (screen) {
            case 0:
            case 5:
                return R.drawable.animation_elora_3;
            case 1:
            case 3:
                return R.drawable.animation_elora_2;
            case 2:
            case 4:
                return R.drawable.animation_elora_1;
            default:
                return -1;  // Sin animacion
        }
    }

    private int getSoundResourceFromScreen(int screen) {
        switch (screen) {
            case 0: return R.raw.sonido_elora_1;
            case 1: return R.raw.sonido_elora_2;
            case 2: return R.raw.sonido_elora_3;
            case 3: return R.raw.sonido_elora_4;
            case 4: return R.raw.sonido_elora_5;
            case 5: return R.raw.sonido_elora_6;
            default: return -1;  // Sin sonido
        }
    }

    private void setupVideoButton(View guideScreen) {
        Button buttonFireDragon = guideScreen.findViewById(R.id.button_fire_dragon);
        VideoView videoView = guideScreen.findViewById(R.id.video_view);
        VideoManager videoManager = new VideoManager(videoView);

        if (buttonFireDragon != null) {
            buttonFireDragon.setOnClickListener(v -> {
                buttonClickCount++;

                // Si se ha pulsado 4 veces, reproducir el video
                if (buttonClickCount == 4) {
                    videoManager.playVideo(v.getContext(), R.raw.spyrothedragon); // Reemplaza con tu video
                    buttonClickCount = 0; // Reiniciar el contador
                }
            });
        }
    }
}
