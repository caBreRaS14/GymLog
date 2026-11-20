# GymLog - Registro de ejercicio

App Android (Kotlin, Jetpack Compose, Room, arquitectura MVVM) para registrar entrenamientos de gimnasio,
ver la carga semanal, la racha, metas y la evolucion de cada ejercicio. Funciona sin internet.

## Como ejecutarla
1. Abrir esta carpeta en Android Studio (File > Open) y esperar el Gradle Sync.
2. Conectar un celular Android con depuracion USB activa (o crear un emulador).
3. Pulsar Run. Requiere minSdk 24.

En el primer arranque se cargan 213 registros de ejemplo (8 semanas de rutina Push/Pull/Legs).

## Estructura
- data/: entidad Registro, RegistroDao, AppDb (Room), RegistroRepository
- ui/: RegistroViewModel, pantallas (Inicio, Historial, Progreso/Detalle, Descanso), componentes y tema
- MainActivity.kt: navegacion (NavHost) y barra inferior
