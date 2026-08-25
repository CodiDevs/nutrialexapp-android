package com.codidevs.nutriapp.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Guarda el progreso del niño en el dispositivo (SharedPreferences).
 */
class ProgresoRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("progreso_nutriapp", Context.MODE_PRIVATE)

    fun registrarResultadoActividad(nivel: Int, actividadId: Int, porcentaje: Int, puntaje: Int): Int {
        val clavePorcentaje = "porcentaje_nivel_${nivel}_actividad_$actividadId"
        val mejorAnterior = prefs.getInt(clavePorcentaje, -1)
        if (porcentaje > mejorAnterior) prefs.edit().putInt(clavePorcentaje, porcentaje).apply()
        
        val clavePuntaje = "puntaje_nivel_${nivel}_actividad_$actividadId"
        val mejorPuntajeAnterior = prefs.getInt(clavePuntaje, -1)
        if (puntaje > mejorPuntajeAnterior) prefs.edit().putInt(clavePuntaje, puntaje).apply()
        
        return estrellasPorPorcentaje(maxOf(mejorAnterior, porcentaje))
    }

    fun estrellasPorPorcentaje(porcentaje: Int): Int = when {
        porcentaje >= 100 -> 3
        porcentaje >= 70 -> 2
        porcentaje >= 40 -> 1
        else -> 0
    }

    fun monedasMinijuego(estrellas: Int): Int = when {
        estrellas >= 3 -> 20
        estrellas >= 2 -> 10
        estrellas >= 1 -> 5
        else -> 0
    }

    fun estrellasActividad(nivel: Int, actividadId: Int): Int {
        val pct = prefs.getInt("porcentaje_nivel_${nivel}_actividad_$actividadId", -1)
        return if (pct >= 0) estrellasPorPorcentaje(pct) else -1
    }

    fun porcentajeActividad(nivel: Int, actividadId: Int): Int =
        prefs.getInt("porcentaje_nivel_${nivel}_actividad_$actividadId", -1)

    fun estrellasAsignadasActividad(nivel: Int, actividadId: Int, totalActividades: Int): Int {
        val completada = estrellasActividad(nivel, actividadId) > 0
        if (!completada) return 0
        val asignadas = estrellasPorActividad(totalActividades)
        return asignadas[actividadId] ?: 0
    }

    fun estrellasPorActividad(totalActividades: Int): Map<Int, Int> {
        if (totalActividades <= 0) return emptyMap()
        val res = mutableMapOf<Int, Int>()
        when (totalActividades) {
            1 -> res[1] = 3
            2 -> { res[1] = 2; res[2] = 1 }
            3 -> { res[1] = 1; res[2] = 1; res[3] = 1 }
            else -> {
                val mitad = (totalActividades / 2) + 1
                for (i in 1..totalActividades) {
                    res[i] = if (i == 1 || i == mitad || i == totalActividades) 1 else 0
                }
            }
        }
        return res
    }

    fun setEstrellasMinijuego(id: String, estrellas: Int) {
        prefs.edit().putInt("minijuego_estrellas_$id", estrellas).apply()
    }

    fun setPuntajeMinijuego(id: String, puntaje: Int) {
        val actual = prefs.getInt("minijuego_puntaje_$id", 0)
        if (puntaje > actual) prefs.edit().putInt("minijuego_puntaje_$id", puntaje).apply()
    }

    fun estrellasMinijuego(id: String): Int = prefs.getInt("minijuego_estrellas_$id", -1)

    fun minijuegoCompletado(id: String): Boolean = prefs.getInt("minijuego_estrellas_$id", -1) >= 2

    fun setMedallaPerfil(id: String) {
        prefs.edit().putString("medalla_perfil", id).apply()
    }

    val medallaPerfil: String get() = prefs.getString("medalla_perfil", "") ?: ""

    fun canjearRecompensa(id: String, costo: Int) {
        prefs.edit()
            .putBoolean("canjeada_$id", true)
            .putInt("monedas_gastadas", prefs.getInt("monedas_gastadas", 0) + costo)
            .apply()
    }

    fun recompensaCanjeada(id: String): Boolean = prefs.getBoolean("canjeada_$id", false)

    fun guardarUsuario(nombre: String, edad: Int, peso: Double, estatura: Double, sexo: String) {
        prefs.edit()
            .putString("usuario_nombre", nombre)
            .putInt("usuario_edad", edad)
            .putString("usuario_peso", peso.toString())
            .putString("usuario_estatura", estatura.toString())
            .putString("usuario_sexo", sexo)
            .apply()
    }

    fun guardarConsentimientoTutor() {
        prefs.edit().putString("tutor_consent_at", System.currentTimeMillis().toString()).apply()
    }

    val usuarioRegistrado: Boolean get() = prefs.contains("usuario_nombre")
    val usuarioNombre: String get() = prefs.getString("usuario_nombre", "") ?: ""
    val usuarioSexo: String get() = prefs.getString("usuario_sexo", "niño") ?: "niño"

    fun borrarTodo() {
        prefs.edit().clear().apply()
    }

    fun registrarDiaActivo(): Int {
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT)
        val hoy = formato.format(java.util.Date())
        val ultimo = prefs.getString("ultimo_dia", "") ?: ""
        var racha = prefs.getInt("racha_dias", 0)
        if (ultimo != hoy) {
            val ayer = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }.time.let { formato.format(it) }
            racha = if (ultimo == ayer) racha + 1 else 1
            prefs.edit().putString("ultimo_dia", hoy).putInt("racha_dias", racha).apply()
        }
        return racha
    }

    val rachaDias: Int get() = prefs.getInt("racha_dias", 0)

    fun nivelCompleto(nivel: Int, totalActividades: Int): Boolean {
        if (totalActividades <= 0) return false
        val completadas = (1..totalActividades).count { id ->
            prefs.getInt("porcentaje_nivel_${nivel}_actividad_$id", -1) >= 40
        }
        return completadas >= totalActividades
    }

    fun estrellasTotales(actividadesPorNivel: Map<Int, Int>): Int {
        var total = 0
        actividadesPorNivel.forEach { (nivel, numAct) ->
            for (id in 1..numAct) {
                total += estrellasAsignadasActividad(nivel, id, numAct)
            }
        }
        listOf("arrastrar", "vf", "completa", "mejor", "ruleta", "memoria").forEach { id ->
            val est = prefs.getInt("minijuego_estrellas_$id", -1)
            if (est > 0) total += est
        }
        return total
    }

    fun monedasTotales(actividadesPorNivel: Map<Int, Int>): Int {
        var total = 0
        actividadesPorNivel.forEach { (nivel, numAct) ->
            for (id in 1..numAct) {
                val pct = prefs.getInt("porcentaje_nivel_${nivel}_actividad_$id", -1)
                if (pct > 0) total += (20 * pct / 100)
            }
        }
        listOf("arrastrar", "vf", "completa", "mejor", "ruleta", "memoria").forEach { id ->
            val est = prefs.getInt("minijuego_estrellas_$id", -1)
            if (est > 0) total += monedasMinijuego(est)
        }
        return (total - prefs.getInt("monedas_gastadas", 0)).coerceAtLeast(0)
    }

    fun puntosTotales(actividadesPorNivel: Map<Int, Int>): Int {
        var total = 0
        actividadesPorNivel.forEach { (nivel, numAct) ->
            for (id in 1..numAct) {
                total += prefs.getInt("puntaje_nivel_${nivel}_actividad_$id", 0)
            }
        }
        listOf("arrastrar", "vf", "completa", "mejor", "ruleta", "memoria").forEach { id ->
            total += prefs.getInt("minijuego_puntaje_$id", 0)
        }
        return total
    }
}
