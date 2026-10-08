"""
Genera el ícono de la app (adaptive icon + PNG legacy) y el ícono de 512 para Play.
Uso (desde la raíz del repo):  python3 tools/store/make_icons.py
Requiere: pip install playwright pillow && playwright install chromium
"""
import asyncio
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
from icon import variant_heart, variant_bubbles, svg, vector_drawable, background_drawable  # noqa: E402
from render import render_many  # noqa: E402

REPO = HERE.parents[1]
RES = REPO / "app" / "src" / "main" / "res"
FASTLANE = REPO / "fastlane"

ADAPTIVE = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
"""
DENSITIES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}


def crop(text, vb):
    return text.replace('viewBox="0 0 108 108"', f'viewBox="{vb}"')


def legacy(shapes, px, clip):
    s = crop(svg(shapes, px, "square"), "18 18 72 72")
    return s.replace("<defs>", f"<defs><clipPath id=\"r\">{clip}</clipPath>").replace("<g >", '<g clip-path="url(#r)">', 1)


def main():
    shapes, mono = variant_heart()
    for d in ["drawable", "drawable-v24", "mipmap-anydpi-v26"]:
        (RES / d).mkdir(parents=True, exist_ok=True)
    (RES / "drawable/ic_launcher_foreground.xml").write_text(vector_drawable(shapes))
    (RES / "drawable/ic_launcher_monochrome.xml").write_text(
        vector_drawable([(k, d, "#FFFFFFFF", e) for k, d, _, e in mono]))
    (RES / "drawable-v24/ic_launcher_background.xml").write_text(background_drawable())
    (RES / "mipmap-anydpi-v26/ic_launcher.xml").write_text(ADAPTIVE)
    (RES / "mipmap-anydpi-v26/ic_launcher_round.xml").write_text(ADAPTIVE)

    page = lambda s: f'<body style="margin:0;background:transparent">{s}</body>'  # noqa: E731
    jobs = []
    for name, px in DENSITIES.items():
        (RES / f"mipmap-{name}").mkdir(parents=True, exist_ok=True)
        jobs.append((page(legacy(shapes, px, '<rect x="18" y="18" width="72" height="72" rx="15" ry="15"/>')),
                     str(RES / f"mipmap-{name}/ic_launcher.png"), px, px))
        jobs.append((page(legacy(shapes, px, '<circle cx="54" cy="54" r="36"/>')),
                     str(RES / f"mipmap-{name}/ic_launcher_round.png"), px, px))
    out = HERE / "out"
    out.mkdir(exist_ok=True)
    jobs.append((page(crop(svg(shapes, 512, "square"), "14 14 80 80")), str(out / "play_icon_512.png"), 512, 512))
    b_shapes, _ = variant_bubbles()
    jobs.append((page(crop(svg(b_shapes, 512, "square"), "10 10 88 88")),
                 str(out / "play_icon_512_variante_globos.png"), 512, 512))
    asyncio.run(render_many(jobs))
    print("Íconos generados:", len(jobs))


if __name__ == "__main__":
    main()
