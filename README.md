# Juego para Parejas 4.x (Compose)

Reescritura completa de la app. Mismo `applicationId` (`com.idea3d.juegoparaparejas`), así que se
publica como actualización de la ficha actual. La versión está en `app/build.gradle`.

El código de la 3.x se borró del repo; si hace falta consultarlo, está en el historial de git
(commit `4.1.0` o anteriores, carpeta `legacy-3x/`).

## Cómo probarla

1. Abrí la carpeta del proyecto en Android Studio como siempre y dejá que sincronice Gradle.
   Si queda algo raro del módulo viejo: **File › Invalidate Caches › Invalidate and Restart**.
2. Si tenés instalada la versión de Play en el teléfono, **desinstalala primero**: la build debug
   tiene otra firma y no se puede instalar encima.
3. **Run ▶ app**. En debug se usan los anuncios de prueba de Google.
4. **Mismo celular:** "Jugar en este celular" → nombres → pack → responder → pasar → adivinar → resultado.
5. **A distancia** (antes de publicar la página web el link abre el navegador, así que hay que forzarlo):
   - Teléfono A: "Jugar a distancia" → responder las 10 preguntas → **Copiar link**.
   - Teléfono B (o el mismo) con la app instalada, desde la compu:
     `adb shell "am start -a android.intent.action.VIEW -d '<link copiado>' com.idea3d.juegoparaparejas"`
   - Alternativa sin adb: en el teléfono B, Ajustes › Apps › Juego para Parejas › Abrir de forma
     predeterminada › Agregar vínculo › activar `juego-para-parejas-3ea2c.web.app`.
   - Al terminar de adivinar, "Enviar mi resultado" genera otro link: abrilo igual en el teléfono A
     para ver el resultado del lado de quien desafió.
6. **Compras:** solo funcionan con la app subida a un track de Play. Subí el AAB a **Prueba interna**,
   agregá tu cuenta en **Configuración › Pruebas de licencia** y creá el producto `premium_lifetime`.
7. **Tests:** `./gradlew testDebugUnitTest` (codec de desafíos, links, motor y `packs.json`).
8. **Consentimiento (UMP):** en debug se puede simular la región agregando a `local.properties`
   (no se sube a git) y volviendo a compilar:
   ```
   ump.debugGeography=EEA        # EEA, US_STATE u OTHER; vacío = ubicación real
   ump.resetOnLaunch=true        # borra el consentimiento guardado en cada arranque
   ump.testDeviceId=XXXXXXXX     # solo en un teléfono físico: el hash que imprime Logcat
   ```
   - `EEA`: al abrir aparece el formulario de GDPR; después, en el inicio, "Opciones de privacidad".
   - `US_STATE`: no aparece nada al abrir; el mensaje de estados de EE. UU. se abre desde
     "Opciones de privacidad".
   - En un teléfono físico, la primera vez buscá en Logcat `addTestDeviceHashedId` y copiá el hash.
     Los emuladores ya cuentan como dispositivo de prueba.

## Qué cambia

- Kotlin + Jetpack Compose, una sola Activity (antes 10 Activities con XML).
- Preguntas en `app/src/main/assets/packs.json` (es / en / pt). 15 packs, 195 preguntas:
  10 de etapas de la relación (migrados y corregidos), 2 de amigos y 3 picantes (nuevos).
- 10 preguntas al azar por ronda, repaso pregunta por pregunta al final, cambio de roles.
- **Modo a distancia** por link, sin backend; Install Referrer para abrir el desafío después de instalar.
- **Monetización:** compra única `premium_lifetime`, rewarded para jugar un pack premium una vez,
  intersticial al salir del resultado (desde la 2.ª ronda, mínimo 90 s entre anuncios), sin banners.
  Si un anuncio no carga se reintenta con espera creciente y queda el evento `ad_load_failed` en
  Analytics. Bloques en AdMob: `rewarded_adults` (…/3093275577) e `Inters 12Resp` (…/6385055420).
  `premium_monthly` / `premium_yearly` se siguen reconociendo como premium.
- Consentimiento con UMP (GDPR y estados de EE. UU.): formulario al abrir cuando corresponde y
  "Opciones de privacidad" + "Política de privacidad" siempre en el inicio, también con Premium.
- Pedido de reseña in-app y eventos de Firebase Analytics.
- Los packs adultos 11-16 de la 3.x no se migraron (texto explícito y preguntas con edades de menores).

## Antes de publicar

1. Play Console › Productos integrados: crear y activar `premium_lifetime`. En Suscripciones,
   desactivar los planes base viejos para compras nuevas.
2. **Links de desafío en Firebase Hosting** (proyecto `juego-para-parejas-3ea2c`, dominio
   `juego-para-parejas-3ea2c.web.app`, definido en `CHALLENGE_HOST` de `gradle.properties`):
   - En `web/.well-known/assetlinks.json`, reemplazá las dos huellas por las SHA-256 de
     Play Console › Prueba y publicación › Integridad de la app (clave de firma de la app y clave de subida).
   - Si es la primera vez: Firebase Console › Hosting › Comenzar.
   - `npm install -g firebase-tools`, `firebase login` y `firebase deploy --only hosting` desde la raíz.
   - Verificá que abran `https://juego-para-parejas-3ea2c.web.app/.well-known/assetlinks.json` y
     `https://juego-para-parejas-3ea2c.web.app/j/`.
   - Opcional: `web/j/og.png` (1200 × 630) para la vista previa en WhatsApp.
3. **Política de privacidad**: la app abre `https://juego-para-parejas-3ea2c.web.app/privacidad/`
   (`PRIVACY_POLICY_URL` en `app/build.gradle`). Tiene que estar en `web/privacidad/index.html` y
   publicada con el deploy de Hosting; poné la misma URL en Play Console › Política de privacidad.
   (Los mensajes de GDPR y de estados de EE. UU. ya están publicados en AdMob.)
4. Play Console › Seguridad de los datos: declarar ID de dispositivo/publicidad, ubicación aproximada,
   interacciones, diagnósticos y compras (publicidad, estadísticas, prevención de fraude).
5. Ficha nueva: `play-store/ficha-play-store.txt` + imágenes de `play-store/`.

## Ficha de Play Store

Todo en `play-store/`: `ficha-play-store.txt` tiene títulos, descripciones breves y completas de los
4 idiomas (es-419, es-ES, pt-BR, en-US) y las notas de la versión; `icono.png` (512) y una carpeta por
idioma con el gráfico destacado y las 8 capturas (es-ES usa las de es-419).
Los generadores del ícono y de las capturas están en `tools/store/`.

## Contenido

`app/src/main/assets/packs.json` es la fuente de verdad: para sumar un pack, agregalo al JSON con `id` nuevo.
**No reutilices ni renumeres los `id` de preguntas**: los links de desafío guardan esos ids.
