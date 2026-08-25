package com.codidevs.nutriapp.ui.actividades

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.codidevs.nutriapp.data.models.GrupoAlimenticio
import com.codidevs.nutriapp.ui.components.AlimentoFigura
import com.codidevs.nutriapp.ui.components.BarraProgresoActividad
import com.codidevs.nutriapp.ui.components.DecoracionFondoActividad
import com.codidevs.nutriapp.ui.components.ScreenHeader
import com.codidevs.nutriapp.ui.components.pulsoAnimado
import com.codidevs.nutriapp.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun GrupoPerteneceScreen(
    grupos: List<GrupoAlimenticio>,
    onBack: () -> Unit,
    onTerminada: (puntaje: Int) -> Unit
) {
    val rondas = remember(grupos) {
        val alimentos = grupos.flatMap { g -> g.alimentos.map { a -> a to g.nombre } }
            .shuffled()
            .take(6)
        alimentos.map { (alimento, grupoCorrecto) ->
            val correcto = grupos.first { it.nombre == grupoCorrecto }
            val otros = grupos.filter { it.nombre != grupoCorrecto }.shuffled().take(3)
            Triple(alimento, correcto, (otros + correcto).shuffled())
        }
    }

    var indice by remember { mutableStateOf(0) }
    var puntaje by remember { mutableStateOf(0) }
    var retro by remember { mutableStateOf<String?>(null) }
    var grupoResultado by remember { mutableStateOf<Pair<String, String>?>(null) }

    var posBoxRaiz by remember { mutableStateOf(Offset.Zero) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var inicioDrag by remember { mutableStateOf<Offset?>(null) }
    var ultimoPunto by remember { mutableStateOf<Offset?>(null) }
    var arrastrando by remember { mutableStateOf(false) }
    var rectTarjeta by remember { mutableStateOf(Rect.Zero) }
    var zonaSobre by remember { mutableStateOf<String?>(null) }
    val posicionesGrupo = remember(indice) { mutableMapOf<String, Rect>() }

    val ronda = rondas[indice]
    val alimento = ronda.first
    val gruposRonda = ronda.third

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgApp)
            .onGloballyPositioned { posBoxRaiz = it.positionInRoot() }
            .pointerInput(alimento, indice, posBoxRaiz) {
                detectDragGestures(
                    onDragStart = { pos ->
                        if (retro == null) {
                            val puntoGlobal = posBoxRaiz + pos
                            if (rectTarjeta.contains(puntoGlobal)) {
                                inicioDrag = puntoGlobal
                                ultimoPunto = puntoGlobal
                                arrastrando = true
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        if (arrastrando) {
                            change.consume()
                            val puntoGlobal = posBoxRaiz + change.position
                            dragOffset = puntoGlobal - (inicioDrag ?: puntoGlobal)
                            ultimoPunto = puntoGlobal
                            zonaSobre = grupoMasCercano(puntoGlobal, posicionesGrupo)
                        }
                    },
                    onDragEnd = {
                        if (arrastrando) {
                            val punto = ultimoPunto ?: (inicioDrag ?: Offset.Zero)
                            val grupoSoltado = grupoMasCercano(punto, posicionesGrupo)
                            when (grupoSoltado) {
                                null -> {
                                    dragOffset = Offset.Zero
                                    grupoResultado = null
                                }
                                ronda.second.nombre -> {
                                    puntaje += 10
                                    retro = "correcto"
                                    dragOffset = punto - (inicioDrag ?: punto)
                                    grupoResultado = grupoSoltado to "correcto"
                                }
                                else -> {
                                    retro = "incorrecto"
                                    dragOffset = punto - (inicioDrag ?: punto)
                                    grupoResultado = grupoSoltado to "incorrecto"
                                }
                            }
                        }
                        arrastrando = false
                        inicioDrag = null
                        ultimoPunto = null
                        zonaSobre = null
                    }
                )
            }
    ) {
        DecoracionFondoActividad()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            ScreenHeader(titulo = "¿A qué grupo pertenece?", onBack = onBack)

            Spacer(Modifier.height(12.dp))

            BarraProgresoActividad(actual = indice + 1, total = rondas.size)

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Arrastra el alimento hasta su grupo",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, if (retro == null) Leaf else LineColor),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .size(96.dp)
                        .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                        .onGloballyPositioned { rectTarjeta = it.boundsInRoot() }
                        .pulsoAnimado(enabled = retro == null)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AlimentoFigura(nombre = alimento.nombre, emoji = alimento.emoji, tamano = 40)
                        Text(text = alimento.nombre, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = InkSoft)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                gruposRonda.chunked(2).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        fila.forEach { grupo ->
                            ZonaGrupo(
                                grupo = grupo,
                                resaltada = zonaSobre == grupo.nombre,
                                resultado = grupoResultado?.takeIf { it.first == grupo.nombre }?.second,
                                modifier = Modifier.weight(1f),
                                onPosicion = { nombre, rect -> posicionesGrupo[nombre] = rect }
                            )
                        }
                        if (fila.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            retro?.let { estado ->
                Surface(
                    color = if (estado == "correcto") LeafLight else BerryLight,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (estado == "correcto")
                                "✅ ¡Correcto! +10 puntos"
                            else
                                "❌ Casi. ${alimento.emoji} ${alimento.nombre} es de ${ronda.second.emoji} ${ronda.second.nombre}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (estado == "correcto") LeafDark else Berry,
                            textAlign = TextAlign.Center
                        )
                        if (estado == "correcto" && alimento.curiosidad.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = alimento.curiosidad,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = InkSoft,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = com.codidevs.nutriapp.data.audio.onClickConSonido {
                        if (indice + 1 >= rondas.size) {
                            onTerminada(puntaje)
                        } else {
                            indice++
                            retro = null
                            grupoResultado = null
                            dragOffset = Offset.Zero
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Leaf),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp).pulsoAnimado()
                ) {
                    Text(
                        text = if (indice + 1 >= rondas.size) "Ver resultados" else "Siguiente →",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ZonaGrupo(
    grupo: GrupoAlimenticio,
    resaltada: Boolean,
    resultado: String?,
    modifier: Modifier = Modifier,
    onPosicion: (String, Rect) -> Unit
) {
    Surface(
        color = when (resultado) {
            "correcto" -> LeafLight
            "incorrecto" -> BerryLight
            else -> if (resaltada) LeafLight else Color.White
        },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (resaltada || resultado != null) 3.dp else 2.dp, when (resultado) {
            "correcto" -> Leaf
            "incorrecto" -> Berry
            else -> if (resaltada) Mango else LineColor
        }),
        modifier = modifier
            .height(96.dp)
            .onGloballyPositioned { coords -> onPosicion(grupo.nombre, coords.boundsInRoot()) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = grupo.emoji, fontSize = 30.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = grupo.nombre,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (resultado != null) (if (resultado == "correcto") LeafDark else Berry) else (if (resaltada) LeafDark else InkSoft),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun grupoMasCercano(punto: Offset, posiciones: Map<String, Rect>): String? {
    val mejor = posiciones.entries
        .map { (nombre, rect) ->
            val cx = rect.left + rect.width / 2
            val cy = rect.top + rect.height / 2
            val dist = kotlin.math.sqrt((punto.x - cx) * (punto.x - cx) + (punto.y - cy) * (punto.y - cy))
            nombre to dist
        }
        .minByOrNull { it.second } ?: return null
    return if (mejor.second < 400f) mejor.first else null
}
