package dam.pmdm.spyrothedragon.guide;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;

import dam.pmdm.spyrothedragon.R;

/**
 * Clase que gestiona la guía de usuario de la aplicación Spyro The Dragon.
 * Controla la visualización secuencial de pantallas de guía con animaciones y navegación.
 * Tambien se encarga de lanzar sonidos y las acciones requeridas para los dos Easter Egg:
 *      - En la pantalla coleccionables (guide_screen_4) -> Muestra un video como respuesta a 4 clicks
 *      - En la pantalla personajes (guide_screen_2) -> Muestra llamas como respuesta a una pulsación larga
 *
 * @author Fco José Rodríguez Ruiz
 * @version 1.0.0
 */
public class UserGuideManager {
    /** Permite localizar mis Logs de cara a facilitar la localización de errores */
    private static final String TAG = "FRANTAG -->";

    /** Clave para verificar si la guía ya fue visualizada. */
    private static final String SETTING_VIEW_GUIDE = "isViewedGuide";

    /** Tiempo de duración de las animaciones */
    private static final int TIME_ANIMATIONS = 2500;

    /** Tiempo de duración de los fade (apariciones) */
    private static final long FADE_DURATION = 1000;

    /* Se concibe este tiempo de espera antes de que comience la animación de Elora para que la screen esté cargada */
    private static final long ELORA_DELAY = 1000;

    /* Array de vistas que almacena las pantallas de la guia. Se leen desde MainActivity, lo que permite añadir de forma dinámica las necesarias */
    private View[] guideScreens;

    /* Indicador que lleva el conteo de los clicks del usuario de cara al lanzamiento de la Easter Egg del video */
    private int buttonClickCount = 0;

    /* Indicador de la pantalla de la guia en curso. Esto permitiría usar botones de retroceso. */
    private int currentScreen = 0;

    private SharedPreferences sharedPreferences;
    private final ActionBar actionBar;
    private NavController navController;
    private DrawerLayout drawerLayout;
    private MediaPlayer mediaPlayer;
    private Activity activity;

    /**
     * Constructor de UserGuideManager.
     *
     * @param activity Contexto de la actividad que utiliza la guía.
     * @param sharedPreferences SharedPreferences para gestionar el estado de la guía: Vista o No Vista
     * @param guideScreens Array de vistas correspondientes a las pantallas de la guía.
     * @param navController Controlador de navegación para manejar destinos. Permitirá la carga de los fragments de forma automática desde aquí.
     * @param drawerLayout Layout del contenedor de navegación.
     * @param actionBar Barra de acción de la actividad.
     */
    public UserGuideManager(Activity activity, SharedPreferences sharedPreferences,
                            View[] guideScreens, NavController navController,
                            ConstraintLayout drawerLayout, ActionBar actionBar) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
        this.guideScreens = guideScreens;
        this.navController = navController;
        this.drawerLayout = drawerLayout;
        this.actionBar = actionBar;

        if (guideScreens[0] == null) {
            Log.i(TAG, "Constructor userguidemanager -> array de pantallas vacio");
        }
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

    /**
     * Inicia la guía del usuario si no ha sido vista previamente.
     *
     * Este método realiza las siguientes acciones:
     * 1. Obtiene el estado de visualización de la guía desde las SharedPreferences ({@link SharedPreferences}),
     *    utilizando la clave {@link #SETTING_VIEW_GUIDE}. Si no existe un valor previo, se asume {@code false}.
     * 2. Si la guía no ha sido vista previamente ({@code isViewed} es {@code false}):
     *    - Bloquea la interfaz de usuario (UI) llamando a {@link #toLockUI(boolean)} con el valor {@code true}.
     *    - Muestra la pantalla actual de la guía llamando a {@link #showScreen(int)}.
     *
     * @see SharedPreferences#getBoolean(String, boolean)
     * @see #toLockUI(boolean)
     * @see #showScreen(int)
     * @see Log
     */
    public void startGuide() {
        boolean isViewed = sharedPreferences.getBoolean(SETTING_VIEW_GUIDE, false);
        Log.i(TAG, "startGuide -> isViewed is " + isViewed);
        if (!isViewed) {
            toLockUI(true);
            showScreen(currentScreen);
        }
    }

