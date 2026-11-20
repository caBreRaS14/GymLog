package co.fexe.gymlog.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Fondo = Color(0xFF0D0F12)
val Superficie = Color(0xFF171A20)
val SuperficieAlta = Color(0xFF222731)
val Lima = Color(0xFFC6FF3D)
val TextoSuave = Color(0xFF9AA3B2)

fun colorGrupo(g: String) = when (g) {
    "Pecho" -> Color(0xFFFF6B35)
    "Espalda" -> Color(0xFF4DA3FF)
    "Pierna" -> Color(0xFFB36BFF)
    "Hombro" -> Color(0xFFFFC53D)
    "Brazo" -> Color(0xFFFF5C8A)
    else -> Color(0xFF2EE6A8) // Core
}

private val Esquema = darkColorScheme(
    primary = Lima, onPrimary = Color(0xFF10140A),
    background = Fondo, onBackground = Color(0xFFF2F4F8),
    surface = Superficie, onSurface = Color(0xFFF2F4F8),
    surfaceVariant = SuperficieAlta, onSurfaceVariant = TextoSuave,
    secondaryContainer = Color(0xFF2F3B14), onSecondaryContainer = Lima,
    error = Color(0xFFFF6B6B), outline = Color(0xFF3A4150)
)

private val Tipo = Typography(
    headlineLarge = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelLarge = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
)

@Composable
fun GymTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = Esquema, typography = Tipo, content = content)