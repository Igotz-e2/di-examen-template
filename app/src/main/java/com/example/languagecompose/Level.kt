package com.example.languagecompose

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Dimensiones lógicas de la escena
object LevelConstants {
    var sceneLogicalWidth = MazeConfig.MAZE_COLS * MazeConfig.TILE_SIZE
    var sceneLogicalHeight = MazeConfig.MAZE_ROWS * MazeConfig.TILE_SIZE

    val sceneWidthDp = sceneLogicalWidth.dp
    val sceneHeightDp = sceneLogicalHeight.dp
}

object MazeConfig {
    const val MAZE_COLS = 20  // Ancho
    const val MAZE_ROWS = 36  // Alto
    const val TILE_SIZE = 25f // Tamaño lógico de cada celda

    // Ejemplo de matriz 45 filas x 20 columnas
    // Solo se muestran las primeras filas como ejemplo;
    // tú deberás completarlo a 45. El borde es 1 para que haya paredes.
    val mazeData = Array(MAZE_ROWS) { row ->
        Array(MAZE_COLS) { col ->
            when {
                // Borde
                row == 0 || row == MAZE_ROWS - 1 ||
                        col == 0 || col == MAZE_COLS - 1 -> 1

                // Un par de paredes verticales internas de ejemplo
                // (ajusta a tu gusto)
                (col == 5 && row in 5..10) -> 1
                (col == 10 && row in 20..30) -> 1

                else -> 0
            }
        }
    }
}


class LevelViewModel(
) : ViewModel() {

    private val _scale = MutableLiveData<Float>(0f)
    val scale: LiveData<Float> = _scale

    fun setScale(containerWidthDp: Dp, containerHeightDp: Dp) {
        // Calcular el factor de escala para mantener la escena completa visible
        _scale.value =
            minOf(containerWidthDp / LevelConstants.sceneWidthDp, containerHeightDp / LevelConstants.sceneHeightDp) * 0.9f
    }
}


@Composable
@Preview
fun LevelPreview() {
    Level()
}

@Composable
fun Level(){

    // Inicializar el viewModel del mapa
    val levelVM = LevelViewModel()

    // Inicializar el viewModel de los controles
    val hudVM: HudViewModel = HudViewModel()

    // Inicializar el viewModel del personaje
    val characterVM: CharacterViewModel = CharacterViewModel(CharacterState.IDLE)

    // Inicializar el viewModel de las configuraciones
    val settingsVM: SettingsViewModel = SettingsViewModel(LocalContext.current)

    val scale by levelVM.scale.observeAsState(1f)

    FondoGradienteRadial(color = Color(0xFF87CEEB))

    // Medir el tamaño real del contenedor donde se dibujará la escena
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val containerWidthDp = this.maxWidth
        val containerHeightDp = this.maxHeight

        // Calculamos la escala tan pronto sepamos las dimensiones
        levelVM.setScale(containerWidthDp, containerHeightDp)

        Log.d("Level", "Scale: $scale")
        Log.d("Level", "Container width: ${containerWidthDp}}")
        Log.d("Level", "Container height: ${containerHeightDp}")

        // Contenedor principal a tamaño completo
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center  // Centramos el contenido escalado
        ) {
            // Esta Box se dibuja con el tamaño escalado de nuestra escena lógica
            Box(
                modifier = Modifier
                    .size(LevelConstants.sceneWidthDp * scale, LevelConstants.sceneHeightDp * scale)
                    .offset(x = 0.dp, y = -((containerHeightDp - LevelConstants.sceneHeightDp * scale) / 2) * 0.3f)
                    .border(3.dp, Color.Red)
                    .background(Color.hsv(0f, 0f, 1f, 0.3f))
                    .graphicsLayer {
                        // Opcional: rotaciones, traslaciones, etc.
                    }
            ) {
                // Dibuja tu mapa “dentro” de este tamaño escalado
                MazeWallsView(MazeConfig.mazeData)
                // Dibuja el personaje en su posición escalada
                CharacterView(characterVM, scale)
            }

            // HUD (joystick) se superpone en pantalla completa,
            // normalmente sin “scale”, porque es un UI overlay.
            HudView(hudVM)
        }

    }

    // Inicializamos la recolección del joystick (lo ideal es hacerlo solo una vez).
    LaunchedEffect(Unit) {
        // Iniciamos el loop de actualización del personaje pasandole el livedata del joystick
        characterVM.startCollectingJoystick(hudVM.joystickState)
    }

}

