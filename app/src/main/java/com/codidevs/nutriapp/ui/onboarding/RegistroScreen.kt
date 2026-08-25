package com.codidevs.nutriapp.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codidevs.nutriapp.ui.components.ScreenHeader
import com.codidevs.nutriapp.ui.theme.*

@Composable
fun RegistroScreen(
    onBack: () -> Unit,
    onVerPrivacidad: () -> Unit,
    onContinuar: (nombre: String, edad: String, peso: String, estatura: String, sexo: String) -> Unit
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var edad by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var estatura by rememberSaveable { mutableStateOf("") }
    var sexo by rememberSaveable { mutableStateOf("niño") }
    var aceptaPrivacidad by rememberSaveable { mutableStateOf(false) }

    var errorNombre by rememberSaveable { mutableStateOf<String?>(null) }
    var errorEdad by rememberSaveable { mutableStateOf<String?>(null) }
    var errorPeso by rememberSaveable { mutableStateOf<String?>(null) }
    var errorEstatura by rememberSaveable { mutableStateOf<String?>(null) }
    var errorAcepta by rememberSaveable { mutableStateOf<String?>(null) }

    fun validarYContinuar() {
        errorNombre = if (nombre.isBlank()) "Escribe el nombre de tu hijo" else null
        errorEdad = when {
            edad.isBlank() -> "Escribe la edad"
            edad.toIntOrNull() == null -> "Solo números"
            edad.toInt() !in 6..12 -> "Debe ser entre 6 y 12 años"
            else -> null
        }
        errorPeso = when {
            peso.isBlank() -> "Escribe el peso"
            peso.toDoubleOrNull() == null -> "Solo números"
            peso.toDouble() <= 0 -> "Peso no válido"
            else -> null
        }
        errorEstatura = when {
            estatura.isBlank() -> "Escribe la estatura"
            estatura.toDoubleOrNull() == null -> "Solo números"
            estatura.toDouble() <= 0 -> "Estatura no válida"
            else -> null
        }
        errorAcepta = if (!aceptaPrivacidad) "Acepta la política de privacidad" else null
        
        val ok = errorNombre == null && errorEdad == null && errorPeso == null &&
                errorEstatura == null && errorAcepta == null
        
        if (ok) {
            onContinuar(nombre.trim(), edad.trim(), peso.trim(), estatura.trim(), sexo)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgApp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        ScreenHeader(titulo = "Datos de tu hijo", onBack = onBack)
        Spacer(Modifier.height(8.dp))
        Text(text = "Tú registras el perfil. Luego pueden jugar juntos.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, placeholder = { Text("Ej. Sofía") },
            singleLine = true, isError = errorNombre != null, supportingText = { errorNombre?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Leaf, unfocusedBorderColor = LineColor, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = edad, onValueChange = { if (it.length <= 2) edad = it.filter(Char::isDigit) }, label = { Text("Edad (años)") },
            singleLine = true, isError = errorEdad != null, supportingText = { errorEdad?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Leaf, unfocusedBorderColor = LineColor, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = peso, onValueChange = { if (it.length <= 5) peso = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Peso (kg)") }, placeholder = { Text("Ej. 32.5") },
            singleLine = true, isError = errorPeso != null, supportingText = { errorPeso?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Leaf, unfocusedBorderColor = LineColor, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = estatura, onValueChange = { if (it.length <= 5) estatura = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Estatura (cm)") }, placeholder = { Text("Ej. 135") },
            singleLine = true, isError = errorEstatura != null, supportingText = { errorEstatura?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Leaf, unfocusedBorderColor = LineColor, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        Text(text = "Sexo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val esBoy = sexo == "niño"
            Surface(onClick = com.codidevs.nutriapp.data.audio.onClickConSonido { sexo = "niño" }, color = if (esBoy) Mango else Color.White, shape = RoundedCornerShape(16.dp), border = BorderStroke(2.dp, if (esBoy) Mango else LineColor), modifier = Modifier.weight(1f).height(52.dp)) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = "👦 Niño", style = MaterialTheme.typography.labelLarge, color = if (esBoy) Color.White else Ink)
                }
            }
            val esGirl = sexo == "niña"
            Surface(onClick = com.codidevs.nutriapp.data.audio.onClickConSonido { sexo = "niña" }, color = if (esGirl) Mango else Color.White, shape = RoundedCornerShape(16.dp), border = BorderStroke(2.dp, if (esGirl) Mango else LineColor), modifier = Modifier.weight(1f).height(52.dp)) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Text(text = "👧 Niña", style = MaterialTheme.typography.labelLarge, color = if (esGirl) Color.White else Ink)
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = aceptaPrivacidad, onCheckedChange = { aceptaPrivacidad = it; errorAcepta = null }, colors = CheckboxDefaults.colors(checkedColor = Leaf))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Acepto la política de privacidad.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Leer política de privacidad", style = MaterialTheme.typography.bodyMedium, color = LeafDark, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable(onClick = com.codidevs.nutriapp.data.audio.onClickConSonido { onVerPrivacidad() }))
            }
        }
        errorAcepta?.let { Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)) }
        Spacer(Modifier.height(32.dp))
        Button(onClick = com.codidevs.nutriapp.data.audio.onClickConSonido { validarYContinuar() }, colors = ButtonDefaults.buttonColors(containerColor = Leaf), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Continuar", style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
        Spacer(Modifier.height(24.dp))
    }
}
