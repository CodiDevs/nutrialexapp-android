package com.codidevs.nutriapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import com.codidevs.nutriapp.data.audio.SoundManager
import com.codidevs.nutriapp.data.models.*
import com.codidevs.nutriapp.data.repository.ActividadMapper
import com.codidevs.nutriapp.data.repository.PreguntasRepository
import com.codidevs.nutriapp.data.repository.ProgresoRepository
import com.codidevs.nutriapp.ui.actividades.*
import com.codidevs.nutriapp.ui.components.TabScaffold
import com.codidevs.nutriapp.ui.home.HomeScreen
import com.codidevs.nutriapp.ui.juegos.JuegosScreen
import com.codidevs.nutriapp.ui.navigation.NutriRoutes
import com.codidevs.nutriapp.ui.onboarding.ImcScreen
import com.codidevs.nutriapp.ui.onboarding.ModulosScreen
import com.codidevs.nutriapp.ui.onboarding.PrivacyScreen
import com.codidevs.nutriapp.ui.onboarding.RegistroScreen
import com.codidevs.nutriapp.ui.onboarding.SplashScreen
import com.codidevs.nutriapp.ui.perfil.PerfilScreen
import com.codidevs.nutriapp.ui.recompensas.RecompensasScreen
import com.codidevs.nutriapp.ui.sendero.ActividadInfo
import com.codidevs.nutriapp.ui.sendero.ActividadesScreen
import com.codidevs.nutriapp.ui.sendero.NIVELES_INFO
import com.codidevs.nutriapp.ui.sendero.NivelDetalleScreen
import com.codidevs.nutriapp.ui.sendero.SenderoScreen
import com.codidevs.nutriapp.ui.theme.NutriAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoundManager.init()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent {
            NutriAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val progreso = remember { ProgresoRepository(applicationContext) }
                    val preguntas = remember { PreguntasRepository(applicationContext) }
                    
                    val prefs = getSharedPreferences("config_app", MODE_PRIVATE)
                    val limpiaRealizada = prefs.getBoolean("limpieza_v1", false)
                    if (!limpiaRealizada) {
                        progreso.borrarTodo()
                        prefs.edit().putBoolean("limpieza_v1", true).apply()
                    }

                    remember { progreso.registrarDiaActivo() }
                    val inicio = if (progreso.usuarioRegistrado) NutriRoutes.HOME else NutriRoutes.SPLASH
                    var nombreUsuario by remember { mutableStateOf(progreso.usuarioNombre) }
                    var tabActiva by remember { mutableStateOf("home") }
                    var moduloActual by remember { mutableIntStateOf(1) }
                    var versionProgreso by remember { mutableIntStateOf(0) }
                    val actividadesPorNivel = remember {
                        (1..7).associateWith { preguntas.totalActividadesNivel(it) }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = inicio,
                        modifier = Modifier.fillMaxSize().safeDrawingPadding()
                    ) {
                        composable(NutriRoutes.SPLASH) {
                            SplashScreen(onComenzar = {
                                navController.navigate(
                                    if (progreso.usuarioRegistrado) NutriRoutes.HOME else NutriRoutes.REGISTRO
                                )
                            })
                        }
                        composable(NutriRoutes.PRIVACY) {
                            PrivacyScreen(onBack = { navController.popBackStack() })
                        }
                        composable(NutriRoutes.REGISTRO) {
                            RegistroScreen(
                                onBack = { navController.popBackStack() },
                                onVerPrivacidad = {
                                    navController.navigate(NutriRoutes.PRIVACY)
                                },
                                onContinuar = { nombre, edad, peso, estatura, sexo ->
                                    progreso.guardarConsentimientoTutor()
                                    navController.navigate(
                                        "${NutriRoutes.IMC}/${Uri.encode(nombre)}/$edad/$peso/$estatura/$sexo"
                                    )
                                }
                            )
                        }
                        composable(
                            "${NutriRoutes.IMC}/{nombre}/{edad}/{peso}/{estatura}/{sexo}"
                        ) { backStackEntry ->
                            val args = backStackEntry.arguments
                            val nombre = args?.getString("nombre").orEmpty()
                            val edad = args?.getString("edad")?.toIntOrNull() ?: 0
                            val peso = args?.getString("peso")?.toDoubleOrNull() ?: 0.0
                            val estatura = args?.getString("estatura")?.toDoubleOrNull() ?: 0.0
                            val sexo = args?.getString("sexo") ?: "niño"
                            ImcScreen(
                                nombre = nombre, edad = edad, peso = peso, estatura = estatura, sexo = sexo,
                                onBack = { navController.popBackStack() },
                                onAventura = {
                                    progreso.guardarUsuario(nombre, edad, peso, estatura, sexo)
                                    nombreUsuario = nombre
                                    navController.navigate("${NutriRoutes.MODULOS}/${Uri.encode(nombre)}")
                                }
                            )
                        }
                        composable("${NutriRoutes.MODULOS}/{nombre}") { backStackEntry ->
                            val nombre = backStackEntry.arguments?.getString("nombre").orEmpty()
                            ModulosScreen(
                                nombre = nombre,
                                nutricionCompletado = (1..3).all { progreso.nivelCompleto(it, preguntas.totalActividadesNivel(it)) },
                                actividadCompletado = (4..7).all { progreso.nivelCompleto(it, preguntas.totalActividadesNivel(it)) },
                                nivelNutricion = 1 + (if (progreso.nivelCompleto(1, preguntas.totalActividadesNivel(1))) 1 else 0) + (if (progreso.nivelCompleto(2, preguntas.totalActividadesNivel(2))) 1 else 0),
                                onBack = { navController.popBackStack() },
                                onNutricion = {
                                    nombreUsuario = nombre; moduloActual = 1; navController.navigate(NutriRoutes.HOME)
                                },
                                onActividadFisica = {
                                    nombreUsuario = nombre; moduloActual = 2; navController.navigate(NutriRoutes.HOME)
                                }
                            )
                        }
                        composable(NutriRoutes.HOME) {
                            BackHandler(enabled = tabActiva != "home") { tabActiva = "home" }
                            val totalMonedas = remember(versionProgreso) { progreso.monedasTotales(actividadesPorNivel) }
                            val totalEstrellas = remember(versionProgreso) { progreso.estrellasTotales(actividadesPorNivel) }
                            TabScaffold(
                                tabActiva = tabActiva, onTab = { tabActiva = it }, monedas = "🪙 $totalMonedas",
                                racha = "🔥 ${progreso.rachaDias}", estrellas = "⭐ $totalEstrellas"
                            ) {
                                when (tabActiva) {
                                    "sendero" -> SenderoScreen(
                                        modulo = moduloActual,
                                        nivelesDesbloqueados = 1 + (1..6).count { progreso.nivelCompleto(it, preguntas.totalActividadesNivel(it)) },
                                        estrellasNivel = remember(versionProgreso) {
                                            (1..7).associateWith { nivel ->
                                                val acts = preguntas.actividadesDelNivel(nivel)
                                                acts.sumOf { progreso.estrellasAsignadasActividad(nivel, it.id, acts.size) }.coerceIn(0, 3)
                                            }
                                        },
                                        onNivelClick = { numero -> navController.navigate("${NutriRoutes.NIVEL_DETALLE}/$numero") },
                                        onElegirModulo = { navController.navigate("${NutriRoutes.MODULOS}/${Uri.encode(nombreUsuario)}") },
                                        onCambiarModulo = { navController.navigate("${NutriRoutes.MODULOS}/${Uri.encode(nombreUsuario)}") }
                                    )
                                    "juegos" -> JuegosScreen(
                                        estrellas = remember(versionProgreso) {
                                            listOf("arrastrar", "vf", "completa", "mejor", "ruleta", "memoria").associateWith { progreso.estrellasMinijuego(it) }
                                        },
                                        onMinijuegoClick = { id ->
                                            val ruta = when (id) {
                                                "arrastrar" -> NutriRoutes.JUEGO_ARRASTRAR
                                                "vf" -> NutriRoutes.JUEGO_VF
                                                "completa" -> NutriRoutes.JUEGO_COMPLETA
                                                "mejor" -> NutriRoutes.JUEGO_MEJOR
                                                "ruleta" -> NutriRoutes.JUEGO_RULETA
                                                "memoria" -> NutriRoutes.JUEGO_MEMORIA
                                                else -> null
                                            }
                                            ruta?.let { navController.navigate(it) }
                                        }
                                    )
                                    "perfil" -> {
                                        val nivelesCompletados = (1..7).filter { progreso.nivelCompleto(it, preguntas.totalActividadesNivel(it)) }.toSet()
                                        val minijuegosCompleto = listOf("arrastrar", "vf", "completa", "mejor", "ruleta", "memoria").all { progreso.minijuegoCompletado(it) }
                                        PerfilScreen(
                                            nombre = nombreUsuario, nivel = nivelActualGlobal(progreso, preguntas), sexo = progreso.usuarioSexo,
                                            puntos = remember(versionProgreso) { progreso.puntosTotales(actividadesPorNivel) },
                                            medallas = remember(versionProgreso) {
                                                CatalogoMedallas.conProgreso(nivelesCompletados, minijuegosCompleto).filter { progreso.recompensaCanjeada(it.id) }
                                            },
                                            medallaPerfil = progreso.medallaPerfil,
                                            onPonerMedalla = { id -> progreso.setMedallaPerfil(id); versionProgreso++ },
                                            onVerRecompensas = { navController.navigate(NutriRoutes.RECOMPENSAS) },
                                            onVerPrivacidad = { navController.navigate(NutriRoutes.PRIVACY) },
                                            onCrearRegistro = { progreso.borrarTodo(); nombreUsuario = ""; versionProgreso++; navController.navigate(NutriRoutes.REGISTRO) { popUpTo(NutriRoutes.HOME) { inclusive = true } } }
                                        )
                                    }
                                    else -> HomeScreen(
                                        nombre = nombreUsuario, sexo = progreso.usuarioSexo,
                                        nivelTexto = nivelActualTexto(progreso, preguntas, moduloActual),
                                        onSendero = { tabActiva = "sendero" },
                                        onRecompensas = { navController.navigate(NutriRoutes.RECOMPENSAS) },
                                        onPerfil = { tabActiva = "perfil" }
                                    )
                                }
                            }
                        }
                        composable("${NutriRoutes.NIVEL_DETALLE}/{nivelId}") { backStackEntry ->
                            val nivelId = backStackEntry.arguments?.getString("nivelId")?.toIntOrNull() ?: 1
                            val nivelJson = preguntas.nivel(nivelId)
                            val nivel = NIVELES_INFO.firstOrNull { it.numero == nivelId } ?: NIVELES_INFO.first()
                            val nivelInfo = nivelJson?.let { n ->
                                nivel.copy(titulo = n.titulo, descripcion = n.descripcion, actividades = n.actividades.size, monedas = "+${20 * n.actividades.size}", puntosMaximos = preguntas.puntosMaximosNivel(nivelId))
                            } ?: nivel
                            NivelDetalleScreen(nivel = nivelInfo, onBack = { navController.popBackStack() }, onVerActividades = { navController.navigate("${NutriRoutes.ACTIVIDADES}/${nivelInfo.numero}") })
                        }
                        composable("${NutriRoutes.ACTIVIDADES}/{nivelId}") { backStackEntry ->
                            val nivelId = backStackEntry.arguments?.getString("nivelId")?.toIntOrNull() ?: 1
                            val actividadesJson = remember(nivelId) { preguntas.actividadesDelNivel(nivelId) }
                            val actividadesNivel = remember(actividadesJson) { actividadesJson.map { act -> ActividadInfo(act.id, act.emoji, act.nombre) } }
                            val estrellasActividades = remember(nivelId, versionProgreso) { actividadesNivel.associate { act -> act.id to progreso.estrellasAsignadasActividad(nivelId, act.id, actividadesNivel.size) } }
                            val porcentajesActividades = remember(nivelId, versionProgreso) { actividadesNivel.associate { act -> act.id to progreso.porcentajeActividad(nivelId, act.id) } }
                            ActividadesScreen(nivelNumero = nivelId, actividades = actividadesNivel, estrellas = estrellasActividades, porcentajes = porcentajesActividades, onBack = { navController.popBackStack() }, onActividadClick = { actividad -> navController.navigate("${NutriRoutes.ACTIVIDAD}/$nivelId/${actividad.id}") }, onNivelCompletado = { tabActiva = "sendero"; navController.popBackStack(NutriRoutes.HOME, false) })
                        }
                        composable("${NutriRoutes.ACTIVIDAD}/{nivelId}/{actividadId}") { backStackEntry ->
                            val nivelId = backStackEntry.arguments?.getString("nivelId")?.toIntOrNull() ?: 1
                            val actividadId = backStackEntry.arguments?.getString("actividadId")?.toIntOrNull() ?: 1
                            val actJson = preguntas.actividadesDelNivel(nivelId).firstOrNull { it.id == actividadId }
                            val datos: Any? = when (actJson?.tipo) {
                                "descubre" -> actJson?.let { ActividadMapper.descubre(it) }
                                "grupos" -> GruposAlimenticios.TODOS
                                "memoria" -> actJson?.let { ActividadMapper.memoria(it) }
                                "une" -> actJson?.let { ActividadMapper.une(it) }
                                "vf" -> actJson?.let { ActividadMapper.preguntasVF(it) }
                                "completa" -> actJson?.let {
                                    ActividadMapper.frases(it).map { frase ->
                                        FraseIncompleta(emoji = frase.emoji, fraseAntes = frase.antes, fraseDespues = frase.despues, respuesta = frase.respuesta, opciones = frase.opciones, curiosidad = frase.curiosidad)
                                    }
                                }
                                "mejor_opcion", "situaciones" -> actJson?.let { ActividadMapper.mejorOpcion(it) }
                                "ruleta" -> actJson?.let { ActividadMapper.ruleta(it) }
                                "quiz" -> actJson?.let { ActividadMapper.quiz(it) }
                                "semaforo" -> actJson?.let { ActividadMapper.semaforo(it) }
                                "reto" -> actJson?.let { ActividadMapper.reto(it) }
                                else -> null
                            }
                            ActividadGenericaScreen(
                                tipo = actJson?.tipo ?: "", datos = datos, titulo = actJson?.nombre ?: "Actividad",
                                puntosMaximos = actJson?.let { preguntas.puntosMaximosActividad(it) } ?: 0,
                                onBack = { navController.popBackStack() },
                                onTerminada = { puntaje, porcentaje ->
                                    val totalMonedasAntes = progreso.monedasTotales(actividadesPorNivel)
                                    val totalPuntosAntes = progreso.puntosTotales(actividadesPorNivel)
                                    val actsJson = preguntas.actividadesDelNivel(nivelId)
                                    val estrellasAntes = progreso.estrellasAsignadasActividad(nivelId, actividadId, actsJson.size)
                                    progreso.registrarResultadoActividad(nivelId, actividadId, porcentaje, puntaje)
                                    val totalMonedasDespues = progreso.monedasTotales(actividadesPorNivel)
                                    val totalPuntosDespues = progreso.puntosTotales(actividadesPorNivel)
                                    val estrellasDespues = progreso.estrellasAsignadasActividad(nivelId, actividadId, actsJson.size)
                                    val monedasGanadas = (totalMonedasDespues - totalMonedasAntes).coerceAtLeast(0)
                                    val puntosGanados = (totalPuntosDespues - totalPuntosAntes).coerceAtLeast(0)
                                    val estrellasGanadas = (estrellasDespues - estrellasAntes).coerceAtLeast(0)
                                    versionProgreso++
                                    navController.navigate("${NutriRoutes.PREMIO}/$porcentaje/$estrellasGanadas/$monedasGanadas/$puntosGanados") { popUpTo("${NutriRoutes.ACTIVIDAD}/$nivelId/$actividadId") { inclusive = true } }
                                }
                            )
                        }
                        composable(NutriRoutes.JUEGO_ARRASTRAR) {
                            GrupoPerteneceScreen(grupos = GruposAlimenticios.TODOS, onBack = { navController.popBackStack() }, onTerminada = { puntaje ->
                                val porcentaje = (puntaje * 100 / 60).coerceIn(0, 100)
                                val estrellas = progreso.estrellasPorPorcentaje(porcentaje)
                                val totalMonedasAntes = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosAntes = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasAntes = progreso.estrellasMinijuego("arrastrar")
                                if (estrellas > estrellasAntes) progreso.setEstrellasMinijuego("arrastrar", estrellas)
                                progreso.setPuntajeMinijuego("arrastrar", puntaje)
                                val totalMonedasDespues = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosDespues = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasDespues = progreso.estrellasMinijuego("arrastrar")
                                val monedasGanadas = (totalMonedasDespues - totalMonedasAntes).coerceAtLeast(0)
                                val puntosGanados = (totalPuntosDespues - totalPuntosAntes).coerceAtLeast(0)
                                val estrellasGanadas = (estrellasDespues - maxOf(0, estrellasAntes)).coerceAtLeast(0)
                                versionProgreso++
                                navController.navigate("${NutriRoutes.PREMIO}/$porcentaje/$estrellasGanadas/$monedasGanadas/$puntosGanados") { popUpTo(NutriRoutes.JUEGO_ARRASTRAR) { inclusive = true } }
                            })
                        }
                        composable(NutriRoutes.JUEGO_VF) {
                            val vfAlimentos = preguntas.actividadesDelNivel(2).firstOrNull { it.tipo == "vf" }
                            val vfDeporte = preguntas.actividadesDelNivel(5).firstOrNull { it.tipo == "vf" }
                            val preguntasVF = remember { ((vfAlimentos?.let { ActividadMapper.preguntasVF(it) } ?: emptyList()) + (vfDeporte?.let { ActividadMapper.preguntasVF(it) } ?: emptyList())).shuffled() }
                            VerdaderoFalsoScreen(preguntas = preguntasVF, onBack = { navController.popBackStack() }, onTerminada = { puntaje ->
                                val maximo = preguntasVF.size * 10
                                val porcentaje = if (maximo > 0) (puntaje * 100 / maximo) else 0
                                val estrellas = progreso.estrellasPorPorcentaje(porcentaje)
                                val totalMonedasAntes = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosAntes = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasAntes = progreso.estrellasMinijuego("vf")
                                if (estrellas > estrellasAntes) progreso.setEstrellasMinijuego("vf", estrellas)
                                progreso.setPuntajeMinijuego("vf", puntaje)
                                val totalMonedasDespues = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosDespues = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasDespues = progreso.estrellasMinijuego("vf")
                                val monedasGanadas = (totalMonedasDespues - totalMonedasAntes).coerceAtLeast(0)
                                val puntosGanados = (totalPuntosDespues - totalPuntosAntes).coerceAtLeast(0)
                                val estrellasGanadas = (estrellasDespues - maxOf(0, estrellasAntes)).coerceAtLeast(0)
                                versionProgreso++
                                navController.navigate("${NutriRoutes.PREMIO}/$porcentaje/$estrellasGanadas/$monedasGanadas/$puntosGanados") { popUpTo(NutriRoutes.JUEGO_VF) { inclusive = true } }
                            })
                        }
                        composable(NutriRoutes.JUEGO_COMPLETA) {
                            val fraseAlimentos = preguntas.actividadesDelNivel(2).firstOrNull { it.tipo == "completa" }
                            val fraseHabitos = preguntas.actividadesDelNivel(7).firstOrNull { it.tipo == "completa" }
                            val frases = remember { ((fraseAlimentos?.let { ActividadMapper.frases(it) } ?: emptyList()) + (fraseHabitos?.let { ActividadMapper.frases(it) } ?: emptyList())).map { frase -> FraseIncompleta(emoji = frase.emoji, fraseAntes = frase.antes, fraseDespues = frase.despues, respuesta = frase.respuesta, opciones = frase.opciones, curiosidad = frase.curiosidad) }.shuffled() }
                            CompletaFraseScreen(frases = frases, onBack = { navController.popBackStack() }, onTerminada = { puntaje ->
                                val maximo = frases.size * 10
                                val porcentaje = if (maximo > 0) (puntaje * 100 / maximo) else 0
                                val estrellas = progreso.estrellasPorPorcentaje(porcentaje)
                                val totalMonedasAntes = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosAntes = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasAntes = progreso.estrellasMinijuego("completa")
                                if (estrellas > estrellasAntes) progreso.setEstrellasMinijuego("completa", estrellas)
                                progreso.setPuntajeMinijuego("completa", puntaje)
                                val totalMonedasDespues = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosDespues = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasDespues = progreso.estrellasMinijuego("completa")
                                val monedasGanadas = (totalMonedasDespues - totalMonedasAntes).coerceAtLeast(0)
                                val puntosGanados = (totalPuntosDespues - totalPuntosAntes).coerceAtLeast(0)
                                val estrellasGanadas = (estrellasDespues - maxOf(0, estrellasAntes)).coerceAtLeast(0)
                                versionProgreso++
                                navController.navigate("${NutriRoutes.PREMIO}/$porcentaje/$estrellasGanadas/$monedasGanadas/$puntosGanados") { popUpTo(NutriRoutes.JUEGO_COMPLETA) { inclusive = true } }
                            })
                        }
                        composable(NutriRoutes.JUEGO_MEJOR) {
                            val mejorAlimentos = preguntas.actividadesDelNivel(3).firstOrNull { it.tipo == "mejor_opcion" }
                            val mejorDeporte1 = preguntas.actividadesDelNivel(4).firstOrNull { it.tipo == "mejor_opcion" }
                            val mejorDeporte2 = preguntas.actividadesDelNivel(6).firstOrNull { it.tipo == "mejor_opcion" }
                            val preguntasMejor = remember { ((mejorAlimentos?.let { ActividadMapper.mejorOpcion(it) } ?: emptyList()) + (mejorDeporte1?.let { ActividadMapper.mejorOpcion(it) } ?: emptyList()) + (mejorDeporte2?.let { ActividadMapper.mejorOpcion(it) } ?: emptyList())).shuffled() }
                            MejorOpcionNivel2Screen(preguntas = preguntasMejor, onBack = { navController.popBackStack() }, onTerminada = { puntaje ->
                                val maximo = preguntasMejor.size * 10
                                val porcentaje = if (maximo > 0) (puntaje * 100 / maximo) else 0
                                val estrellas = progreso.estrellasPorPorcentaje(porcentaje)
                                val totalMonedasAntes = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosAntes = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasAntes = progreso.estrellasMinijuego("mejor")
                                if (estrellas > estrellasAntes) progreso.setEstrellasMinijuego("mejor", estrellas)
                                progreso.setPuntajeMinijuego("mejor", puntaje)
                                val totalMonedasDespues = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosDespues = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasDespues = progreso.estrellasMinijuego("mejor")
                                val monedasGanadas = (totalMonedasDespues - totalMonedasAntes).coerceAtLeast(0)
                                val puntosGanados = (totalPuntosDespues - totalPuntosAntes).coerceAtLeast(0)
                                val estrellasGanadas = (estrellasDespues - maxOf(0, estrellasAntes)).coerceAtLeast(0)
                                versionProgreso++
                                navController.navigate("${NutriRoutes.PREMIO}/$porcentaje/$estrellasGanadas/$monedasGanadas/$puntosGanados") { popUpTo(NutriRoutes.JUEGO_MEJOR) { inclusive = true } }
                            })
                        }
                        composable(NutriRoutes.JUEGO_RULETA) {
                            val ruletaData = preguntas.actividadesDelNivel(3).firstOrNull { it.tipo == "ruleta" }
                            val alimentosRuleta = remember { ruletaData?.let { ActividadMapper.ruleta(it) } ?: emptyList() }
                            RuedaAlimentacionScreen(alimentos = alimentosRuleta, onBack = { navController.popBackStack() }, onTerminada = { puntaje ->
                                val maximo = alimentosRuleta.size * 10
                                val porcentaje = if (maximo > 0) (puntaje * 100 / maximo) else 0
                                val estrellas = progreso.estrellasPorPorcentaje(porcentaje)
                                val totalMonedasAntes = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosAntes = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasAntes = progreso.estrellasMinijuego("ruleta")
                                if (estrellas > estrellasAntes) progreso.setEstrellasMinijuego("ruleta", estrellas)
                                progreso.setPuntajeMinijuego("ruleta", puntaje)
                                val totalMonedasDespues = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosDespues = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasDespues = progreso.estrellasMinijuego("ruleta")
                                val monedasGanadas = (totalMonedasDespues - totalMonedasAntes).coerceAtLeast(0)
                                val puntosGanados = (totalPuntosDespues - totalPuntosAntes).coerceAtLeast(0)
                                val estrellasGanadas = (estrellasDespues - maxOf(0, estrellasAntes)).coerceAtLeast(0)
                                versionProgreso++
                                navController.navigate("${NutriRoutes.PREMIO}/$porcentaje/$estrellasGanadas/$monedasGanadas/$puntosGanados") { popUpTo(NutriRoutes.JUEGO_RULETA) { inclusive = true } }
                            })
                        }
                        composable(NutriRoutes.JUEGO_MEMORIA) {
                            val memoriaAlimentos = preguntas.actividadesDelNivel(1).firstOrNull { it.tipo == "memoria" }
                            val memoriaDeporte = preguntas.actividadesDelNivel(7).firstOrNull { it.tipo == "une" }
                            val pares = remember {
                                val todas = (memoriaAlimentos?.let { ActividadMapper.memoria(it) } ?: emptyList()) +
                                        (memoriaDeporte?.let { ActividadMapper.une(it).map { u -> ParMemoria(u.emoji, u.texto, u.curiosidad) } } ?: emptyList())
                                todas.shuffled().take(8)
                            }
                            MemoriaNutritivaScreen(pares = pares, onBack = { navController.popBackStack() }, onTerminada = { puntaje ->
                                val maximo = pares.size * 10
                                val porcentaje = if (maximo > 0) (puntaje * 100 / maximo) else 0
                                val estrellas = progreso.estrellasPorPorcentaje(porcentaje)
                                val totalMonedasAntes = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosAntes = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasAntes = progreso.estrellasMinijuego("memoria")
                                if (estrellas > estrellasAntes) progreso.setEstrellasMinijuego("memoria", estrellas)
                                progreso.setPuntajeMinijuego("memoria", puntaje)
                                val totalMonedasDespues = progreso.monedasTotales(actividadesPorNivel)
                                val totalPuntosDespues = progreso.puntosTotales(actividadesPorNivel)
                                val estrellasDespues = progreso.estrellasMinijuego("memoria")
                                val monedasGanadas = (totalMonedasDespues - totalMonedasAntes).coerceAtLeast(0)
                                val puntosGanados = (totalPuntosDespues - totalPuntosAntes).coerceAtLeast(0)
                                val estrellasGanadas = (estrellasDespues - maxOf(0, estrellasAntes)).coerceAtLeast(0)
                                versionProgreso++
                                navController.navigate("${NutriRoutes.PREMIO}/$porcentaje/$estrellasGanadas/$monedasGanadas/$puntosGanados") { popUpTo(NutriRoutes.JUEGO_MEMORIA) { inclusive = true } }
                            })
                        }
                        composable("${NutriRoutes.PREMIO}/{porcentaje}/{estrellas}/{monedas}/{puntos}") { backStackEntry ->
                            val args = backStackEntry.arguments
                            PremioScreen(
                                porcentaje = args?.getString("porcentaje")?.toIntOrNull() ?: 100,
                                estrellas = args?.getString("estrellas")?.toIntOrNull() ?: 0,
                                monedas = args?.getString("monedas")?.toIntOrNull() ?: 0,
                                puntos = args?.getString("puntos")?.toIntOrNull() ?: 0,
                                onContinuar = { navController.popBackStack() }
                            )
                        }
                        dialog(NutriRoutes.RECOMPENSAS) {
                            val totalMonedas = remember(versionProgreso) { progreso.monedasTotales(actividadesPorNivel) }
                            val nivelesCompletados = (1..7).filter { progreso.nivelCompleto(it, preguntas.totalActividadesNivel(it)) }.toSet()
                            val minijuegosCompleto = listOf("arrastrar", "vf", "completa", "mejor", "ruleta", "memoria").all { progreso.minijuegoCompletado(it) }
                            val nutricionCompletado = (1..3).all { it in nivelesCompletados }
                            val deporteCompletado = (4..7).all { it in nivelesCompletados }
                            val medallas = CatalogoMedallas.conProgreso(nivelesCompletados, minijuegosCompleto)
                            RecompensasScreen(
                                monedas = totalMonedas, medallas = medallas,
                                canjeadas = remember(versionProgreso) { medallas.map { it.id }.filter { id -> progreso.recompensaCanjeada(id) }.toSet() },
                                onCanjear = { medalla ->
                                    val esGratis = (medalla.id == "frutas" && nutricionCompletado) || (medalla.id == "deportista" && deporteCompletado)
                                    if (esGratis) {
                                        SoundManager.medallaCanjeada()
                                        progreso.canjearRecompensa(medalla.id, 0)
                                        versionProgreso++
                                    } else {
                                        val costo = if (medalla.especial) 150 else 50
                                        if (totalMonedas >= costo) {
                                            SoundManager.medallaCanjeada()
                                            progreso.canjearRecompensa(medalla.id, costo)
                                            versionProgreso++
                                        }
                                    }
                                },
                                onCerrar = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }
}

private fun nivelActualTexto(progreso: ProgresoRepository, preguntas: PreguntasRepository, modulo: Int): String {
    val nombreModulo = if (modulo == 1) "Nutrición" else "Actividad física"
    val rango = if (modulo == 1) 1..3 else 4..7
    val nivelActual = rango.firstOrNull { nivel -> !progreso.nivelCompleto(nivel, preguntas.totalActividadesNivel(nivel)) } ?: rango.last
    return "Nivel $nivelActual · $nombreModulo"
}

private fun nivelActualGlobal(progreso: ProgresoRepository, preguntas: PreguntasRepository): Int {
    val nivel = (1..7).firstOrNull { nivel -> !progreso.nivelCompleto(nivel, preguntas.totalActividadesNivel(nivel)) } ?: 7
    return nivel
}