@Composable
fun MazeWallsView(
    mazeData: Array<Array<Int>>
) {
    val rows = mazeData.size
    val cols = mazeData[0].size

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // El tamaño total disponible en este canvas
            val cellWidthPx = size.width  / cols
            val cellHeightPx = size.height / rows

            // Dibujamos cada celda de la matriz
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    if (mazeData[row][col] == 1) {
                        val left = col * cellWidthPx
                        val top  = row * cellHeightPx

                        drawRect(
                            color = Color.DarkGray,
                            topLeft = androidx.compose.ui.geometry.Offset(left, top),
                            size    = androidx.compose.ui.geometry.Size(cellWidthPx, cellHeightPx)
                        )
                    }
                }
            }
        }
    }
}




/**
 * Intenta mover la bola en X, comprueba colisión, ajusta X si hace falta.
 * Luego intenta mover la bola en Y, comprueba colisión, ajusta Y si hace falta.
 *
 * @param oldX Posición anterior en X
 * @param oldY Posición anterior en Y
 * @param targetX Posición deseada en X tras sumar la velocidad
 * @param targetY Posición deseada en Y
 * @param diameter Diámetro de la bola
 *
 * @return Pair(finalX, finalY) con la posición corregida.
 */
fun checkCollisionWithMaze(
    oldX: Float,
    oldY: Float,
    targetX: Float,
    targetY: Float,
    diameter: Float
): Pair<Float, Float> {
    val radius = diameter / 2f

    // 1) Primero movemos en X, dejando Y igual.
    //   - Comprobamos si colisiona con muros. Si colisiona, revertimos X.
    val attemptX = targetX
    val attemptY = oldY  // nos quedamos con la Y anterior
    val collideX = collidesWithMaze(attemptX, attemptY, radius)
    val finalX = if (collideX) oldX else attemptX

    // 2) Después movemos en Y, dejando X como haya quedado.
    //   - Si colisiona, revertimos Y (mantenemos la Y anterior).
    val attemptY2 = targetY
    val collideY = collidesWithMaze(finalX, attemptY2, radius)
    val finalY = if (collideY) oldY else attemptY2

    return finalX to finalY
}


/**
 * Devuelve true si la bola centrada en (centerX, centerY) con
 * cierto [radius] choca con alguna celda de MazeConfig que sea 1.
 */
fun collidesWithMaze(centerX: Float, centerY: Float, radius: Float): Boolean {
    // Bounding box de la bola
    val left = centerX - radius
    val right = centerX + radius
    val top = centerY - radius
    val bottom = centerY + radius

    // Determinamos qué celdas del laberinto chequeamos
    val minCol = (left / MazeConfig.TILE_SIZE).toInt().coerceIn(0, MazeConfig.MAZE_COLS - 1)
    val maxCol = (right / MazeConfig.TILE_SIZE).toInt().coerceIn(0, MazeConfig.MAZE_COLS - 1)
    val minRow = (top / MazeConfig.TILE_SIZE).toInt().coerceIn(0, MazeConfig.MAZE_ROWS - 1)
    val maxRow = (bottom / MazeConfig.TILE_SIZE).toInt().coerceIn(0, MazeConfig.MAZE_ROWS - 1)

    for (row in minRow..maxRow) {
        for (col in minCol..maxCol) {
            if (MazeConfig.mazeData[row][col] == 1) {
                // bounding box de la celda
                val tileLeft = col * MazeConfig.TILE_SIZE
                val tileRight = tileLeft + MazeConfig.TILE_SIZE
                val tileTop = row * MazeConfig.TILE_SIZE
                val tileBottom = tileTop + MazeConfig.TILE_SIZE

                val overlapHoriz = (right > tileLeft) && (left < tileRight)
                val overlapVert = (bottom > tileTop) && (top < tileBottom)

                if (overlapHoriz && overlapVert) {
                    return true  // colisión detectada
                }
            }
        }
    }
    return false
}


val BlancoAmarillento = Color(0xFFEFEFEF)

@Composable
fun FondoGradienteRadial(color: Color) {
    // Gradiente radial grande para el fondo
    val largeRadialGradient = remember(color) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                val biggerDimension = maxOf(size.height, size.width)
                return RadialGradientShader(
                    colors = listOf(BlancoAmarillento, color),
                    center = size.center,
                    radius = biggerDimension / 2f,
                    colorStops = listOf(0f, 0.95f)
                )
            }
        }
    }

    // Box vacía que ocupa toda la pantalla con el fondo degradado
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(largeRadialGradient),
        contentAlignment = Alignment.Center,
    ) {
    }
}