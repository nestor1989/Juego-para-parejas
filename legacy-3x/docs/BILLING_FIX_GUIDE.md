# Billing & Ads Fix Summary

## Diagnóstico REAL (Agosto 2026)

El problema **NO estaba en Google Play Console**. La app ya estaba:
- ✓ Firmada correctamente
- ✓ En producción
- ✓ Con los SKUs `premium_monthly` y `premium_yearly` en estado **Active**

Los dos síntomas (error de billing + ads que no aparecían) venían de **bugs en el código** de `SubscriptionManager.kt`.

---

## Certificate Information (referencia)
- **Upload key SHA1**: `28:31:50:0E:19:DA:F1:89:53:91:CE:5F:9A:0D:07:21:27:12:21:5B`
- **App signing key SHA1** (la gestiona Google Play): `F5:FB:D4:7C:73:94:3B:C7:F2:AB:62:5E:8E:FC:70:3C:6E:4A:37:39`
- **Keystore Alias**: `ideastudio`

> Tener DOS claves (upload + app signing) es NORMAL con Play App Signing. No es la causa del error.

---

## Bug 1 — Diálogo "not configured for billing through Google Play"

**Causa:** `SubscriptionManager` tenía un fallback al SKU de prueba estático
`android.test.purchased`. Cuando la consulta del SKU real fallaba un instante,
saltaba a ese SKU de test. En una app de producción firmada, `android.test.purchased`
produce **exactamente** el diálogo:

```
This version of the application is not configured for
billing through Google Play.
```

**Fix:** Se eliminó por completo el fallback al SKU de test y la simulación de compras.
Ahora `queryAndLaunchBillingFlow` solo usa los SKUs reales y, si fallan, reporta el
error sin lanzar el diálogo confuso.

---

## Bug 2 — Los anuncios (ads) no se mostraban

**Causa:** La antigua función `simulateSuccessfulPurchase()` guardaba
`is_premium = true` de forma **permanente** (sin fecha de expiración) en
`SharedPreferences`. Como `isSubscriptionExpired()` devuelve `false` cuando no hay
expiración, la app consideraba al usuario premium para siempre y ocultaba todos los
banners (`binding.banner.visibility = View.GONE`).

**Fix:** `verifySubscriptionStatus()` ahora, si Google Play no reporta ninguna
suscripción real activa, **resetea `is_premium = false`** automáticamente. Esto limpia
el estado obsoleto en cualquier dispositivo al abrir la app, restaurando los anuncios.

---

## Cambios de código aplicados

### `SubscriptionManager.kt`
- Eliminadas las constantes `TEST_SKU_MONTHLY` / `TEST_SKU_YEARLY`.
- Eliminado el fallback al SKU de test en `queryAndLaunchBillingFlow`.
- `simulateSuccessfulPurchase` neutralizada (ya no marca premium).
- `verifySubscriptionStatus` resetea premium si no hay suscripción real activa.

### `app/build.gradle`
- `versionCode 11 → 12`, `versionName 2.1 → 2.2`.
- Removida la config de signing temporal (se firma desde Android Studio con `ideastudio`).

---

## Cómo publicar

1. Android Studio → **Build → Generate Signed Bundle / APK → Android App Bundle → Release**
   (keystore `ideastudio`).
2. Subir el nuevo AAB (versionCode 12) a **Production** en Google Play Console.
3. Esperar la revisión/publicación.

## Resultado esperado

- **Billing:** "Suscribirse" abre el diálogo real de Google Play (SKUs Active).
- **Ads:** al abrir la app se resetea el flag premium falso y los banners vuelven.

---

## Configuración actual de la app
- **Package**: com.idea3d.juegoparaparejas
- **Billing Library**: 7.1.1
- **BILLING Permission**: Declarado
- **SKUs**: premium_monthly, premium_yearly (Active)

## Debug
Filtrar Logcat por `SubscriptionManager`. También podés llamar:
```kotlin
subscriptionManager.printBillingStatus()
```
