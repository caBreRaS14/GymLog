# GymLog - Registro de ejercicio

App Android (Kotlin, Jetpack Compose, Room, arquitectura MVVM) para registrar entrenamientos de gimnasio,
ver la carga semanal, la racha, metas y la evolucion de cada ejercicio. Funciona sin internet.

## Como ejecutarla
1. Abrir esta carpeta en Android Studio (File > Open) y esperar el Gradle Sync.
2. Conectar un celular Android con depuracion USB activa (o crear un emulador).
3. Pulsar Run. Requiere minSdk 24.

En el primer arranque se cargan 213 registros de ejemplo (8 semanas de rutina Push/Pull/Legs).

## Instalar sin Android Studio (APK)
El APK de la version 1.0 esta en `releases/GymLog-v1.0-debug.apk` (compilacion de depuracion, Android 7.0 o superior).
1. Copiarlo al celular (o descargarlo desde GitHub).
2. Abrirlo y permitir la instalacion desde esa fuente cuando Android lo pida.
3. Tambien por USB con depuracion activa: `adb install -r releases/GymLog-v1.0-debug.apk`

## Estructura
- data/: entidad Registro, RegistroDao, AppDb (Room), RegistroRepository
- ui/: RegistroViewModel, pantallas (Inicio, Historial, Progreso/Detalle, Descanso), componentes y tema
- MainActivity.kt: navegacion (NavHost) y barra inferior
