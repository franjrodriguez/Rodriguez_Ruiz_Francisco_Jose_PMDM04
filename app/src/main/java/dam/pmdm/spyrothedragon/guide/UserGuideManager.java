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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;

import dam.pmdm.spyrothedragon.R;

/**
 * Clase que gestiona la guía de usuario de la aplicación Spyro The Dragon.
 * Controla la visualización secuencial de pantallas de guía con animaciones y navegación.
 * También se encarga de lanzar sonidos y las acciones requeridas para los dos Easter Eggs:
 *      - En la pantalla coleccionables (guide_screen_4) -> Muestra un video como respuesta a 4 clicks.
 *      - En la pantalla personajes (guide_screen_2) -> Muestra llamas como respuesta a una pulsación larga.
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
    private static final long FADE_DURATION = 3000;

    /* Se concibe este tiempo de espera antes de que comience la animación de Elora para que la screen esté cargada */
    private static final long ELORA_DELAY = 3000;

    /* Array de vistas que almacena las pantallas de la guia. Se leen desde MainActivity, lo que permite añadir de forma dinámica las necesarias */
    private View[] guideScreens;

    /* Indicador que lleva el conteo de los clicks del usuario de cara al lanzamiento de la Easter Egg del video */
    private int buttonClickCount = 0;

    /* Indicador de la pantalla de la guia en curso. Esto permitiría usar botones de retroceso. */
    private int currentScreen = 0;

    private SharedPreferences sharedPreferences;
    private final Toolbar toolbar;
    private NavController navController;
    private ConstraintLayout constraintLayout;
    private MediaPlayer mediaPlayer;
    private FragmentActivity activity;

    /**
     * Constructor de UserGuideManager.
     *
     * @param activity Contexto de la actividad que utiliza la guía.
     * @param sharedPreferences SharedPreferences para gestionar el estado de la guía: Vista o No Vista.
     * @param guideScreens Array de vistas correspondientes a las pantallas de la guía.
     * @param navController Controlador de navegación para manejar destinos. Permitirá la carga de los fragments de forma automática desde aquí.
     * @param constraintLayout Layout del contenedor de navegación.
     * @param toolbar Barra de acción de la actividad.
     */
    public UserGuideManager(FragmentActivity activity, SharedPreferences sharedPreferences,
                            View[] guideScreens, NavController navController,
                            ConstraintLayout constraintLayout, Toolbar toolbar) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
        this.guideScreens = guideScreens;
        this.navController = navController;
        this.constraintLayout = constraintLayout;
        this.toolbar = toolbar;

        if (guideScreens[0] == null) {
            Log.i(TAG, "Constructor userguidemanager -> array de pantallas vacio");
        }
        //
