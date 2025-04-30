package com.example.languagecompose

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitDragOrCancellation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Data class que representa el estado normalizado del joystick:
 * - orientationX, orientationY: vector de orientación
 * - acceleration: aceleración normalizada (0 a 1)
 */
data class JoystickState(
    val orientationX: Float,
    val orientationY: Float,
    val acceleration: Float
)

/**
 * ViewModel del HUD que mantiene y actualiza los valores normalizados
 * del joystick y la aceleración.
 */
class HudViewModel : ViewModel() {

    private val _isSettingsOpen = MutableLiveData(false)
    val isSettingsOpen: LiveData<Boolean> = _isSettingsOpen

    // LiveData con el estado actual del joystick
    private val _joystickState = MutableLiveData(JoystickState(0f, 0f, 0f))
    val joystickState: LiveData<JoystickState> = _joystickState

    fun updateJoystick(x: Float, y: Float) {
        _joystickState.value = normalizaJoyStick(x, y)
    }

    /**
     * Función que, dada la posición (x, y) del joystick, normaliza el vector
     * de orientación y calcula la aceleración entre 0 y 1.
     */
    private fun normalizaJoyStick(x: Float, y: Float): JoystickState {
        val dist = hypot(x, y)
        if (dist == 0f) {
            // Si no hay desplazamiento, la orientación es (0, 0) y aceleración 0.
            return JoystickState(0f, 0f, 0f)
        }

        // Vector unitario
        val nx = x / dist
        val ny = y / dist

        // La distancia máxima que puede alejarse el joystick
        val maxDist = 150f

        val accel = if (dist >= maxDist) 1f else dist / maxDist

        return JoystickState(nx, ny, accel)
    }
}



/* ──────────────────────────────────────────────────────────────── */
/* HUD : controla gestos y pinta el joystick (solo visual)         */
/* ──────────────────────────────────────────────────────────────── */
@Composable
fun HudView(hudViewModel: HudViewModel, modifier: Modifier = Modifier) {
    /* centro del joystick y desplazamiento del knob (en px) */
    var joyCenter by remember { mutableStateOf<Offset?>(null) }
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    /* dimensiones */
    val joySizeDp = 120.dp
    val knobSizeDp = 50.dp
    val radiusPx = with(LocalDensity.current) { joySizeDp.toPx() / 2f }

    /* capa raíz que captura TODO el gesto */
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    /* solo si el toque está en el tercio inferior */
                    if (down.position.y <= size.height * 2f / 3f) return@awaitEachGesture

                    /* fijamos la posición del joystick */
                    val joyHalf = joySizeDp.toPx() / 2f
                    joyCenter = Offset(down.position.x - joyHalf, down.position.y - joyHalf)
                    knobOffset = Offset.Zero               // knob al centro
                    hudViewModel.updateJoystick(0f, 0f)

                    var prev = down.position
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break

                        /* desplazamiento desde el último frame */
                        val delta = change.position - prev
                        prev = change.position

                        /* actualizamos offset del knob con clamp al radio */
                        val newOffset = knobOffset + delta
                        val dist = hypot(newOffset.x, newOffset.y)
                        val scale = if (dist <= radiusPx) 1f else radiusPx / dist
                        knobOffset = newOffset * scale

                        hudViewModel.updateJoystick(knobOffset.x, knobOffset.y)
                        change.consume()
                    } while (true)

                    /* finger up → centro y desaparecer */
                    joyCenter = null
                    knobOffset = Offset.Zero
                    hudViewModel.updateJoystick(0f, 0f)
                }
            }
    ) {
        /* solo se dibuja si joyCenter ≠ null */
        joyCenter?.let { center ->
            JoyStickVisual(
                modifier = Modifier.offset {
                    IntOffset(center.x.roundToInt(), center.y.roundToInt())
                },
                knobOffset = knobOffset,
                joySize = joySizeDp,
                knobSize = knobSizeDp
            )
        }
    }
}

/* ──────────────────────────────────────────────────────────────── */
/* SOLO VISUAL – sin pointerInput                                  */
/* ──────────────────────────────────────────────────────────────── */
@Composable
private fun JoyStickVisual(
    modifier: Modifier,
    knobOffset: Offset,
    joySize: Dp = 140.dp,
    knobSize: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(joySize),
        contentAlignment = Alignment.Center
    ) {
        /* fondo: gradiente radial + borde blanco */
        Canvas(Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(0.05f),Color.Black.copy(0.1f), Color.Black.copy(0.4f)),
                    radius = r
                ),
                radius = r
            )
            drawCircle(color = Color.White, radius = r, style = Stroke(3.dp.toPx()))
        }

        /* knob desplazado */
        Box(
            modifier = Modifier
                .offset { IntOffset(knobOffset.x.roundToInt(), knobOffset.y.roundToInt()) }
                .size(knobSize)
                .background(Color.White, CircleShape)
        )
    }
}

/* ──────────────────────────────────────────────────────────────── */
/* PREVIEW                                                         */
/* ──────────────────────────────────────────────────────────────── */
@Preview(showBackground = true)
@Composable
fun HudPreview() = HudView(HudViewModel())