    /**
     * Muestra una pantalla específica de la guía con una animación de desvanecimiento (fade-in).
     *
     * Este método realiza las siguientes acciones:
     * 1. Verifica si el índice de la pantalla ({@code screenIndex}) es válido.
     * 2. Si la vista de la pantalla ({@code screenView}) es nula, se registra un mensaje de log y se detiene la ejecución.
     * 3. Configura la visibilidad de la pantalla y aplica una animación de desvanecimiento (fade-in) cargada desde {@code R.anim.fade_in}.
     * 4. Define un listener para la animación para asegurar la opacidad de la pantalla cuando la animación termine.
     * 5. Por si hay más elementos con animaciones llama a {@link #animateScreenElements(int)}.
     * 6. Configura la animación de Elora (si existe) con un retardo definido por {@link #ELORA_DELAY}.
     * 7. Si el índice de la pantalla no es válido, finaliza la guía llamando a {@link #endGuide(boolean)} con el valor {@code true}.
     *
     * @param screenIndex El índice de la pantalla que se desea mostrar.
     *
     * @see AnimationUtils#loadAnimation(Context, int)
     * @see Animation.AnimationListener
     * @see #animateScreenElements(int)
     * @see #setupEloraAnimation(int, ImageView)
     * @see #endGuide(boolean)
     * @see Log
     */
    private void showScreen(int screenIndex) {
        if (screenIndex < guideScreens.length) {
            View screenView = guideScreens[screenIndex];
            Log.i(TAG, "showScreen -> screen: " + screenIndex + ", screenView: " + screenView);

            if (screenView == null) {
                Log.i(TAG, "showScreen -> screenView es null para screen " + screenIndex);
                return;
            }

            // Cargamos las pantallas con fadeIn
            screenView.setAlpha(0f);
            screenView.setVisibility(View.VISIBLE);
            Log.i(TAG, "showScreen -> Definida screen visible: " + screenIndex);

            Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
            if (fadeIn == null) {
                Log.i(TAG, "showScreen -> Ha fallado la carga de la animación fadeIN");
            }
            fadeIn.setDuration(FADE_DURATION);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    Log.d(TAG, "showScreen -> FadeIn ha comenzado para la screen " + screenIndex);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    Log.d(TAG, "showScreen -> FadeIn finalizada para la screen " + screenIndex);
                    screenView.setAlpha(1f); // Asegurar opacidad final
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            screenView.startAnimation(fadeIn);

            // Aplicando animaciones según la pantalla en la que me encuentro
            animateScreenElements(screenIndex);

            // Configurar Elora
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                int eloraId = activity.getResources().getIdentifier(
                        "elora_" + (screenIndex + 1), "id", activity.getPackageName());
                ImageView eloraView = screenView.findViewById(eloraId);
                if (eloraView != null) {
                    Log.i(TAG, "showScreen -> Elora ImageView es nulo para screen " + screenIndex);
                    setupEloraAnimation(screenIndex, eloraView);
                }
            }, ELORA_DELAY);
        } else {
            endGuide(true);
        }
    }

    /**
     * Configura los botones de todas las pantallas de la guía.
     *
     * Este método recorre todas las pantallas de la guía y realiza las siguientes acciones:
     * 1. Configura los botones especiales (Easter Eggs) para las pantallas específicas:
     *    - Para la pantalla 2 (índice 1), configura el botón de fuego llamando a {@link #setupFireButton(View)}.
     *    - Para la pantalla 5 (índice 4), configura el botón de video llamando a {@link #setupVideoButton(View)}.
     *
     * 2. Configura los botones comunes para las pantallas 1 a 5:
     *    - El botón "continue_button" avanza a la siguiente pantalla llamando a {@link #nextScreen()}.
     *    - El botón "exit_guide" finaliza la guía llamando a {@link #endGuide(boolean)} con el valor {@code false}.
     *
     * 3. Configura los botones especiales para la pantalla 6 (índice 5):
     *    - El botón "button_close_guide" finaliza la guía llamando a {@link #endGuide(boolean)} con el valor {@code true}.
     *    - El botón "button_comenzar" reinicia la guía llamando a {@link #repeatGuide()}.
     *
     * Los identificadores de los botones se obtienen dinámicamente utilizando {@link Resources#getIdentifier(String, String, String)}.
     *
     * @see #setupFireButton(View)
     * @see #setupVideoButton(View)
     * @see #nextScreen()
     * @see #endGuide(boolean)
     * @see #repeatGuide()
     * @see Resources#getIdentifier(String, String, String)
     */
    private void setupGuideButtons() {
        for (int i = 0; i < guideScreens.length; i++) {
            View screen = guideScreens[i];

            // El caso de los Easter Eggs... Lo podia hacer de otro modo pero era por cambiar un poco y visualmente me queda limpio
            switch (i) {
                case 1:
                    setupFireButton(screen);
                    break;
                case 4:
                    setupVideoButton(screen);
                    break;
            }

            // Ahora preparamos el continue_button y el exit_guide de las pantallas 1 a 5
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
                    exitButton.setOnClickListener(v -> endGuide(false));
                }
            } else { // Pantalla 6 -> button_close_guide y button_comenzar
                Button closeButton = screen.findViewById(R.id.button_close_guide_6);
                if (closeButton != null) {
                    closeButton.setOnClickListener(v -> endGuide(true));
                }

                Button repeatButton = screen.findViewById(R.id.button_comenzar_6);
                if (repeatButton != null) {
                    repeatButton.setOnClickListener(v -> repeatGuide());
                }
            }
        }
    }

    /**
     * Anima los elementos de la pantalla de la guía según el número de pantalla proporcionado.
     *
     * Este método realiza las siguientes acciones para las pantallas 2 a 5 (índices 1 a 4):
     * 1. Anima el elemento "hamburguesa" (identificado dinámicamente por el nombre "hamburguesa_" + (screen + 1))
     *    utilizando una animación de desvanecimiento (fade-in) cargada desde el recurso {@code R.anim.fade_in}.
     * 2. Anima el botón "continue_button" (identificado dinámicamente por el nombre "continue_button_" + (screen + 1))
     *    utilizando una animación de pulso cargada desde el recurso {@code R.anim.pulse}.
     *
     * @param screenIndex El número de la pantalla actual (índice basado en 0). Solo se animan elementos para las pantallas 2 a 5.
     *
     * @see AnimationUtils#loadAnimation(Context, int)
     * @see View#startAnimation(Animation)
     * @see Log
     */
    private void animateScreenElements(int screenIndex) {
        // Animar hamburguesa para pantallas 2 a 5
        if (screenIndex >= 1 && screenIndex <= 4) {
            int hamburguesaId = activity.getResources().getIdentifier(
                    "hamburguesa_" + (screenIndex + 1), "id", activity.getPackageName());
            View hamburguesa = guideScreens[screenIndex].findViewById(hamburguesaId);
            if (hamburguesa != null) {
                hamburguesa.setAlpha(0f);
                //hamburguesa.setVisibility(View.VISIBLE);
                Log.i(TAG, "animateScreenElements -> hamburguesa se hace visible para screen " + screenIndex);
                Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                fadeIn.setDuration(FADE_DURATION);
            } else {
                Log.i(TAG, "animateScreenElements -> hamburguesa es nulo para screen " + screenIndex);
            }

            // Animar el continue_button de las pantallas 2 a 5 con pulse
            int continueButtonId = activity.getResources().getIdentifier(
                    "continue_button_" + (screenIndex + 1), "id", activity.getPackageName());
            Button continueButton = guideScreens[screenIndex].findViewById(continueButtonId);
            if (continueButton != null) {
                Animation pulse = AnimationUtils.loadAnimation(activity, R.anim.pulse);
                continueButton.startAnimation(pulse);
            }
        }
    }

    /**
     * Avanza a la siguiente pantalla de la guía y gestiona la navegación entre fragmentos pero
     * haciendo uso de los action creados en el nav_graph.xml para que tenga lugar la animación.
     *
     * Este método realiza las siguientes acciones:
     * 1. Oculta la pantalla actual de la guía estableciendo su visibilidad a {@link View#GONE}.
     * 2. Incrementa el índice de la pantalla actual ({@code currentScreen}).
     * 3. Gestiona la navegación entre fragmentos cuando se alcanzan las pantallas 2 y 3:
     *    - Si la pantalla actual es 2 (personajes), navega al fragmento {@code navigation_worlds} utilizando
     *      {@link NavController#navigate(int)} y muestra la pantalla después de un retardo de 300 ms.
     *    - Si la pantalla actual es 3 (mundos), navega al fragmento {@code navigation_collectibles} y muestra
     *      la pantalla después de un retardo de 300 ms.
     * 4. Para todos los casos, muestra la pantalla correspondiente llamando a {@link #showScreen(int)}. Esto
     *      hace que se cargue la siguiente pantalla de la guia.
     *
     * @see NavController#navigate(int)
     * @see Handler
     * @see Looper
     * @see #showScreen(int)
     */
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
        }

        // Muestra la pantalla de la guia
        showScreen(currentScreen);
    }

    /**
     * Finaliza la guía del usuario y realiza las operaciones necesarias para limpiar y guardar el estado.
     *
     * Este método realiza las siguientes acciones:
     * 1. Oculta todas las pantallas de la guía llamando a {@link #hideAllScreens()}.
     * 2. Desbloquea la interfaz de usuario (UI) llamando a {@link #toLockUI(boolean)} con el valor {@code false}.
     * 3. Libera la memoria utilizada por el reproductor de sonidos llamando a {@link SoundManager#freeMemoryPlayer()}.
     * 4. Guarda el estado de visualización de la guía llamando a {@link #setGuideVisualized(boolean)} con el valor
     *    proporcionado en el parámetro {@code isSeen}.
     *
     * @param isSeen Indica si la guía ha sido completamente vista por el usuario.
     *              - {@code true}: La guía ha sido vista completamente.
     *              - {@code false}: La guía no ha sido vista completamente. Se reproduce nuevamente.
     *
     * @see #hideAllScreens()
     * @see #toLockUI(boolean)
     * @see SoundManager#freeMemoryPlayer()
     * @see #setGuideVisualized(boolean)
     */
    public void endGuide(boolean isSeen) {
        hideAllScreens();
        toLockUI(false);
        SoundManager.freeMemoryPlayer();
        setGuideVisualized(isSeen);
    }

    /**
     * Configura y reproduce una animación y un sonido asociado para la imagen de Elora en función de la pantalla actual.
     *
     * Este método obtiene los recursos de animación y sonido correspondientes a la pantalla especificada
     * utilizando los métodos {@link #getAnimationResourceFromScreen(int)} y {@link #getSoundResourceFromScreen(int)}.
     * Si se encuentra un recurso de sonido válido, se reproduce utilizando {@link SoundManager#playSound(Context, int)}.
     * Además, se obtiene la duración del sonido para sincronizar la animación con la reproducción del sonido.
     *
     * Si se encuentra un recurso de animación válido, se asigna a la {@link ImageView} proporcionada
     * y se inicia la animación utilizando {@link AnimationDrawable#start()}.
     *
     * @param screen         El identificador de la pantalla actual para obtener los recursos
     *                       de animación y sonido correspondientes.
     * @param eloraImageView La {@link ImageView} en la que se mostrará la animación de Elora.
     *
     * @see SoundManager#playSound(Context, int)
     * @see SoundManager#getSoundDuration()
     * @see AnimationDrawable
     * @see ImageView#setImageDrawable(Drawable)
     */
    private void setupEloraAnimation(int screen, ImageView eloraImageView) {
        // Obtener la animación y el sonido correspondientes
        int animationRes = getAnimationResourceFromScreen(screen);
        int soundRes = getSoundResourceFromScreen(screen);

        // Reproducir el sonido
        if (soundRes != -1) {
            SoundManager.playSound(activity, soundRes);

            // Obtengo la duración del sonido, para adaptar el tiempo de reproduccion de la animación
            int soundDuration = SoundManager.getSoundDuration();

            // Configurar la animación
            if (animationRes != -1) {
                AnimationDrawable animation = (AnimationDrawable) activity.getResources().getDrawable(animationRes);
                eloraImageView.setImageDrawable(animation);
                animation.start();
            }
        }
    }

    /**
     * Configura el comportamiento del botón de "fuego" en la pantalla de la guía.
     *
     * Este método asigna un listener de tipo {@link View.OnLongClickListener} al botón
     * identificado por {@code R.id.button_fire_dragon}. Cuando el usuario mantiene presionado
     * el botón, se realiza lo siguiente:
     * 1. Se hace visible la vista {@code DragonFireView} (identificada por {@code R.id.dragonFireView}).
     * 2. Se inicia la animación de fuego llamando al método {@link DragonFireView#launchFire()}.
     * 3. Se reproduce un sonido de rugido utilizando {@link SoundManager#playSound(Context, int)}.
     * 4. Después de un tiempo definido por {@link #TIME_ANIMATIONS}, se detiene la animación
     *    y se oculta la vista {@code DragonFireView}.
     *
     * @param guideScreen La vista que contiene los elementos de la pantalla de la guía.
     *
     * @see DragonFireView
     * @see SoundManager#playSound(Context, int)
     * @see Handler
     * @see Looper
     */
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

    /**
     * Configura el comportamiento del botón de "fuego" para reproducir un video en la pantalla de la guía.
     *
     * Este método asigna un listener de tipo {@link View.OnClickListener} al botón
     * identificado por {@code R.id.button_fire_dragon}. Cuando el usuario hace clic en el botón,
     * se incrementa un contador. Si el botón se presiona 4 veces, se reproduce un video
     * utilizando {@link VideoManager#playVideo(Context, int)}. Después de reproducir el video,
     * el contador se reinicia.
     *
     * @param guideScreen La vista que contiene los elementos de la pantalla de la guía.
     *
     * @see VideoManager
     * @see VideoManager#playVideo(Context, int)
     */
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

    /**
     * Actualiza el estado de visualización de la guía en las SharedPreferences.
     *
     * @param isVisualized Indica si la guía ha sido visualizada.
     *                    - {@code true}: La guía ha sido visualizada.
     *                    - {@code false}: La guía no ha sido visualizada.
     *
     * @see SharedPreferences
     * @see SharedPreferences.Editor#putBoolean(String, boolean)
     * @see SharedPreferences.Editor#apply()
     * */
    public void setGuideVisualized(boolean isVisualized) {
        sharedPreferences.edit().putBoolean(SETTING_VIEW_GUIDE, isVisualized).apply();
    }

    /**
     * Bloquea o desbloquea la interfaz de usuario (BottomNavigation y Layout).
     *
     * @param toLock Indica si el DrawerLayout debe bloquearse o desbloquearse.
     *               - {@code true}: Bloquea el DrawerLayout en su estado cerrado.
     *               - {@code false}: Desbloquea el DrawerLayout.
     *
     * @see DrawerLayout#setDrawerLockMode(int)
     * @see DrawerLayout#LOCK_MODE_LOCKED_CLOSED
     * @see DrawerLayout#LOCK_MODE_UNLOCKED
     */
    public void toLockUI(boolean toLock) {
        if (drawerLayout != null) {
            if (toLock) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
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

    /**
     * Obtiene el ID del elemento "hamburguesa" según el índice de la pantalla.
     *
     * @param screenIndex Índice de la pantalla.
     * @return ID del recurso del elemento "hamburguesa", o -1 si no aplica.
     */
    private int getHamburguesaId(int screenIndex) {
        switch (screenIndex) {
            case 1: return R.id.hamburguesa_2;
            case 2: return R.id.hamburguesa_3;
            case 3: return R.id.hamburguesa_4;
            case 4: return R.id.hamburguesa_5;
            default: return -1;
        }
    }

    /**
     * Obtiene el ID del botón "Continuar" según el índice de la pantalla.
     *
     * @param screenIndex Índice de la pantalla.
     * @return ID del recurso del botón "Continuar".
     */
    private int getContinueButtonId(int screenIndex) {
        switch (screenIndex) {
            case 0: return R.id.continue_button_1;
            case 1: return R.id.continue_button_2;
            case 2: return R.id.continue_button_3;
            case 3: return R.id.continue_button_4;
            case 4: return R.id.continue_button_5;
            default: return -1;
        }
    }

    /**
     * Obtiene el ID del botón "Salir" según el índice de la pantalla.
     *
     * @param screenIndex Índice de la pantalla.
     * @return ID del recurso del botón "Salir".
     */
    private int getExitButtonId(int screenIndex) {
        switch (screenIndex) {
            case 0: return R.id.exit_guide_1;
            case 1: return R.id.exit_guide_2;
            case 2: return R.id.exit_guide_3;
            case 3: return R.id.exit_guide_4;
            case 4: return R.id.exit_guide_5;
            default: return -1;
        }
    }

    /**
     * Obtiene el ID de la ImageView de Elora según el índice de la pantalla.
     *
     * @param screenIndex Índice de la pantalla.
     * @return ID del recurso.
     */
    private ImageView getEloraImageViewForScreen(View screenView, int screenIndex) {
        switch (screenIndex) {
            case 0: return screenView.findViewById(R.id.elora_1);
            case 1: return screenView.findViewById(R.id.elora_2);
            case 2: return screenView.findViewById(R.id.elora_3);
            case 3: return screenView.findViewById(R.id.elora_4);
            case 4: return screenView.findViewById(R.id.elora_5);
            case 5: return screenView.findViewById(R.id.elora_6);
            default: return null;   // Sin imagen
        }
    }

    /**
     * Obtiene el ID de la animacion de Elora según el índice de la pantalla.
     *
     * @param screenIndex Índice de la pantalla.
     * @return ID del recurso.
     */
    private int getAnimationResourceFromScreen(int screenIndex) {
        switch (screenIndex) {
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

    /**
     * Obtiene el ID de los archivos de sonido animacion de Elora según el índice de la pantalla.
     *
     * @param screenIndex Índice de la pantalla.
     * @return ID del recurso.
     */
    private int getSoundResourceFromScreen(int screenIndex) {
        switch (screenIndex) {
            case 0: return R.raw.elora_hablando_1;
            case 1: return R.raw.elora_hablando_2;
            case 2: return R.raw.elora_hablando_3;
            case 3: return R.raw.elora_hablando_4;
            case 4: return R.raw.elora_hablando_5;
            case 5: return R.raw.elora_hablando_6;
            default: return -1;  // Sin sonido
        }
    }
}
