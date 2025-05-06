
<div style="display: flex; justify-content: space-between;">
  <img src="doc/txur.png" alt="alt text" style="width: 20%;">
  <img src="doc/eusko.png" alt="alt text" style="width: 48%;">
</div>

# Examen DI - Eval 2 - 8/5/2025

- [Examen DI - Eval 2 - 8/5/2025](#examen-di---eval-2---852025)
  - [Introducción](#introducción)
  - [Objetivo de la prueba](#objetivo-de-la-prueba)
  - [The boring ball](#the-boring-ball)
    - [Configuralo - `1.5 p`](#configuralo---15-p)
    - [Configuralo Más - `1.5 p`](#configuralo-más---15-p)
    - [Calentamiento por velocidad – `2 p`](#calentamiento-por-velocidad-2p)
    - [Dash - `2 p`](#dash---2-p)
    - [Eternauta - `3 p`](#eternauta---3-p)
    - [Saltan Chispas - `3.5 p`](#saltan-chispas---35-p)
    - [El bug en la pared `0.5p`](#el-bug-en-la-pared-05p)
  - [Guía básica del código](#guía-básica-del-código)
    - [Level](#level)
    - [Hud](#hud)
    - [Personaje](#personaje)
    - [Settings](#settings)
    - [Esquema completo](#esquema-completo)

## Introducción


El campo de juego de este examen será un minijuego, **The boring ball**, con una pelota que se mueve dentro de un rectángulo, controlada por un joystick virtual. El código inicial ya impide salir de los límites y aplica una ligera aceleración.

<img src="doc/hud.png" alt="alt text" style="width: 25%;">


## Objetivo de la prueba

Evaluar tu dominio de **Kotlin + Jetpack Compose** y de los componentes propios de Android, así como tu capacidad de buscar solucciónes, organización, refactorización del código.

> 🔔 **Entrega**: Sube tu el código final a GitHub classroom en este repositorio antes de la hora de finalización de la prueba.

## The boring ball

### Configuralo - `1.5 p`

 * Agrega un boton con icono de tuerca en la parte superior izquierda de la pantalla.
 * Al pulsar se ha de abrir la vista de configuracion.
 * Centra la vista de configuracion en la pantalla, pero que no la cubra del todo y que se pueda ver el fondo ligeramente.
 * Agrega un botón para cerrar la vista de configuracion.


### Configuralo Más - `1.5 p`

 * En el menu de configuracion añade un slider que permita cambiar el color de la pelota.
 * Los colores han de variar en tono y estar equiespaciados entre ellos.
 * Agrega otro slider que permita cambiar el tamaño de la pelota entre la mitad de su tamaño y el doble de su tamaño.

> ❗ Usa hsv para el color y varia el hue entre 0 y 360.
 
### Calentamiento por velocidad – `2 p`

 * La pelota cambia de color al ir más rápido.
 * Mapea la velocidad ‑› color, usando `lerp()` entre dos tonos. 
 * Uno de los tonos ha de ser el color de la pelota, que es el color cuando esta no tiene velocidad y el otro ha de ser su color análogo.

### Dash - `2 p`

 * Crea un botón flotante abajo a la izquierda con un icono de flecha como  `>>`.
 * Al pulsar, la pelota aumenta su *aceleración* durante 500 milisegundos.
 * El dash ha de ser claramente perceptible, pero no excesivo.

> ❗ Tal vez tengas que reducir el espacio en el que se puede usar el joystick ya que este ocupa toda la parte de abajo.
 
### Eternauta - `3 p`

* Crea un botón flotante abajo a la izquierda con un icono de flecha como `#`.
* Al pulsarlo, la pelota se vuelve etérea durante 1000 milisegundos. 
* La pelota ha de transparentarse un 80% durante el tiempo que dure el efecto, y ha de volver a su color original al finalizar el efecto.
* La pelota ha de ser capaz de atravesar las paredes durante el tiempo que dure el efecto.
* El efecto ha de ser claramente perceptible, pero no excesivo.

### Saltan Chispas - `3.5 p`


### El bug en la pared `0.5p`

Si eres un poco observador, te habrás fijado que cuando te acercas con velocidad a una pared la pelota se frena frente a esta y luego termina de pegarse.

* Corrije el bug.

  
## Guía básica del código

La estructura del código sigue el modelo de árbol de vistas de Jetpack Compose. La vista principal está `Level` y el resto de vistas son funciones que se llaman desde esta.

![](doc/Examen%20di_page-0005.jpg)
 
`MainActivity` incia el contexto de la activity y lo único que hace es llamar a la la función Level que es la que contiene el juego. 

A su vez, `Level` inicia los componentes inferiories, define sus estados inciales empezando así los subprogramas. Componentes como HUD, Character o Settings.

A nivel de architectura, hay cuatro viewModels que se encargan de gestionar el estado de la vista. `LevelViewModel`, `HudViewModel`, `CharacterViewModel` y `SettingsViewModel`. Cada uno de ellos tiene su propio estado y se encarga de gestionar los eventos que ocurren en su vista.

### Level
![](doc/Examen%20di_page-0003.jpg)

### Hud 
![](doc/Examen%20di_page-000.jpg)
![alt text](doc/hud1.png)

### Personaje
![](doc/Examen%20di_page-0001.jpg)

### Settings

![](doc/Examen%20di_page-0002.jpg)

### Esquema completo
![](doc/Examen%20di%20(1)_page-0001.jpg)
