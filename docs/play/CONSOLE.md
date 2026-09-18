# Play Console — NutriApp

App **para padres y madres**. El hijo (4–14) puede jugar junto a ellos. Offline. Paquete `com.codidevs.nutriapp`.

No marcar Families / Designed for children. El usuario de la ficha es el adulto.

## Antes de subir

1. Activar GitHub Pages: repo → Settings → Pages → Deploy from branch `main` → folder `/docs`.
2. URL de privacidad:
   `https://codidevs.github.io/nutriapp-android/legal/privacidad.html`
3. Generar upload key (no commitear el `.jks`):

```
keytool -genkey -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```

Validez debe pasar del 22 oct 2033. Copiar `keystore.properties.example` → `keystore.properties` y rellenar. Correr `build-aab.bat`. Subir `app/build/outputs/bundle/release/app-release.aab`. Enrolar Play App Signing.

## Ficha

**Nombre:** NutriApp

**Descripción corta (≤80):**
Juega con tu hijo hábitos de alimentación y movimiento. Para padres y madres.

**Descripción completa:**

NutriApp es un juego educativo para padres y madres. Un sendero de niveles, minijuegos y recompensas para jugar juntos hábitos de alimentación y movimiento.

El padre o la madre registra el perfil del hijo (nombre, edad, peso y estatura). El indicador de IMC es orientativo.

NutriApp no es un dispositivo médico y no diagnostica, trata ni previene ninguna condición. Consulta a un profesional de la salud.

Los datos se quedan en el teléfono. No hay anuncios, no hay cuenta en la nube y no hay Internet.


**Categoría:** Educación o Salud y bienestar.

**Sitio web / privacidad:** la URL de GitHub Pages de arriba.

## Assets

- Icono 512×512: `docs/play/assets/icon-512.png`
- Feature graphic 1024×500: `docs/play/assets/feature-graphic.png`
- Screenshots (tú los capturas en emulador, mínimo 2, mejor 4):
  1. Splash
  2. Registro (datos de tu hijo)
  3. Home
  4. Sendero

No uses capturas inventadas ni del video HyperFrames como ficha.

## Target audience

- Designed for children: **no**
- Age: **18 and over** (el usuario es el padre o la madre)
- No Families program
- Ads: **no**
- IAP: **no**

Si Play pregunta si hay contenido para menores: el adulto juega con el hijo. No declares la app como app de niños.

## Data safety

- Recopila datos: **no se envían fuera del dispositivo**
- En el teléfono: nombre, edad, peso, estatura, progreso de juego (los introduce el padre)
- Shared: **no**
- Encrypted in transit: N/A (no red)
- Users can request deletion: sí, in-app “Crear otro registro” o desinstalar
- Account: **no hay cuenta cloud**
- Privacy policy URL: la de GitHub Pages

## Health apps

- App content → Health: **sí** (IMC, peso, estatura)
- No es dispositivo médico
- Pegar el disclaimer de la descripción completa

## Content rating (IARC)

Cuestionario: usuario adulto, contenido educativo, sin violencia, sin sexo, sin compras, sin ubicación.

## Closed testing

Si la cuenta de desarrollador es **personal** y se creó **después del 13 nov 2023**: 12 testers opted-in continuos durante 14 días, luego Apply for production.

## Checklist rápido

- [ ] Pages publicado y URL abre
- [ ] AAB firmado (no APK debug)
- [ ] Play App Signing
- [ ] Icon 512 + feature graphic + ≥2 screenshots
- [ ] Descripción con disclaimer médico
- [ ] Data safety
- [ ] Health declaration
- [ ] Target audience = 18+ (no Families)
- [ ] Content rating
- [ ] Closed testing si aplica
