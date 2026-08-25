package com.codidevs.nutriapp.ui.actividades

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codidevs.nutriapp.R
import com.codidevs.nutriapp.data.audio.SoundManager
import com.codidevs.nutriapp.ui.components.BarraProgresoActividad
import com.codidevs.nutriapp.ui.components.DecoracionFondoActividad
import com.codidevs.nutriapp.ui.components.pulsoAnimado
import com.codidevs.nutriapp.ui.components.ScreenHeader
import com.codidevs.nutriapp.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class AlimentoRuleta(
    val emoji: String,
    val nombre: String,
    val aporte: String,
    val opciones: List<String>,
    val curiosidad: String = ""
)

private val COLORES_RULETA = listOf(
    Color(0xFF50B16D), // Verde
    Color(0xFFFBA632), // Naranja
    Color(0xFF5EBEE1), // Azul
    Color(0xFFF2545B), // Rojo
    Color(0xFF9B59B6), // Morado
    Color(0xFFF1C40F)  // Amarillo
)

@Composable
fun RuedaAlimentacionScreen(
    alimentos: List<AlimentoRuleta>,
    onBack: () -> Unit,
    onTerminada: (puntaje: Int) -> Unit
) {
    // Seguridad: si no hay datos, volvemos atrás para evitar división por cero
    if (alimentos.isEmpty()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var indice by remember { mutableStateOf(0) }
    var puntaje by remember { mutableStateOf(0) }
    var seleccionada by remember { mutableStateOf<String?>(null) }
    var girando by remember { mutableStateOf(false) }
    var procesandoSiguiente by remember { mutableStateOf(false) }
    val rotacion = remember { Animatable(0f) }
    val escala = remember { Animatable(1f) }
    var particulas by remember { mutableStateOf<List<ParticulaCelebracion>>(emptyList()) }

    val sectorSize = 360f / alimentos.size
    LaunchedEffect(girando) {
        if (girando) {
            var lastTickSector = -1
            snapshotFlow { rotacion.value }
                .collect { valor ->
                    val currentSector = (valor / sectorSize).toInt()
                    if (currentSector != lastTickSector) {
                        lastTickSector = currentSector
                        SoundManager.ruletaTick()
                    }
                }
        }
    }

    val alimento = alimentos[indice]
    val respondio = seleccionada != null
    val esCorrecto = seleccionada == alimento.aporte
    val opcionesBarajadas = remember(indice) { alimento.opciones.shuffled() }

    LaunchedEffect(indice) {
        girando = true
        SoundManager.ruletaGiro()
        
        // ¡EXPLOSIÓN AL COMENZAR A GIRAR!
        particulas = List(30) { ParticulaCelebracion.aleatoria() }
        
        escala.animateTo(1.05f, tween(400))

        val actual = ((rotacion.value % 360f) + 360f) % 360f
        val deseado = ((270f - (indice + 0.5f) * sectorSize) + 360f) % 360f
        var delta = deseado - actual
        if (delta <= 0) delta += 360f

        rotacion.animateTo(
            targetValue = rotacion.value + delta + 360f * 4f,
            animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing)
        )
        escala.animateTo(1f, tween(300))
        SoundManager.ruletaParo()
        
        // ¡EXPLOSIÓN AL TERMINAR DE GIRAR!
        particulas = List(45) { ParticulaCelebracion.aleatoria() }
        girando = false
        kotlinx.coroutines.delay(2000)
        particulas = emptyList()
    }

    Box(modifier = Modifier.fillMaxSize().background(BgApp)) {
        DecoracionFondoActividad()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenHeader(titulo = "Rueda de la alimentación", onBack = onBack)

            Spacer(Modifier.height(12.dp))

            BarraProgresoActividad(actual = indice + 1, total = alimentos.size)

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                RuletaEstilizada(
                    rotacion = rotacion.value,
                    escala = escala.value,
                    alimentos = alimentos
                )
                
                if (particulas.isNotEmpty()) {
                    particulas.forEach { p -> ParticulaAnimada(p) }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tarjeta Premium para la ruleta
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(32.dp),
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!girando) {
                        Text(
                            text = "¡Cayó en ${alimento.emoji} ${alimento.nombre}!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "¿Qué aporta el ${alimento.nombre.lowercase()}?",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = InkSoft,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            opcionesBarajadas.chunked(2).forEach { fila ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    fila.forEach { opcion ->
                                        BotonOpcion(
                                            texto = opcion,
                                            seleccionada = seleccionada == opcion,
                                            respondio = respondio,
                                            esCorrecta = opcion == alimento.aporte,
                                            onClick = {
                                                seleccionada = opcion
                                                if (opcion == alimento.aporte) puntaje += 10
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "¡Girando la rueda!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Mango,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 30.dp)
                        )
                    }
                }
            }

            if (respondio) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = if (esCorrecto) LeafLight else BerryLight,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (esCorrecto) "✅ ¡Súper! +10 puntos" else "❌ Aporta ${alimento.aporte}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (esCorrecto) LeafDark else Berry,
                            textAlign = TextAlign.Center
                        )
                        if (esCorrecto && alimento.curiosidad.isNotEmpty()) {
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
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = com.codidevs.nutriapp.data.audio.onClickConSonido {
                        if (!procesandoSiguiente) {
                            if (indice + 1 >= alimentos.size) {
                                procesandoSiguiente = true
                                onTerminada(puntaje)
                            } else {
                                indice++; seleccionada = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Leaf),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .pulsoAnimado(escalaFinal = 1.05f) // El botón Siguiente late suavemente
                ) {
                    Text(if (indice + 1 >= alimentos.size) "Ver resultados" else "Siguiente alimento", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RuletaEstilizada(rotacion: Float, escala: Float, alimentos: List<AlimentoRuleta>) {
    val textMeasurer = rememberTextMeasurer()
    Box(
        modifier = Modifier.size(210.dp).graphicsLayer { scaleX = escala; scaleY = escala },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radio = size.minDimension / 2f
            val centro = center
            val sector = 360f / alimentos.size

            drawCircle(color = Color(0xFFFBA632), radius = radio)
            drawCircle(color = Color(0xFFE3852B), radius = radio * 0.92f)
            
            rotate(degrees = rotacion, pivot = centro) {
                alimentos.forEachIndexed { i, item ->
                    drawArc(
                        color = COLORES_RULETA[i % COLORES_RULETA.size],
                        startAngle = i * sector,
                        sweepAngle = sector,
                        useCenter = true,
                        topLeft = Offset(centro.x - radio * 0.85f, centro.y - radio * 0.85f),
                        size = Size(radio * 1.7f, radio * 1.7f)
                    )
                    
                    val angulo = Math.toRadians((i * sector + sector / 2).toDouble())
                    val x = (centro.x + cos(angulo) * radio * 0.55).toFloat()
                    val y = (centro.y + sin(angulo) * radio * 0.55).toFloat()
                    val layout = textMeasurer.measure(item.emoji, TextStyle(fontSize = 28.sp))
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(x - layout.size.width / 2f, y - layout.size.height / 2f)
                    )
                }
            }
            drawCircle(color = Color.White, radius = radio * 0.35f)
        }

        Surface(
            color = Color(0xFFF2545B),
            shape = TriangleShape,
            modifier = Modifier.size(32.dp, 24.dp).align(Alignment.TopCenter).offset(y = (-4).dp).border(2.dp, Color.White, TriangleShape)
        ) {}
    }
}

@Composable
private fun BotonOpcion(
    texto: String,
    seleccionada: Boolean,
    respondio: Boolean,
    esCorrecta: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        respondio && esCorrecta -> LeafLight
        seleccionada && !esCorrecta -> BerryLight
        else -> Color.White
    }
    val borderColor = when {
        respondio && esCorrecta -> Leaf
        seleccionada && !esCorrecta -> Berry
        else -> LineColor
    }

    Surface(
        onClick = com.codidevs.nutriapp.data.audio.onClickConSonido { if (!respondio) onClick() },
        color = bgColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(2.dp, borderColor),
        modifier = Modifier
            .height(58.dp)
            .width(150.dp)
            .pulsoAnimado(enabled = !respondio, escalaFinal = 1.04f) // Latido muy ligero para las opciones
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if(esCorrecta && respondio) "✅" else "🔹", fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Ink,
                maxLines = 2,
                fontSize = 13.sp
            )
        }
    }
}

