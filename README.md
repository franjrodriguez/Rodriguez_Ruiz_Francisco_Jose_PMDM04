README.md

## Tarea Unidad 4 PMDM - Spyro The Dragon
#### Autor: Francisco José Rodríguez Ruiz

Módulo FP DAM PMDM (Distancia)

### Índice de contenidos
- **Introducción**: Explica brevemente el propósito de la aplicación y qué hace. 
- **Características principales**: Describe las funcionalidades clave de la aplicación. 
- **Tecnologías utilizadas**: Enumera las principales tecnologías y librerías empleadas en el proyecto. 
- **Instrucciones de uso**: Proporciona pasos para clonar el repositorio e instalar cualquier dependencia necesaria para ejecutar la aplicación. 
- **Conclusiones del desarrollador**: Reflexiona sobre el proceso de desarrollo y cualquier desafío o aprendizaje obtenido durante el proyecto. 
- **Capturas de pantalla** (opcional): Si lo deseas, agrega imágenes que muestren la interfaz o funcionalidades de la app.
***
### Introducción
En este proyecto se trata de actualizar la app (que ha ya está creada) con una guía de usuario que explique como funciona la app. La idea radica en que actualmente aquellas app que consigan atraer la atención del usuario, son las que pueden tener futuro en un mercado competitivo.

Esta guia interactiva propone (como objetivo):

- Diseñar un entorno donde el usuario pueda aprender que aspectos son los más relevantes en esta app.

Para ello, a través del uso de animaciones, sonidos, elementos gráficos, se propone enriquecer la experiencia del usuario.

Con esto en mente:

- Si el usuario, no ha visionado la guía comenzará de forma automática el lanzamiento de la misma. En otro caso (El booleano almacenado en las SharedPreferences lo dirá) directamente comenzará la app.
- Desde ese momento, se hace cargo de la app, la guía de usuario, llevando a cabo, incluso, las funciones propias del usuario, como si este hiciera click sobre cada uno de los botones del menú inferior, se irá mostrando cada una de las pantallas de la app, sobreponiendo a cada una la información relacionada con la guía de usuario.
- La guía comienza mostrando una pantalla en la que se da la bienvenida al usuario. Dado que se presupone que la app podría ser usada por niños que no saben leer, se ha decidido usar una narración para cada pantalla de la guía. 
- En la misma, se permite acceder a Easter Egg en la segunda y cuarta pantalla. En la primera, presionando durante un tiempo sobre la imagen de Spyro, este lanzará una llama de fuego. En la segunda, haciendo cuatro clicks sobre la imagen del diamante, se podrá visionar un video sobre spyro.
- Además de eso, se muestra al usuario información para cada pantalla.

##### La app se desarrolla utilizando el IDE Android Studio con la siguiente estructura:
- IDE Android Studio
- JDK Versión 8
- Gradle Versión 8.10.2
  
***
### Características principales
Describe las funcionalidades clave de la aplicación.
Las principales funcionalidades de la app son estas:

1. **Diseños XML**
   
   * Utilizando la clase Drawable, se muestra al usuario la información qeu requiere para el uso de la app.
   
2. **Pantallas de la guía**
   
   * A través de varias pantallas (mientra la app cambia de forma automática entre ellas) indicando  lo que el usuario va a ir encontrando en cada una de ellas.
   * Además de esto, podrá encontrar Easter Eggs en dos de las pantallas.
   * La pantalla principal da la bienvenida a la guía de usuario y tras pantallas mostradas durante 
   
3. **Easter Eggs**
   
   * En la pantalla de personajes, si el usuario presiona con un click mantenido la imagen de Spyro, se muestra una llama saliendo de la boca de Spyro.
   * En la pantalla de coleccionables, si el usuario presiona cuatro veces sobre la gema, se mostrará un video sobre el juego de Spyro.
   
5. **Ajustes** (de forma implicita)
   
   * La app gestiona si el usuario ha visualizado previamente la guia de usuario. Para ello se hace uso de las SharedPreferences, almacenando el estado de la visualización de la guía (true si ya ha sido visualizada o false en el caso contrario).
	
	   ------
	
	   