//        if (toolbar != null) {
//            toolbar.setHomeButtonEnabled(false);
//            toolbar.setDisplayHomeAsUpEnabled(false);
//            toolbar.setDisplayShowHomeEnabled(false);
//        }
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
            viewUserGuide(true);
            showScreen(currentScreen);
        }
    }

    /**
     * Muestra u oculta la guía de usuario.
     *
     * @param toOpen Si es {@code true}, muestra la guía; si es {@code false}, la oculta.
     */
    private void viewUserGuide(boolean toOpen) {
        FrameLayout guideLayout = constraintLayout.findViewById(R.id.interactive_guide_layout);
        Log.i(TAG, "viewUserGuide -> guideLayout: " + guideLayout);

        if (guideLayout == null) {
            Log.i(TAG, "viewUserGuide -> guideLayout es null");
            return;
        }
        if (toOpen) {
            guideLayout.setVisibility(View.VISIBLE);
            guideLayout.bringToFront();
            Log.i(TAG, "viewUserGuide -> la guideLayout ha quedado visible. Deberia poder verse");
        } else {
            guideLayout.setVisibility(View.GONE);
            Log.i(TAG, "viewUserGuide -> la guideLayout ha quedado oculta -- bajo las sombras de la tiniebla --");
        }
    }

    /**
     * Muestra una pantalla específica de la guía con una animación de desvanecimiento (fade-in), mientras
     * el resto de pantallas las muestra tal cual. No tendría (desde mi punto de vista) sentido aplicar un efecto
     * a las pantallas 2 a 5 dado que como son transparentes (background) no se apreciaría. Sí, sin embargo, se
     * aplicarán posteriormetne efectos y animaciones a los componentes de dichas pantallas en otros métodos.
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
        if (screenIndex < guideScreens.length && screenIndex >= 0) {
            View screenView = guideScreens[screenIndex];
            Log.i(TAG, "showScreen -> screen: " + screenIndex + ", screenView: " + screenView);

            if (screenView == null) {
                Log.i(TAG, "showScreen -> screenView es null para screen " + screenIndex);
                return;
            }

            // Cargamos las pantallas 1 y 6 con fadeIn (las restantes creo que no tiene sentido ya que es mas
            // relevante los desplazamientos de los fragments
            switch (screenIndex) {
                case 0:     // Primera pantalla de la guia
                    screenView.setVisibility(View.VISIBLE);
                    Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                    if (fadeIn == null) {
                        Log.e(TAG, "showScreen -> Ha fallado la carga de la animación fadeIN");
                    }
                    break;
                default:
                    screenView.setVisibility(View.VISIBLE); // Para el resto de pantallas.
            }

            // Aplicando animaciones según la pantalla en la que me encuentro
            animateScreenElements(screenIndex);

            // Configurar Elora
            FrameLayout guideLayout = constraintLayout.findViewById(R.id.interactive_guide_layout);
            Log.i(TAG, "showScreen -> Visibilidad antes de Elora: " + screenView.getVisibility() + ", guideLayout: " + guideLayout.getVisibility());

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.i(TAG, "showScreen -> Visibilidad en Handler: " + screenView.getVisibility() + ", guideLayout: " + guideLayout.getVisibility());

                int eloraId = activity.getResources().getIdentifier(
                        "elora_" + (screenIndex + 1), "id", activity.getPackageName());
                ImageView eloraView = screenView.findViewById(eloraId);
                if (eloraView != null) {
                    Log.i(TAG, "showScreen -> Elora ImageView es nulo para screen " + screenIndex);
                    setupEloraAnimation(screenIndex, eloraView, getRepetitionNarrationOfElora(screenIndex));
                } else {
                    Log.i(TAG, "showScreen -> Elora ImageView ENCONTRADO para el screen " + screenIndex);
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

            // El caso de los Easter Eggs...
            switch (i) {
                case 1:     // guide_screen_2
                    setupFireButton(screen);
                    break;
                case 3:     // guide_screen_4
                    setupVideoButton(screen, this.activity);
                    break;
            }

            // Ahora preparamos el continue_button y el exit_guide de las pantallas 1 a 5
            if (i < 5) { // Pantallas 1 a 5
                int continueButtonId = activity.getResources().getIdentifier(
                        "continue_button_" + (i + 1), "id", activity.getPackageName());
                Button continueButton = screen.findViewById(continueButtonId);
                if (continueButton != null) {
                    continueButton.setOnClickListener(v -> {
                        Log.i(TAG, "continue_button_ clicado");
                        SoundManager.playSound(activity, R.raw.next_screen);
                        nextScreen();
                    });
                }

                int previousButtonId = activity.getResources().getIdentifier(
                        "button_prev_" + (i + 1), "id", activity.getPackageName());
                Button previousButton = screen.findViewById(previousButtonId);
                if (previousButton != null) {
                    previousButton.setOnClickListener(v -> {
                        SoundManager.playSound(activity, R.raw.next_screen);
                        Log.i(TAG, "previous_button_ clicado");
                        prevScreen();
                    });
                }

                int exitButtonId = activity.getResources().getIdentifier(
                        "exit_guide_" + (i + 1), "id", activity.getPackageName());
                View exitButton = screen.findViewById(exitButtonId);
                if (exitButton != null) {
                    exitButton.setOnClickListener(v -> {
                        SoundManager.playSound(activity, R.raw.oveja_byebye);
                        Log.i(TAG, "exit_button_ clicado");
                        showExitConfirmationDialog(false);
                    });
                }

            } else { // Pantalla 6 -> button_close_guide y button_comenzar
                Button closeButton = screen.findViewById(R.id.button_close_guide_6);
                if (closeButton != null) {
                    // Detallo el proceso:
                    //      1. Se queda a la escucha de que toquen el boton... Adios!!!
                    //      2. Comienza el fadeOut que dura 3500
                    //      3. Se crea un retraso de 3500 para que se lance el proceso en sí mismo de final de guia
                    closeButton.setOnClickListener(v -> {
                        Animation fadeOut = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
                        if (fadeOut == null) {
                            Log.e(TAG, "showScreen -> Ha fallado la carga de la animación fadeOUT");
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(()-> {
                            endGuide(true);
                        }, 3500);
                    });
                }

                Button repeatButton = screen.findViewById(R.id.button_comenzar_6);
                if (repeatButton != null) {
                    repeatButton.setOnClickListener(v -> repeatGuide());
                }
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación para salir de la guía, dependiendo de si el usuario ha completado todas las pantallas.
     * Si el usuario ha visto toda la guía (isSeen es true), la guía se finaliza directamente sin mostrar un diálogo.
     * Si el usuario no ha completado la guía (isSeen es false), se muestra un diálogo preguntando si desea salir,
     * con opciones para confirmar (Sí) o cancelar (No).
     *
     * @param isSeen Indica si el usuario ha visto todas las pantallas de la guía. Si es true, no se muestra el diálogo
     *               y la guía se finaliza inmediatamente; si es false, se muestra el diálogo de confirmación.
     */
    private void showExitConfirmationDialog(boolean isSeen) {
        if (isSeen) {
            // Si ha visto toda la guía, salir directamente sin diálogo
            endGuide(true);
        } else {
            // Mostrar diálogo de confirmación
            new AlertDialog.Builder(activity) // Usar 'activity' como contexto
                    .setTitle(R.string.exit_guide_title)
                    .setMessage(R.string.exit_guide_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> endGuide(false))
                    .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * Anima los elementos de la pantalla de la guía según el número de pantalla proporcionado.
     *
     * Este método realiza las siguientes acciones para las pantallas 2 a 5 (índices 1 a 4):
     * 1. Anima el elemento "hamburguesa" (identificado dinámicamente por el nombre "hamburguesa_" + (screen + 1))
     *    utilizando una animación de desvanecimiento (fade-in) cargada desde el recurso {@code R.anim.fade_in}.
     * 2. Anima el botón "bookmark_button" (identificado dinámicamente por el nombre "bookmark_button_" + (screen + 1))
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
                hamburguesa.setVisibility(View.VISIBLE);
                Log.i(TAG, "animateScreenElement -> visionando hamburguesa: " + hamburguesaId);
                Animation fadeIn = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
                hamburguesa.setAnimation(fadeIn);
            } else {
                Log.e(TAG, "animateScreenElements -> hamburguesa es nulo para screen " + screenIndex);
            }

            // Animar el bookmark_button de las pantallas 2 a 5 con pulse
            int bookmarkButtonId = activity.getResources().getIdentifier(
                    "bookmark_button_" + (screenIndex + 1), "id", activity.getPackageName());
            Button bookmarkButton = guideScreens[screenIndex].findViewById(bookmarkButtonId);
            if (bookmarkButton != null) {
                Animation pulse = AnimationUtils.loadAnimation(activity, R.anim.pulse);
                bookmarkButton.startAnimation(pulse);
                // Emite sonido...
                bookmarkButton.setOnClickListener(v -> {
                    SoundManager.playSound(activity, R.raw.portal);
                });
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
     * Retrocede a la anterior pantalla de la guía y gestiona la navegación entre fragmentos pero
     * haciendo uso de los action creados en el nav_graph.xml para que tenga lugar la animación.
     *
     * Este método realiza las siguientes acciones:
     * 1. Oculta la pantalla actual de la guía estableciendo su visibilidad a {@link View#GONE}.
     * 2. Incrementa el índice de la pantalla actual ({@code currentScreen}).
     * 3. Gestiona la navegación entre fragmentos cuando se alcanzan las pantallas 2 y 3
     * 4. Para todos los casos, muestra la pantalla correspondiente llamando a {@link #showScreen(int)}. Esto
     *      hace que se cargue la pantalla anterior de la guia.
     * (NOTA: Este método no está completamente funcional de momento. No se usa, para lo que se han dejado
     * los objetos correspondientes - button prev_button - con atributo android:visibility="gone"
     *
     * @see NavController#navigate(int)
     * @see Handler
     * @see Looper
     * @see #showScreen(int)
     */
    public void prevScreen() {
        guideScreens[currentScreen].setVisibility(View.GONE);
        currentScreen--;

        if (currentScreen >= 0) {
            // Navegacion de fragment entre las pantallas 2 y 3 unicamente
            if (currentScreen == 3) {
                navController.navigate(R.id.action_navigation_collectibles_to_navigation_worlds);
                new Handler(Looper.getMainLooper()).postDelayed(() -> showScreen(currentScreen), 300);
            } else if (currentScreen == 2) {
                navController.navigate(R.id.action_navigation_worlds_to_navigation_characters);
                new Handler(Looper.getMainLooper()).postDelayed(() -> showScreen(currentScreen), 300);
            }
            // Muestra la pantalla de la guia
            showScreen(currentScreen);
        }
    }

    /**
     * Finaliza la guía del usuario y realiza las operaciones necesarias para limpiar y guardar el estado.
     * Para proceder, se lleva a cabo la solicitud al usuario para cancelar la guía usando una pantalla de alerta
     *
     * OBSEVACIÓN: Unicamente se solicita confirmación para salir de la guía en el caso de que se halla
     *              pulsado el botón de "salir" desde alguna pantalla que no sea la última de la guia,
     *              ya que se sobreentiende que en este caso ha llegado al final y no tiene sentido la petición.
     *
     * Este método realiza las siguientes acciones (Si la petición del usuario ha sido Avandonar la Guía):
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
     * @see SoundManager#freeMemoryPlayer()
     * @see #setGuideVisualized(boolean)
     * @see #viewUserGuide(boolean)
     */
    public void endGuide(boolean isSeen) {
        hideAllScreens();
        SoundManager.freeMemoryPlayer();
        setGuideVisualized(isSeen);
        viewUserGuide(false);
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
    private void setupEloraAnimation(int screen, ImageView eloraImageView, int paramRepeatCount) {
        // Obtener la animación y el sonido correspondientes
        final int repeatCount = paramRepeatCount;
        int animationRes = getAnimationResourceFromScreen(screen);
        int soundRes = getSoundResourceFromScreen(screen);

        // Variable para la duración de la animación, inicializada por defecto
        int animationDuration = 0;

        // Reproducir el sonido
        if (soundRes != -1) {
            SoundManager.playSound(activity, soundRes);
        }

        // Configurar la animación
        if (animationRes != -1) {
            AnimationDrawable animation = (AnimationDrawable) activity.getResources().getDrawable(animationRes);
            eloraImageView.setImageDrawable(animation);

            // Calcular la duración total de una ronda de la animación
            for (int i = 0; i < animation.getNumberOfFrames(); i++) {
                animationDuration += animation.getDuration(i);
            }

            // Hacer animationDuration final para que sea capturada por el Runnable
            final int finalAnimationDuration = animationDuration;

            // Reproducir la animación el número de veces deseado
            animation.start();

            // Usar un Handler con Looper.getMainLooper()
            final Handler handler = new Handler(Looper.getMainLooper());
            Runnable repeatRunnable = new Runnable() {
                int currentRepeat = 0;

                @Override
                public void run() {
                    if (currentRepeat < repeatCount - 1) { // -1 porque ya se reproduce una vez al inicio
                        animation.stop(); // Detener la animación actual
                        animation.start(); // Reiniciarla
                        currentRepeat++;
                        handler.postDelayed(this, finalAnimationDuration);
                    }
                }
            };
            // Iniciar las repeticiones después de la primera ronda (solo si hay duración)
            if (finalAnimationDuration > 0) {
                handler.postDelayed(repeatRunnable, finalAnimationDuration);
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
     * 2. Se inicia la animación de fuego llamando al método {@link FlameView#launchFire()}.
     * 3. Se reproduce un sonido de rugido utilizando {@link SoundManager#playSound(Context, int)}.
     * 4. Después de un tiempo definido por {@link #TIME_ANIMATIONS}, se detiene la animación
     *    y se oculta la vista {@code DragonFireView}.
     *
     * @param guideScreen La vista que contiene los elementos de la pantalla de la guía.
     *
     * @see FlameView
     * @see SoundManager#playSound(Context, int)
     * @see Handler
     * @see Looper
     */
    private void setupFireButton(View guideScreen) {
        Button buttonFireDragon = guideScreen.findViewById(R.id.button_fire_dragon);
        if (buttonFireDragon != null) {
            buttonFireDragon.setOnLongClickListener(v -> {
                FlameView flameView = guideScreen.findViewById(R.id.flame_view);
                if (flameView != null) {
                    flameView.setVisibility(View.VISIBLE);
                    flameView.startFiring();
                    SoundManager.playSound(v.getContext(), R.raw.roar);
                    // Detener el fuego después de 3 segundos
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        flameView.stopFiring();
                        flameView.setVisibility(View.GONE);
                    }, 3000);
                }
                return true;    // Esto es para indicar que el evento fue manejado
            });
        }
    }

    /**
     * Configura el comportamiento del botón de "video" para reproducir un video en la pantalla de la guía.
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
    private void setupVideoButton(View guideScreen, Activity activity) {
        Button buttonPlayVideo = guideScreen.findViewById(R.id.button_play_video);
        final VideoView videoView = activity.findViewById(R.id.video_view);
        VideoManager videoManager = new VideoManager(activity, videoView, toolbar);
        FrameLayout overlayVideo = activity.findViewById(R.id.overlay_video); // FrameLayout que cubre la pantalla

        if (buttonPlayVideo != null) {
            buttonPlayVideo.setOnClickListener(v -> {
                SoundManager.playSound(activity, R.raw.crow);
                buttonClickCount++;

                // Si se ha pulsado 4 veces, reproducir el video
                if (buttonClickCount == 4) {
                    buttonClickCount = 0; // Reiniciar el contador

                    if (videoView != null && overlayVideo != null) {    // La capa visible la gestiona VideoManager
                        // Reproducir el video
                        Log.i(TAG, "Tengo videoView y overlayVideo asi que empiezo la pelicula...");
                        videoManager.playVideo(R.raw.video_of_spyrothedragon, overlayVideo);
                    } else {
                        Log.e(TAG, "setupVideoButton -> videoView: " + videoView);
                        Log.e(TAG, "setupVideoButton -> overlayVideo: " + overlayVideo);
                    }
                }
            });
        }
    }

    /**
     * Configura la animación de tipo PULSE para un botón. Inicialmente la idea es para el botón que
     * señala al usuario donde debe pulsar, pero podría usarse para cualquiera.
     *
     * Este método realiza las siguientes acciones:
     * 1. Limpia cualquier animación previa asociada al botón.
     * 2. Carga una animación de tipo "pulsar" desde los recursos de la aplicación.
     * 3. Inicia la animación en el botón.
     *
     * @param button El botón al que se le aplicará la animación y el comportamiento de clic.
     *
     * @see AnimationUtils#loadAnimation(Context, int)
     * @see Button#clearAnimation()
     * @see Button#startAnimation(Animation)
     */
    private void setupContinueButtonAnimation(Button button) {
        // Primero limpiamos cualquier animación previa
        button.clearAnimation();

        // Creamos la animación tipo Pulsar sobre el recuadro (Button) que indica el boton de la guia
        Animation pulseAnimation = AnimationUtils.loadAnimation(activity, R.anim.pulse);

        // Iniciamos la animación
        button.startAnimation(pulseAnimation);
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
     * Reinicia la guía interactiva volviendo a la primera pantalla.
     * <p>
     * Este método oculta todas las pantallas actuales, reinicia el índice de la pantalla
     * actual a la primera (índice 0) y muestra nuevamente la primera pantalla.
     * </p>
     */
    private void repeatGuide() {
        hideAllScreens();
        currentScreen = 0;
        showScreen(currentScreen);
    }

    /**
     * Oculta todas las pantallas de la guía, deteniendo cualquier animación en curso y estableciendo su visibilidad a GONE.
     *
     * Este método recorre una lista de vistas (`guideScreens`) y realiza las siguientes acciones para cada una:
     * 1. Detiene cualquier animación asociada a la vista.
     * 2. Establece la visibilidad de la vista a `View.GONE`, lo que la oculta y no reserva espacio en el diseño.
     *
     * @see View#clearAnimation()
     * @see View#setVisibility(int)
     */
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
     * Obtiene el ID del botón "BookMark" según el índice de la pantalla.
     *
     * @param screenIndex Índice de la pantalla.
     * @return ID del recurso del botón "BookMark".
     */
    private int getBookMarkButtonId(int screenIndex) {
        switch (screenIndex) {
            case 1: return R.id.bookmark_button_2;
            case 2: return R.id.bookmark_button_3;
            case 3: return R.id.bookmark_button_4;
            case 4: return R.id.bookmark_button_5;
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
     * Obtiene el número de repeticiones de la animacion de Elora según el índice de la pantalla.
     * Con este método se ha pretendido intentar de un modo precario (principalmente por falta de tiempo)
     * de ajustar el tiempo de animación al de la narración auditiva. Por lo tanto es algo aproximado.
     *
     * @param screenIndex Índice de la pantalla.
     * @return ID del recurso.
     */
    private int getRepetitionNarrationOfElora(int screenIndex) {
        switch (screenIndex) {
            case 0:
                return 6;
            case 5:
                return 2;
            case 1:
            case 3:
                return 2;
            case 2:
            case 4:
                return 3;
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
            case 0: return R.raw.narracion_elora_1;
            case 1: return R.raw.narracion_elora_2;
            case 2: return R.raw.narracion_elora_3;
            case 3: return R.raw.narracion_elora_4;
            case 4: return R.raw.narracion_elora_5;
            case 5: return R.raw.narracion_elora_6;
            default: return -1;  // Sin sonido
        }
    }
}