private val TriangleShape = object : androidx.compose.ui.graphics.Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: androidx.compose.ui.unit.LayoutDirection, density: androidx.compose.ui.unit.Density) = androidx.compose.ui.graphics.Outline.Generic(
        Path().apply {
            moveTo(size.width / 2f, size.height)
            lineTo(0f, 0f)
            lineTo(size.width, 0f)
            close()
        }
    )
}

@Composable
private fun ParticulaAnimada(p: ParticulaCelebracion) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, tween(p.duracion, easing = FastOutSlowInEasing))
    }
    val dist = 140.dp * anim.value
    Text(
        text = p.emoji,
        fontSize = p.tamano.sp,
        modifier = Modifier
            .offset(x = (cos(p.angulo) * dist.value).dp, y = (sin(p.angulo) * dist.value).dp)
            .alpha(1f - anim.value)
    )
}

private data class ParticulaCelebracion(val emoji: String, val angulo: Double, val duracion: Int, val tamano: Int) {
    companion object {
        fun aleatoria() = ParticulaCelebracion(
            listOf("⭐", "✨", "🍎", "🥦", "🥑").random(),
            Random.nextDouble() * 2 * Math.PI,
            1000 + Random.nextInt(1000),
            14 + Random.nextInt(12)
        )
    }
}
