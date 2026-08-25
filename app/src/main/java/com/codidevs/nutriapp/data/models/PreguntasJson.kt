package com.codidevs.nutriapp.data.models

import org.json.JSONArray
import org.json.JSONObject

/** Datos de un alimento/nutriente/pareja genérico del JSON. */
data class ItemDato(
    val emoji: String,
    val texto: String,
    val nombre: String = "",
    val curiosidad: String = ""
)

/** Una actividad de un nivel (con su tipo y datos crudos). */
data class ActividadJson(
    val id: Int,
    val tipo: String,
    val nombre: String,
    val emoji: String,
    val datos: org.json.JSONArray,
    val datosObjeto: org.json.JSONObject? = null
)

/** Un nivel con sus actividades. */
data class NivelJson(
    val numero: Int,
    val titulo: String,
    val descripcion: String,
    val monedas: Int,
    val actividades: List<ActividadJson>
)

/** Un módulo con sus niveles. */
data class ModuloJson(
    val id: Int,
    val nombre: String,
    val emoji: String,
    val niveles: List<NivelJson>
)

/** Carga y parsea el JSON de preguntas desde assets. */
object PreguntasJson {

    fun parsear(texto: String): List<ModuloJson> {
        val raiz = JSONObject(texto)
        val modulos = raiz.getJSONArray("modulos")
        return (0 until modulos.length()).map { i ->
            val m = modulos.getJSONObject(i)
            ModuloJson(
                id = m.getInt("id"),
                nombre = m.getString("nombre"),
                emoji = m.getString("emoji"),
                niveles = parsearNiveles(m.getJSONArray("niveles"))
            )
        }
    }

    private fun parsearNiveles(arr: JSONArray): List<NivelJson> =
        (0 until arr.length()).map { i ->
            val n = arr.getJSONObject(i)
            NivelJson(
                numero = n.getInt("numero"),
                titulo = n.getString("titulo"),
                descripcion = n.getString("descripcion"),
                monedas = n.getInt("monedas"),
                actividades = parsearActividades(n.getJSONArray("actividades"))
            )
        }

    private fun parsearActividades(arr: JSONArray): List<ActividadJson> =
        (0 until arr.length()).map { i ->
            val a = arr.getJSONObject(i)
            val datosRaw = a.get("datos")
            ActividadJson(
                id = a.getInt("id"),
                tipo = a.getString("tipo"),
                nombre = a.getString("nombre"),
                emoji = a.getString("emoji"),
                datos = datosRaw as? JSONArray ?: JSONArray(),
                datosObjeto = datosRaw as? JSONObject
            )
        }
}
