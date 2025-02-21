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
    private static final String SETTING_VIEW_GUIDE = "isViewedGuide";
    private static final int TIME_ANIMATIONS = 2500;
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
            R.id.guide_screen_5  // screen == 4
    };

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

            // Preparamos Easter Egg con animacion de llamas
            if (i == 1) {
                setupFireButton(guideScreen);
            }

            // Preparamos Easter Egg con video
            if (i == 4) {
                setupVideoButton(guideScreen);
            }

            // Para todas las guide_screen entre 1 y 5, indice 0 a 4, registro el continue_button y el exit_guide
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
                    if (i > 0 && i < 5) {               // ... si me encuentro entre las pantallas 2 y 5 (no es ni la primera ni la ultima)
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
            } else {
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
        }
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
            animateScreenElements(screen, activity);

            // Aplicando la narracion de Elora según la pantalla en la que me encuentro con un retraso
            // para esperar un poco la aparición de la hamburguesa...
            new Handler().postDelayed(() -> {
                ImageView eloraImageView = getEloraImageViewForScreen(guideScreens[screen], screen);
                if (eloraImageView != null) {
                    setupEloraAnimation(screen + 1, eloraImageView); // Llamamos al método con el número de pantalla
                }            }, TIME_ANIMATIONS);
        } else {
            finishGuide();
        }
    }

    private void animateScreenElements(int screen, Activity activity) {
        // Animar hamburguesa para pantallas 1 a 4
        if (screen >= 1 && screen <= 4) {
            View hamburguesa = guideScreens[screen].findViewById(R.id.hamburguesa);
            startFadeInAnimation(hamburguesa, activity);
        }

        // Animar linearLayoutFadeIn para pantallas 1 y 5
        if (screen == 0 || screen == 4) {
            int screenId = SCREEN_IDS[screen];
            View linearLayoutFadeIn = guideScreens[screen].findViewById(screenId);
            startFadeInAnimation(linearLayoutFadeIn, activity);
            SoundManager.playSound(activity, R.raw.portal);
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
        SoundManager.freeMemoryPlayer();
        sharedPreferences.edit().putBoolean(SETTING_VIEW_GUIDE, false).apply();
    }

    public void finishGuide() {
        deactivateScreens();
        unlockDrawerLayout();
        SoundManager.freeMemoryPlayer();
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

    private ImageView getEloraImageViewForScreen(View screenView, int screen) {
        switch (screen) {
            case 1:
                return screenView.findViewById(R.id.elora_1);
            case 2:
                return screenView.findViewById(R.id.elora_2);
            case 3:
                return screenView.findViewById(R.id.elora_3);
            case 4:
                return screenView.findViewById(R.id.elora_4);
            case 5:
                return screenView.findViewById(R.id.elora_5);
            case 6:
                return screenView.findViewById(R.id.elora_6);
            default:
                return null;
        }
    }

    private int getAnimationResourceFromScreen(int screen) {
        switch (screen) {
            case 1:
            case 6:
                return R.drawable.animation_elora_3;
            case 2:
            case 4:
                return R.drawable.animation_elora_2;
            case 3:
            case 5:
                return R.drawable.animation_elora_1;
            default:
                return -1;  // Sin animacion
        }
    }

    private int getSoundResourceFromScreen(int screen) {
        switch (screen) {
            case 1:
                return R.raw.sonido_elora_1;
            case 2:
                return R.raw.sonido_elora_2;
            case 3:
                return R.raw.sonido_elora_3;
            case 4:
                return R.raw.sonido_elora_4;
            case 5:
                return R.raw.sonido_elora_5;
            case 6:
                return R.raw.sonido_elora_6;
            default:
                return -1;  // Sin sonido
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