### Tecnologías utilizadas
Las principales tecnologías y librerías usadas en el proyecto se relacionan a continuación:
* **Android SDK**:
  - La aplicación está desarrollada utilizando el **Android Software Development Kit (SDK)**, que proporciona las herramientas y APIs necesarias para construir aplicaciones Android.
* **Java**:
  - El código de la aplicación está escrito en **Java**, que es uno de los lenguajes principales para el desarrollo de aplicaciones Android.
* **XML**:
  - Se utiliza **XML** para definir la interfaz de usuario (UI) en archivos de diseño como `activity_main.xml` y otros layouts. También se usa en el archivo `AndroidManifest.xml` para definir la configuración de la aplicación, como permisos, actividades y servicios.
* **Navigation Component**:
  - La aplicación utiliza el **Navigation Component** de Android Jetpack para gestionar la navegación entre fragmentos. Esto permite una navegación fluida y gestionada entre las diferentes pantallas de la aplicación.
* **SharedPreferences**:
  - Se utiliza **SharedPreferences** para almacenar y recuperar datos simples, como el estado de visualización de la guía de usuario (`isViewedGuide`).
* **MediaPlayer**:
  - Para la reproducción de sonidos, se utiliza la clase **MediaPlayer**. Esto permite reproducir efectos de sonido y narraciones en la guía de usuario.
* **VideoView**:
  - Para la reproducción de videos, se utiliza **VideoView**, que permite incrustar y reproducir videos en la aplicación.
* **Canvas y Custom Views**:
  - La aplicación utiliza **Canvas** para dibujar animaciones personalizadas, como las llamas de Spyro en la clase `FlameView`. Esto permite crear efectos visuales personalizados.
* **Animaciones**:
  - Se utilizan animaciones definidas en XML (como `fade_in`, `fade_out`, y `pulse`) para mejorar la experiencia del usuario. Estas animaciones se aplican a elementos de la interfaz de usuario.
* **Toolbar y ActionBar**:
  - La aplicación utiliza **Toolbar** y **ActionBar** para proporcionar una barra de navegación y opciones de menú en la parte superior de la pantalla.
* **Fragmentos**:
  - La aplicación está estructurada en **fragmentos**, que son componentes reutilizables que permiten una gestión modular de la interfaz de usuario. Los fragmentos se gestionan mediante el **NavController**.
* **Handler y Looper**:
  - Se utilizan **Handler** y **Looper** para gestionar tareas asíncronas y retrasos en la ejecución de código, como la aparición de animaciones después de un cierto tiempo.
* **AlertDialog**:
  - Se utiliza **AlertDialog** para mostrar diálogos de confirmación y mensajes al usuario, como el diálogo de salida de la guía.
* **FrameLayout y ConstraintLayout**:
  - Se utilizan **FrameLayout** y **ConstraintLayout** para organizar y posicionar los elementos de la interfaz de usuario de manera flexible.

***
### Instrucciones de uso
* Pasos para clonar el repositorio (Desde Android Studio)
  * Entramos en Android Studio IDE
  * Si no tenemos ningún proyecto abierto, pinchamos sobre el botón [Clone Repository]
  * En el caso de estar trabajando con algún proyecto vamos a **File > New > Projecto from Version Control**
  * En la ventana que se abre, seleccionamos **Git** como Version Control
  * Desde el navegador, debemos ir a l repositorio de GitHub que queremos clonar y hacemos click sobre el botón verde [Code].
  * Copiamos la URL que se muestra.
  * Pegamos dicha URL en el campo URL del Control de Versiones de Android.
  * En el campo "Directory" le indicamos la carpeta en la que vamos a almacenar el proyecto clonado.
  * Hacemos click en el boton [Clone]. Con estos pasos, deberías tener una copia del proyecto en tu equipo local.
