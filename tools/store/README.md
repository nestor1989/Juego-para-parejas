# Generadores de recursos de Play Store

- `make_icons.py`: ícono de la app (adaptive icon con capa monocromática + PNG para Android 6-7)
  y el ícono de 512 px para Play, a partir de la geometría de `icon.py`.
- `compose.py`: las 8 capturas (1080 × 1920) por idioma y el gráfico destacado (1024 × 500).
  Las pantallas son réplicas HTML de la UI Compose (`screens.py`) con los textos reales de
  `strings.xml` y `packs.json`. Salen en `tools/store/out/<idioma>/`.

```
pip install playwright pillow && playwright install chromium
python3 tools/store/make_icons.py
python3 tools/store/compose.py all      # o: es | pt | en | feature | result ...
```

Cuando la UI cambie, conviene actualizar `screens.py` o reemplazar las capturas por capturas reales
del teléfono: Google pide que las capturas reflejen la experiencia real de la app.
