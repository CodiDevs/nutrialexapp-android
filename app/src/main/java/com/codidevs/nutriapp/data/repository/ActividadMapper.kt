package com.codidevs.nutriapp.data.repository

import com.codidevs.nutriapp.data.models.*
import com.codidevs.nutriapp.ui.actividades.AccionReto
import com.codidevs.nutriapp.ui.actividades.AlimentoRuleta
import com.codidevs.nutriapp.ui.actividades.PreguntaQuiz
import com.codidevs.nutriapp.ui.actividades.SemaforoDatos
import org.json.JSONObject

object ActividadMapper {

    fun descubre(act: ActividadJson): List<com.codidevs.nutriapp.ui.actividades.ItemDescubre> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            com.codidevs.nutriapp.ui.actividades.ItemDescubre(
                o.getString("emoji"),
                o.getString("nombre"),
                o.getString("texto")
            )
        }

    fun preguntasVF(act: ActividadJson): List<PreguntaVF> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            PreguntaVF(
                o.getString("emoji"),
                o.getString("enunciado"),
                o.getBoolean("verdadero"),
                o.optString("curiosidad", "")
            )
        }

    fun frases(act: ActividadJson): List<FraseNivel2> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            FraseNivel2(
                o.getString("emoji"),
                o.getString("antes"),
                o.getString("despues"),
                o.getString("respuesta"),
                (0 until o.getJSONArray("opciones").length()).map { j -> o.getJSONArray("opciones").getString(j) },
                o.optString("curiosidad", "")
            )
        }

    fun mejorOpcion(act: ActividadJson): List<MejorOpcionNivel2> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            val correcta = o.getJSONObject("correcta")
            val incorrecta = o.getJSONObject("incorrecta")
            MejorOpcionNivel2(
                o.getString("pregunta"),
                correcta.getString("emoji"),
                correcta.getString("texto"),
                incorrecta.getString("emoji"),
                incorrecta.getString("texto"),
                correcta.optString("curiosidad", "")
            )
        }

    fun ruleta(act: ActividadJson): List<AlimentoRuleta> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            AlimentoRuleta(
                o.getString("emoji"),
                o.getString("nombre"),
                o.getString("aporte"),
                (0 until o.getJSONArray("opciones").length()).map { j -> o.getJSONArray("opciones").getString(j) },
                o.optString("curiosidad", "")
            )
        }

    fun memoria(act: ActividadJson): List<ParMemoria> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            ParMemoria(
                o.getString("emoji"),
                o.getString("texto"),
                o.optString("curiosidad", "")
            )
        }

    fun quiz(act: ActividadJson): List<PreguntaQuiz> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            PreguntaQuiz(
                o.getString("pregunta"),
                o.getString("correcta"),
                (0 until o.getJSONArray("incorrectas").length()).map { j -> o.getJSONArray("incorrectas").getString(j) },
                o.optString("curiosidad", "")
            )
        }

    fun semaforo(act: ActividadJson): SemaforoDatos {
        val o = act.datosObjeto ?: return SemaforoDatos(emptyList(), emptyList(), emptyList())
        return SemaforoDatos(
            listaAlimentos(o.getJSONArray("verde")),
            listaAlimentos(o.getJSONArray("amarillo")),
            listaAlimentos(o.getJSONArray("rojo"))
        )
    }

    fun reto(act: ActividadJson): List<AccionReto> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            AccionReto(
                o.getString("emoji"),
                o.getString("accion"),
                o.getInt("puntos")
            )
        }

    fun une(act: ActividadJson): List<ItemDato> =
        (0 until act.datos.length()).map { i ->
            val o = act.datos.getJSONObject(i)
            ItemDato(
                o.getString("emoji"),
                o.getString("texto"),
                o.optString("nombre", ""),
                o.optString("curiosidad", "")
            )
        }

    private fun listaAlimentos(arr: org.json.JSONArray): List<AlimentoSemaforo> =
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            AlimentoSemaforo(
                o.getString("emoji"),
                o.getString("nombre"),
                o.optString("curiosidad", "")
            )
        }
}