* En manifest.xml debemos añadir el siguiente código dentro de la opción activity:

  ```
  android:configChanges="orientation|screenSize"
  ```

  Permite ayudar a la gestión del cambio de orientación del movil durante la reproducción del video, de forma que facilite al cambio de horizontalidad del mismo.

* Instalar dependencias necesarias para ejecutar la aplicación
  Las dependencias son un conjunto de librerias que se deben añadir al archivo ***build.gradle.kts (:app)

  - En dependencies
    - com.google.android.material:material:1.12.0: Componentes de diseño moderno de Google
    - androidx.coordinatorlayout:coordinatorlayout:1.2.0: Contenedores para gestionar transiciones y desplazamientos.
    - androidx.drawerlayout:drawerlayout:1.2.0: Permite el uso de un Navigation Drawer.
   ```    // Firebase BoM
      implementation("com.google.android.material:material:1.12.0")
      implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
      implementation("androidx.drawerlayout:drawerlayout:1.2.0")
  
***
### Conclusiones del desarrollador 
Me he dado cuenta de que cualquier proyecto lo convierto en un reto de mejora tanto personal como profesional. Si Pokedex supuso un gran esfuerzo por el cambio en el modelo de paradigma que conlleva el desarrollo de app Android. Cuando comencé con Java, pasar de una programación lineal, modular a OOP ya fue un reto. Android ha supuesto el siguiente paso al unir OOP y Programación Orientada a Eventos. 

De cualquier modo, cada vez me siento más cómodo, ya que aunque tenemos que tener la globalidad del código en nuestra cabeza mientras llevamos a cabo el desarrollo, trabajar con "trozos" considerablemente más pequeños facilita en mucho el desarrollo y la posterior búsqueda de errores.

Corrigiendo errores del anterior proyecto, comencé trabajando profundamente en la modularidad del proyecto, de donde podría obtener más información necesaria, que herramientas podría necesitar, etc. Aún así, nunca te puedes quedar con la primera opción, ya que en la medida que implementas, vas encontrado limitaciones y errores, que te condicionan a tomar otros caminos, añadir o eliminar opciones que pensabas que eran innecesarias o necesarias según el caso.

Tener que hacer uso de la IA como herramienta de busqueda rápida de información, leer y releer material escrito (tanto online como en papel), localizar en foros especializados como se han resuelto determinados problemas con los que me he ido encontrando se han convertido en herramientas de incalculable valor como desarrollador.

Al igual que para el proyecto anterior, el mejor modo (personalmente) que he hallado para ir solventando determinadas técnicas y la implementación de determinadas tecnologías es el de ir creando pequeños proyectos que únicamente se encargan por ejemplo de solucionar el visionado de un video o la gestión de archivos de sonido. De este modo, luego todo se convierte en un "más o menos" copiar y pegar adaptando algunas variables y retoques para integrarlo en el código. Me he percatado que a mí, trabajar así me facilita mucho posteriormente la creación del código que necesito y por otra parte, voy creando una biblioteca de software que podré reutilizar en cualquier momento.

Tal como me pasó en el proyecto anterior (Pokémon), me he dejado en el cajón un montón de ideas que se pueden implementar y que mejoran considerablemente la aplicación:

- Botón de retroceso
- Ajustar mejor la coordinación entre el tiempo de emisión de los sonidos y las animaciones (para que ambos comiencen y terminen al tiempo)
- Añadir más información (almacenada por ejemplo en SharedPreferences) donde se solicite el nombre del jugador y la narración vaya en función de su nombre
- Etc.

Esto es algo que deberé mejorar. Aprender a limitarme a las condiciones que inicialmente se solicitan de la app, ya que tal como me dijo en una ocasión un amigo: "¿Qué hubiera pasado si Microsoft en lugar de empezar con Windows 3.0 hubiera seguido corrigiendo errores y mejorando el sistema?... Que nadie tendría Windows porque seguirían mejorándolo antes de entregarlo. Mejor entrega con errores o posibles mejoras y luego añades, que no entregar nunca". Así que: hasta aquí de momento.

***
### Capturas de pantalla
