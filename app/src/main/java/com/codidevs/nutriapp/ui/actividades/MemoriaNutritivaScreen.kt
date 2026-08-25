package com.codidevs.nutriapp.ui.actividades

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codidevs.nutriapp.data.models.ParMemoria
import com.codidevs.nutriapp.ui.components.BarraProgresoActividad
import com.codidevs.nutriapp.ui.components.DecoracionFondoActividad
import com.codidevs.nutriapp.ui.components.ScreenHeader
import com.codidevs.nutriapp.ui.components.pulsoAnimado
import com.codidevs.nutriapp.ui.theme.*

private data class CartaMemoria(
    val id: Int,
    val emoji: String,
    val texto: String
)

@Composable
fun MemoriaNutritivaScreen(
    pares: List<ParMemoria>,
    onBack: () -> Unit,
    onTerminada: (puntaje: Int) -> Unit
) {
    val cartas = remember(pares) {
        pares.flatMapIndexed { i, par ->
            listOf(
                CartaMemoria(i * 2, par.emoji, par.texto),
                CartaMemoria(i * 2 + 1, par.emoji, par.texto)
            )
        }.shuffled()
    }

    var volteadas by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var seleccionadas by remember { mutableStateOf<List<Int>>(emptyList()) }
    var puntaje by remember { mutableStateOf(0) }
    var bloqueado by remember { mutableStateOf(false) }
    var parejasEncontradas by remember { mutableStateOf(0) }
    var mensajeFeedback by remember { mutableStateOf<String?>(null) }

    fun alTocar(id: Int) {
        if (bloqueado) return
        if (id in volteadas) return
        if (id in seleccionadas) return
        val nueva = seleccionadas + id
        if (nueva.size == 2) {
            bloqueado = true
            val (a, b) = nueva
            if (a / 2 == b / 2) {
                puntaje += 10
                parejasEncontradas++
                seleccionadas = emptyList()
                volteadas = volteadas + a + b
                val par = pares[a / 2]
                mensajeFeedback = if (par.curiosidad.isNotEmpty()) par.curiosidad else "✅ ¡Pareja encontrada! +10 puntos"
                bloqueado = false
            } else {
                seleccionadas = nueva
                mensajeFeedback = "❌ No son iguales"
            }
        } else {
            seleccionadas = nueva
        }
    }

    LaunchedEffect(mensajeFeedback) {
        if (mensajeFeedback != null) {
            kotlinx.coroutines.delay(1200)
            mensajeFeedback = null
        }
    }

    LaunchedEffect(seleccionadas) {
        if (seleccionadas.size == 2) {
            val (a, b) = seleccionadas
            if (a / 2 != b / 2) {
                kotlinx.coroutines.delay(900)
                seleccionadas = emptyList()
                bloqueado = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgApp)) {
        DecoracionFondoActividad()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ScreenHeader(titulo = "Memoria nutritiva", onBack = onBack)

            Spacer(Modifier.height(12.dp))

            BarraProgresoActividad(actual = parejasEncontradas, total = pares.size)

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().height(24.dp), contentAlignment = Alignment.Center) {
                if (mensajeFeedback != null) {
                    Text(
                        text = mensajeFeedback!!,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (mensajeFeedback!!.startsWith("✅")) LeafDark else InkSoft
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cartas.chunked(4).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { carta ->
                            CartaMemoriaView(
                                carta = carta,
                                descubierta = carta.id in volteadas || carta.id in seleccionadas,
                                onClick = { alTocar(carta.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(4 - fila.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            if (parejasEncontradas == pares.size) {
                Button(
                    onClick = com.codidevs.nutriapp.data.audio.onClickConSonido { onTerminada(puntaje) },
                    colors = ButtonDefaults.buttonColors(containerColor = Leaf),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .pulsoAnimado()
                ) {
                    Text("Ver resultados", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            } else {
                Text(
                    text = "⭐ Puntaje: $puntaje",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CartaMemoriaView(
    carta: CartaMemoria,
    descubierta: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (descubierta) LeafLight else Leaf,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, if (descubierta) Leaf else LeafDark),
        modifier = modifier
            .height(68.dp)
            .clickable(enabled = !descubierta, onClick = {
                com.codidevs.nutriapp.data.audio.SoundManager.click()
                onClick()
            })
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (descubierta) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(text = carta.emoji, fontSize = 18.sp)
                    Text(
                        text = carta.texto,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LeafDark,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                }
            } else {
                Text(text = "❓", fontSize = 22.sp, color = Color.White)
            }
        }
    }
}
