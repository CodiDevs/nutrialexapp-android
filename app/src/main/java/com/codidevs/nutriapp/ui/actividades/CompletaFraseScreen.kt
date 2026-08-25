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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codidevs.nutriapp.ui.components.BarraProgresoActividad
import com.codidevs.nutriapp.ui.components.DecoracionFondoActividad
import com.codidevs.nutriapp.ui.components.ScreenHeader
import com.codidevs.nutriapp.ui.components.pulsoAnimado
import com.codidevs.nutriapp.ui.theme.*

data class FraseIncompleta(
    val emoji: String,
    val fraseAntes: String,
    val fraseDespues: String,
    val respuesta: String,
    val opciones: List<String>,
    val curiosidad: String = ""
)

@Composable
fun CompletaFraseScreen(
    frases: List<FraseIncompleta>,
    onBack: () -> Unit,
    onTerminada: (puntaje: Int) -> Unit
) {
    var indice by remember { mutableStateOf(0) }
    var puntaje by remember { mutableStateOf(0) }
    var seleccionada by remember { mutableStateOf<String?>(null) }

    val frase = frases[indice]
    val opcionesBarajadas = remember(indice) { frase.opciones.shuffled() }
    val esCorrecto = seleccionada == frase.respuesta

    Box(modifier = Modifier.fillMaxSize().background(BgApp)) {
        DecoracionFondoActividad()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ScreenHeader(titulo = "Completa la frase", onBack = onBack)

            Spacer(Modifier.height(12.dp))

            BarraProgresoActividad(actual = indice + 1, total = frases.size)

            Spacer(Modifier.height(20.dp))

            // Tarjeta Premium con el emoji en burbuja
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(3.dp, if (seleccionada != null) (if (esCorrecto) Leaf else Berry) else LineColor),
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
                        modifier = Modifier.size(90.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = frase.emoji, fontSize = 54.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(20.dp))

                    val blancoColor = when {
                        seleccionada == null -> InkSoft
                        esCorrecto -> LeafDark
                        else -> Berry
                    }
                    val blancoTexto = seleccionada ?: "_______"

                    val anotado = buildAnnotatedString {
                        withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.Bold)) {
                            append("${frase.fraseAntes} ")
                        }
                        withStyle(SpanStyle(color = blancoColor, fontWeight = FontWeight.ExtraBold, textDecoration = TextDecoration.Underline)) {
                            append(blancoTexto)
                        }
                        withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.Bold)) {
                            append(" ${frase.fraseDespues}")
                        }
                    }
                    Text(text = anotado, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, lineHeight = 30.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            if (seleccionada == null) {
                Text(text = "Toca la palabra correcta:", style = MaterialTheme.typography.titleMedium, color = Ink, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    opcionesBarajadas.forEach { opcion ->
                        OutlinedButton(
                            onClick = com.codidevs.nutriapp.data.audio.onClickConSonido {
                                seleccionada = opcion
                                if (opcion == frase.respuesta) puntaje += 10
                            },
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = MangoDark),
                            border = BorderStroke(2.dp, Mango),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(4.dp, RoundedCornerShape(20.dp))
                                .pulsoAnimado()
                        ) {
                            Text(text = opcion, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            } else {
                Surface(
                    color = if (esCorrecto) LeafLight else BerryLight,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, if (esCorrecto) Leaf else Berry),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (esCorrecto) "✅ ¡Excelente! +10 puntos" else "❌ La respuesta era: \"${frase.respuesta}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (esCorrecto) LeafDark else Berry,
                            textAlign = TextAlign.Center
                        )
                        if (esCorrecto && frase.curiosidad.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = frase.curiosidad,
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
                        if (indice + 1 >= frases.size) onTerminada(puntaje) else { indice++; seleccionada = null }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Leaf),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .pulsoAnimado()
                ) {
                    Text(text = if (indice + 1 >= frases.size) "Ver resultados" else "Siguiente frase →", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
