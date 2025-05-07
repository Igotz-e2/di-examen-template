package com.example.languagecompose



import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.hypot


enum class CharacterState {
    IDLE,
    MOVING
}

class CharacterViewModel(
    startState: CharacterState = CharacterState.IDLE
) : ViewModel() {

    private val _currentState : MutableStateFlow<CharacterState> = MutableStateFlow(startState)
    val currentState = _currentState.asStateFlow()

    private val _characterX = MutableLiveData(170f)
    val characterX: LiveData<Float> = _characterX

    private val _characterY = MutableLiveData(500f)
    val characterY: LiveData<Float> = _characterY

    // Velocidad del personaje en cada eje
    private var velocityX = 0f
    private var velocityY = 0f

    // Parámetros de la física
    private val maxSpeed = 10f         // Velocidad máxima
    private val friction = 0.93f      // Factor de rozamiento en [0..1]
    private val accelFactor = 1.2f    // Multiplicador de la aceleración

    // Tamaño "lógico" del personaje
    val characterDiameter = 25f


    private var joystickState = JoystickState(0f, 0f, 0f)

    /**
     * Llamar a esta función para que el CharacterViewModel
     * empiece a observar el LiveData del joystick y aplicar
     * los cambios de forma continua.
     *
     * @param joystickLiveData LiveData<JoystickState> proveniente del HudViewModel
     */

    fun startCollectingJoystick(joystickLiveData: LiveData<JoystickState>) {

        // 1) Observar los cambios del joystick
        // 2) Iniciar la simulación con un "game loop"

        // Observamos el LiveData (puedes usar Flow o StateFlow si lo prefieres)
        joystickLiveData.observeForever {
            joystickState = it
        }

        // Lanzamos una corrutina que actualiza la física ~60 veces por segundo
        viewModelScope.launch {
            while (true) {
                // Aproximación: 16 ms por frame => 60 fps
                // (Si quieres más precisión, usa measure de tiempo real.)
                delay(16)

                // Aceleración = JoystickState.orientation * JoystickState.acceleration
                // multiplied by accelFactor
                val accelX = joystickState.orientationX * joystickState.acceleration * accelFactor
                val accelY = joystickState.orientationY * joystickState.acceleration * accelFactor

                // Actualizar velocidad con la aceleración
                velocityX += accelX
                velocityY += accelY

                // Aplicar rozamiento multiplicando la velocidad
                velocityX *= friction
                velocityY *= friction

                // Limitar velocidad máxima
                var speed = hypot(velocityX, velocityY)

                if (speed > maxSpeed) {
                    // Reescalar la velocidad hasta maxSpeed
                    val ratio = maxSpeed / speed
                    velocityX *= ratio
                    velocityY *= ratio

                    speed = maxSpeed
                }

                // Forzar la velocidad a 0 si es muy pequeña (para que pare del todo)
                if (speed < 0.2f) {
                    velocityX = 0f
                    velocityY = 0f
                    speed = 0f
                }

                // Actualizar posición => necesitamos el valor actual:
                val oldX = _characterX.value ?: 0f
                val oldY = _characterY.value ?: 0f

                // Nueva posición
                val newX = oldX + velocityX
                val newY = oldY + velocityY

                // Llamamos a la función para corregir la colisión (o clamp).
                val (finalX, finalY) = checkCollisionWithMaze(
                    oldX = oldX,
                    oldY = oldY,
                    targetX = newX,
                    targetY = newY,
                    diameter = characterDiameter*0.8f
                )


                _characterX.postValue(finalX)
                _characterY.postValue(finalY)

                // Actualizar estado (IDLE si casi no hay movimiento)
                _currentState.value = if (speed == 0f) CharacterState.IDLE else CharacterState.MOVING
            }
        }

    }
}

@Composable
@Preview
fun CharacterPreview() {
    CharacterView(CharacterViewModel(), 1f)
}

@Composable
fun CharacterView(characterViewModel: CharacterViewModel, scale: Float) {
    // Observamos coordenadas y estado actual
    val x by characterViewModel.characterX.observeAsState(170f)
    val y by characterViewModel.characterY.observeAsState(500f)
    val state by characterViewModel.currentState.collectAsState()

    // Radio del personaje
    val logicalRadius = 12.5f // 25 / 2
    val scaledRadiusDp = (logicalRadius * scale).dp

    // Offset visual (ajustamos para que se dibuje centrado)
    val offsetX = (x * scale).dp - scaledRadiusDp
    val offsetY = (y * scale).dp - scaledRadiusDp

    // Color dinámico según el estado
    val color = when (state) {
        CharacterState.IDLE -> Color.Blue
        CharacterState.MOVING -> Color.Red
    }
    val stateText = when (state) {
        CharacterState.IDLE -> stringResource(id = R.string.status_idle)
        CharacterState.MOVING -> stringResource(id = R.string.status_moving)
    }
    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .offset(x = offsetX, y = offsetY)
                .size((25 * scale).dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = color,
                    radius = size.minDimension / 2
                )
            }
        }

        Text(
            text = "${stringResource(R.string.status_label)} $stateText",
            modifier = Modifier
                .fillMaxHeight(0.1f)
                .align(Alignment.BottomEnd)
                .height(60.dp)
                .padding(20.dp),
            color = Color.Black
        )
    }
}
