package com.codidevs.nutriapp.ui.actividades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codidevs.nutriapp.data.models.PreguntaVF
import com.codidevs.nutriapp.ui.components.BarraProgresoActividad
import com.codidevs.nutriapp.ui.components.DecoracionFondoActividad
import com.codidevs.nutriapp.ui.components.ScreenHeader
import com.codidevs.nutriapp.ui.components.pulsoAnimado
import com.codidevs.nutriapp.ui.theme.*

@Composable
fun VerdaderoFalsoScreen(
    preguntas: List<PreguntaVF>,
    onBack: () -> Unit,
    onTerminada: (puntaje: Int) -> Unit
) {
    var indice by remember { mutableStateOf(0) }
    var puntaje by remember { mutableStateOf(0) }
    var respuesta by remember { mutableStateOf<Boolean?>(null) }

    val pregunta = preguntas[indice]
    val respondio = respuesta != null
    val esCorrecto = respuesta == pregunta.esVerdadero

    Box(modifier = Modifier.fillMaxSize().background(BgApp)) {
        DecoracionFondoActividad()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ScreenHeader(titulo = "Verdadero o falso", onBack = onBack)

            Spacer(Modifier.height(12.dp))

            BarraProgresoActividad(actual = indice + 1, total = preguntas.size)

            Spacer(Modifier.height(20.dp))

            // Tarjeta Premium con el enunciado
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(3.dp, if (respondio) (if (esCorrecto) Leaf else Berry) else LineColor),
                shadowElevation = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        color = BgApp,
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = pregunta.emoji, fontSize = 60.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))
                    
                    Text(
                        text = pregunta.enunciado,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = com.codidevs.nutriapp.data.audio.onClickConSonido {
                        respuesta = true
                        if (pregunta.esVerdadero) puntaje += 10
                    },
                    enabled = !respondio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Leaf,
                        disabledContainerColor = Leaf.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .shadow(if (!respondio) 6.dp else 0.dp, RoundedCornerShape(20.dp))
                        .pulsoAnimado(enabled = !respondio)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✓", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("VERDADERO", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Button(
                    onClick = com.codidevs.nutriapp.data.audio.onClickConSonido {
                        respuesta = false
                        if (!pregunta.esVerdadero) puntaje += 10
                    },
                    enabled = !respondio,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Berry,
                        disabledContainerColor = Berry.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .shadow(if (!respondio) 6.dp else 0.dp, RoundedCornerShape(20.dp))
                        .pulsoAnimado(enabled = !respondio)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✖", fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("FALSO", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (respondio) {
                Surface(
                    color = if (esCorrecto) LeafLight else BerryLight,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, if (esCorrecto) Leaf else Berry),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (esCorrecto) "✅ ¡Genial! +10 puntos" else "❌ La respuesta era ${if (pregunta.esVerdadero) "Verdadero" else "Falso"}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (esCorrecto) LeafDark else Berry,
                            textAlign = TextAlign.Center
                        )
                        if (esCorrecto && pregunta.curiosidad.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = pregunta.curiosidad,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = InkSoft,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = com.codidevs.nutriapp.data.audio.onClickConSonido {
                        if (indice + 1 >= preguntas.size) onTerminada(puntaje) else { indice++; respuesta = null }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Leaf),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .pulsoAnimado()
                ) {
                    Text(text = if (indice + 1 >= preguntas.size) "Ver resultados" else "Siguiente pregunta →", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
