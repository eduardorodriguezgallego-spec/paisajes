Aplicación desarrollada en Kotlin con Android Studio Panda 2.

Estructura

La app tiene dos ejercicios principales:

Ejercicio 1: Valoración de paisajes
Ejercicio 2: Descarga de imágenes

Ejercicio 1: Paisajes

Funcionalidad
Animación del texto “Valoración de paisajes”
Uso de ViewFlipper para mostrar imágenes
Navegación con gestos (swipe)
Campo de valoración de 1 a 10
Botón para guardar la nota
Sonido al cambiar de imagen

Mejoras
Cada imagen guarda su propia valoración
Se recupera al volver a la imagen
Interfaz mejorada con Material Design

Ejercicio 2: Imágenes

Funcionalidad
Descarga de un fichero imagenes.txt
Lectura de URLs
Descarga de imágenes con Coil
Visualización con ViewFlipper
Navegación por gestos
Animaciones y sonido

Gestión de errores
Los errores se guardan en un archivo errores.txt con:

URL
Fecha y hora
Causa del error

Ejemplo:

https://ejemplo.com/noexiste.jpg | 2026-04-20 20:10:12 | Error HTTP 404
Ubicación del archivo errores.txt
Android/data/[nombre.paquete]/files/errores.txt

Tecnologías
Kotlin
Android SDK
ViewBinding
ViewFlipper
Coil
MediaPlayer
Animaciones XML

