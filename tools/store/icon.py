"""
Ícono de Juego para Parejas: geometría única (canvas 108x108 de adaptive icon)
exportada a SVG (para renderizar PNG) y a VectorDrawable (para la app).
Zona segura: todo el contenido dentro de un círculo de radio 33 centrado en (54,54).
"""
import math

PINK = "#E01B58"
PINK_LIGHT = "#F2386F"
PURPLE = "#7D41D3"
WHITE = "#FFFFFF"
LILAC = "#EBDDFF"

BG_START = "#F2386F"
BG_END = "#7D41D3"


def rounded_rect(x0, y0, x1, y1, r, tail=None):
    """Rect redondeado; tail = ('bl'|'br', base_a, base_b, tip_x, tip_y) en el borde inferior."""
    p = [f"M{x0 + r},{y0}", f"H{x1 - r}", f"A{r},{r} 0 0 1 {x1},{y0 + r}", f"V{y1 - r}",
         f"A{r},{r} 0 0 1 {x1 - r},{y1}"]
    if tail:
        _, a, b, tx, ty = tail  # a > b, recorrido de derecha a izquierda por el borde inferior
        p += [f"H{a}", f"L{tx},{ty}", f"L{b},{y1}"]
    p += [f"H{x0 + r}", f"A{r},{r} 0 0 1 {x0},{y1 - r}", f"V{y0 + r}", f"A{r},{r} 0 0 1 {x0 + r},{y0}", "Z"]
    return " ".join(p)


def heart(cx, top, w, h):
    """Corazón centrado en cx, de ancho w y alto h, empezando en y=top."""
    s = w / 56.0
    t = h / 50.0
    def X(v): return round(cx + (v - 54) * s, 2)
    def Y(v): return round(top + (v - 30) * t, 2)
    return (f"M{X(54)},{Y(80)} C{X(54)},{Y(80)} {X(26)},{Y(62)} {X(26)},{Y(45)} "
            f"C{X(26)},{Y(36)} {X(33)},{Y(30)} {X(41)},{Y(30)} C{X(47)},{Y(30)} {X(51.5)},{Y(33.5)} {X(54)},{Y(38)} "
            f"C{X(56.5)},{Y(33.5)} {X(61)},{Y(30)} {X(67)},{Y(30)} C{X(75)},{Y(30)} {X(82)},{Y(36)} {X(82)},{Y(45)} "
            f"C{X(82)},{Y(62)} {X(54)},{Y(80)} {X(54)},{Y(80)} Z")


def question(cx, top, size):
    """Signo de pregunta: trazo + punto. size = alto total."""
    k = size / 26.0
    def X(v): return round(cx + v * k, 2)
    def Y(v): return round(top + v * k, 2)
    stroke = (f"M{X(-7.5)},{Y(7.5)} C{X(-7.5)},{Y(2.5)} {X(-3.8)},{Y(0)} {X(0)},{Y(0)} "
              f"C{X(4.2)},{Y(0)} {X(7.5)},{Y(2.8)} {X(7.5)},{Y(7)} C{X(7.5)},{Y(11)} {X(4.2)},{Y(12.5)} {X(2)},{Y(14)} "
              f"C{X(0.5)},{Y(15.2)} {X(0)},{Y(16.5)} {X(0)},{Y(19)}")
    dot_r = round(2.9 * k, 2)
    dot = (X(0), Y(24.2), dot_r)
    return stroke, round(5.0 * k, 2), dot


def circle_path(cx, cy, r):
    return f"M{cx - r},{cy} A{r},{r} 0 1 0 {cx + r},{cy} A{r},{r} 0 1 0 {cx - r},{cy} Z"


# ---------------------------------------------------------------- variantes

def variant_heart():
    """A: corazón blanco con signo de pregunta."""
    shapes = [("fill", heart(54, 28.5, 57, 51.5), WHITE, 1.0)]
    q, sw, (dx, dy, dr) = question(53.4, 41.5, 22.5)
    shapes.append(("stroke", q, PINK, sw))
    shapes.append(("fill", circle_path(dx, dy, dr), PINK, 1.0))
    mq, msw, mdot = question(53.4, 43, 18)
    mono = [("stroke", heart(54, 30, 54, 49), "#000000", 4.2),
            ("stroke", mq, "#000000", msw),
            ("fill", circle_path(*mdot), "#000000", 1.0)]
    return shapes, mono


