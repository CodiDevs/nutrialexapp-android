package com.codidevs.nutriapp.ui.actividades

import androidx.compose.runtime.Composable
import com.codidevs.nutriapp.data.models.*

/**
 * Pantalla genérica de actividad: recibe el nivel y la actividad (del JSON),
 * carga los datos según su tipo y muestra el minijuego correspondiente.
 * Al terminar, llama a onTerminada(puntaje) para que MainActivity registre el progreso.
 */
@Composable
fun ActividadGenericaScreen(
    tipo: String,
    datos: Any?,
    titulo: String,
    puntosMaximos: Int,
    onBack: () -> Unit,
    onTerminada: (puntaje: Int, porcentaje: Int) -> Unit
) {
    // Solo "descubre" es puramente exploratorio y siempre da 100%.
    // Las demás (ruleta, semáforo, une, etc.) tienen puntuación y pueden fallar.
    val sinFallos = tipo == "descubre"
    val terminar = { puntaje: Int ->
        val porcentaje = if (sinFallos) {
            100
        } else {
            if (puntosMaximos > 0) (puntaje * 100 / puntosMaximos).coerceIn(0, 100) else 0
        }
        onTerminada(puntaje, porcentaje)
    }

    when (tipo) {
        "descubre" -> {
            val lista = datos as? List<ItemDescubre>
            DescubreAlimentosScreen(
                alimentos = lista?.map { it.toAlimento() } ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "grupos" -> {
            val lista = datos as? List<com.codidevs.nutriapp.data.models.GrupoAlimenticio>
            GrupoPerteneceScreen(
                grupos = lista ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "memoria" -> {
            val lista = datos as? List<ParMemoria>
            MemoriaNutritivaScreen(
                pares = lista ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "vf" -> {
            val lista = datos as? List<com.codidevs.nutriapp.data.models.PreguntaVF>
            VerdaderoFalsoScreen(
                preguntas = lista ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "completa" -> {
            val lista = datos as? List<FraseIncompleta>
            CompletaFraseScreen(
                frases = lista ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "mejor_opcion", "situaciones" -> {
            val lista = datos as? List<MejorOpcionNivel2>
            if (!lista.isNullOrEmpty()) {
                MejorOpcionNivel2Screen(
                    preguntas = lista,
                    onBack = onBack,
                    onTerminada = terminar
                )
            } else {
                onBack()
            }
        }
        "ruleta" -> {
            val lista = datos as? List<AlimentoRuleta>
            RuedaAlimentacionScreen(
                alimentos = lista ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "quiz" -> {
            val lista = datos as? List<PreguntaQuiz>
            QuizScreen(
                preguntas = lista ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "semaforo" -> {
            val dato = datos as? SemaforoDatos
            SemaforoScreen(
                datos = dato ?: SemaforoDatos(emptyList(), emptyList(), emptyList()),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "reto" -> {
            val lista = datos as? List<AccionReto>
            RetoScreen(
                acciones = lista ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        "une" -> {
            val lista = datos as? List<com.codidevs.nutriapp.data.models.ItemDato>
            UneImagenScreen(
                pares = lista ?: emptyList(),
                onBack = onBack,
                onTerminada = terminar
            )
        }
        else -> {
            onBack()
        }
    }
}

/** Item de "descubre" del JSON. */
data class ItemDescubre(
    val emoji: String,
    val nombre: String,
    val texto: String
) {
    fun toAlimento() = com.codidevs.nutriapp.data.models.Alimento(emoji, nombre, texto)
}