def variant_bubbles():
    """B: dos globos de diálogo, uno con corazón y otro con signo de pregunta."""
    back = rounded_rect(48, 26, 83, 55, 9, tail=("br", 74, 65, 80, 63))
    front = rounded_rect(24, 39, 67, 71, 10, tail=("bl", 45, 36, 32, 79))
    shapes = [("fill", back, LILAC, 1.0)]
    q, sw, (dx, dy, dr) = question(75.5, 30.5, 17)
    shapes.append(("stroke", q, PURPLE, sw))
    shapes.append(("fill", circle_path(dx, dy, dr), PURPLE, 1.0))
    shapes.append(("fill", front, WHITE, 1.0))
    shapes.append(("fill", heart(45.5, 45.5, 27, 24), PINK, 1.0))
    mono = [("stroke", back, "#000000", 3.5), ("fill", front, "#000000", 1.0)]
    return shapes, mono


VARIANTS = {"heart": variant_heart, "bubbles": variant_bubbles}


# ---------------------------------------------------------------- exportadores

def svg(shapes, size=512, shape="square", background=True, fg_scale=1.0):
    """Ícono completo en SVG. shape: square (Play), rounded (legacy), circle (round)."""
    clip = ""
    if shape == "rounded":
        clip = '<clipPath id="c"><rect x="0" y="0" width="108" height="108" rx="22" ry="22"/></clipPath>'
    elif shape == "circle":
        clip = '<clipPath id="c"><circle cx="54" cy="54" r="54"/></clipPath>'
    body = []
    if background:
        body.append('<rect x="0" y="0" width="108" height="108" fill="url(#g)"/>')
    tf = f'transform="translate({54 - 54 * fg_scale},{54 - 54 * fg_scale}) scale({fg_scale})"'
    body.append(f"<g {tf}>")
    for kind, d, color, extra in shapes:
        if kind == "fill":
            body.append(f'<path d="{d}" fill="{color}" fill-opacity="{extra}"/>')
        else:
            body.append(f'<path d="{d}" fill="none" stroke="{color}" stroke-width="{extra}" '
                        f'stroke-linecap="round" stroke-linejoin="round"/>')
    body.append("</g>")
    clip_attr = 'clip-path="url(#c)"' if clip else ""
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 108 108">'
            f'<defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">'
            f'<stop offset="0" stop-color="{BG_START}"/><stop offset="1" stop-color="{BG_END}"/></linearGradient>{clip}</defs>'
            f'<g {clip_attr}>{"".join(body)}</g></svg>')


def vector_drawable(shapes):
    out = ['<?xml version="1.0" encoding="utf-8"?>',
           '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
           '    android:width="108dp"', '    android:height="108dp"',
           '    android:viewportWidth="108"', '    android:viewportHeight="108">']
    for kind, d, color, extra in shapes:
        if kind == "fill":
            alpha = "" if extra == 1.0 else f'\n        android:fillAlpha="{extra}"'
            out.append(f'    <path\n        android:fillColor="{color}"{alpha}\n        android:pathData="{d}" />')
        else:
            out.append(f'    <path\n        android:fillColor="#00000000"\n        android:strokeColor="{color}"\n'
                       f'        android:strokeWidth="{extra}"\n        android:strokeLineCap="round"\n'
                       f'        android:strokeLineJoin="round"\n        android:pathData="{d}" />')
    out.append("</vector>")
    return "\n".join(out) + "\n"


def background_drawable():
    return f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="linear"
                android:startX="0"
                android:startY="0"
                android:endX="108"
                android:endY="108">
                <item android:offset="0" android:color="{BG_START}" />
                <item android:offset="1" android:color="{BG_END}" />
            </gradient>
        </aapt:attr>
    </path>
</vector>
'''
