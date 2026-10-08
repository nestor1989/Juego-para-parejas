"""
Réplicas HTML de las pantallas Compose de la 4.0 (mismos textos, colores y medidas en dp)
para generar las capturas de Google Play.
"""
import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

import icon

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1] / "app" / "src" / "main"
FONTS = HERE / "fonts"

PINK = "#E01B58"
PURPLE = "#7D41D3"
PAPER = "#FFF8F6"
INK = "#2B1220"
PRIMARY_CONTAINER = "#FDE7EE"
ON_PRIMARY_CONTAINER = "#7A1238"
ON_SURFACE_VARIANT = "#6B4F5B"
OUTLINE = "#E3CBD3"
CORRECT = "#1E8F6B"
WRONG = "#D64545"
GOLD = "#D4A24C"

RES_DIR = {"es": "values-es", "pt": "values-pt", "en": "values"}
NAMES = {"es": ("Sofi", "Martín"), "pt": ("Ana", "Lucas"), "en": ("Emma", "Jake")}


def strings(lang):
    tree = ET.parse(ROOT / "res" / RES_DIR[lang] / "strings.xml")
    out = {}
    for el in tree.getroot():
        text = (el.text or "").replace("\\'", "'").replace('\\"', '"')
        out[el.get("name")] = text
    return out


def fmt(template, *args):
    """Convierte %1$s / %1$d / %s / %d / %% al estilo de Android."""
    s = template.replace("%%", "\u0000")
    seq = iter(args)
    def repl(m):
        if m.group(1):
            return str(args[int(m.group(1)) - 1])
        return str(next(seq))
    s = re.sub(r"%(?:(\d+)\$)?[sd]", repl, s)
    return s.replace("\u0000", "%")


PACKS = {p["id"]: p for p in json.load(open(ROOT / "assets" / "packs.json"))["packs"]}


def esc(s):
    return (s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))


def font_faces():
    return f"""
@font-face {{ font-family: 'Roboto'; font-weight: 400; src: url('file://{FONTS}/Roboto-Regular.ttf'); }}
@font-face {{ font-family: 'Roboto'; font-weight: 500; src: url('file://{FONTS}/Roboto-Medium.ttf'); }}
@font-face {{ font-family: 'Roboto'; font-weight: 600; src: url('file://{FONTS}/Roboto-Medium.ttf'); }}
@font-face {{ font-family: 'Roboto'; font-weight: 700; src: url('file://{FONTS}/Roboto-Bold.ttf'); }}
@font-face {{ font-family: 'Roboto'; font-weight: 900; src: url('file://{FONTS}/Roboto-Black.ttf'); }}
@font-face {{ font-family: 'Aladin'; src: url('file://{FONTS}/aladin.ttf'); }}
"""


APP_CSS = f"""
.scr {{ width: 412px; height: 892px; position: relative; overflow: hidden; background: {PAPER};
       font-family: 'Roboto', 'Noto Color Emoji', sans-serif; color: {INK}; }}
.scr * {{ box-sizing: border-box; }}
.status {{ height: 32px; display: flex; align-items: center; justify-content: space-between;
          padding: 0 22px; font-size: 14px; font-weight: 500; }}
.status.light {{ color: #fff; }}
.navbar {{ position: absolute; bottom: 8px; left: 50%; width: 110px; height: 4px; margin-left: -55px;
          border-radius: 2px; background: rgba(0,0,0,.55); }}
.navbar.light {{ background: rgba(255,255,255,.8); }}
.hero {{ background: linear-gradient(135deg, {PINK}, {PURPLE}); }}
.btn {{ width: 100%; min-height: 56px; border-radius: 28px; display: flex; align-items: center;
       justify-content: center; font-size: 17px; font-weight: 600; text-align: center; padding: 14px 24px; }}
.btn.primary {{ background: {PINK}; color: #fff; }}
.btn.white {{ background: #fff; color: {PINK}; }}
.btn.outline {{ min-height: 52px; border: 1.5px solid {PINK}; color: {PINK}; font-size: 16px; font-weight: 500; }}
.btn.outline.white {{ background: transparent; border-color: #fff; color: #fff; }}
.tbtn {{ font-size: 14px; font-weight: 500; color: {PINK}; padding: 10px 12px; text-align: center; }}
.header {{ display: flex; align-items: center; padding: 8px 8px; }}
.header .back {{ color: {PINK}; font-size: 16px; font-weight: 500; padding: 10px 12px; }}
.header .title {{ font-size: 22px; font-weight: 600; margin-left: 4px; }}
.card {{ background: #fff; border: 1px solid rgba(227,203,211,.6); border-radius: 22px; padding: 16px; }}
.chip {{ display: inline-block; border-radius: 999px; padding: 4px 10px; font-size: 12px; font-weight: 600; }}
.option {{ background: #fff; border: 1px solid {OUTLINE}; border-radius: 20px; min-height: 64px; padding: 16px 20px;
          display: flex; align-items: center; justify-content: center; text-align: center; font-size: 17px;
          font-weight: 500; box-shadow: 0 1px 2px rgba(0,0,0,.12); }}
.option.correct {{ background: {CORRECT}; color: #fff; border-color: transparent; box-shadow: none; }}
.option.wrong {{ background: {WRONG}; color: #fff; border-color: transparent; box-shadow: none; }}
.option.dim {{ background: rgba(255,255,255,.5); color: rgba(43,18,32,.5); border-color: transparent; box-shadow: none; }}
"""


def status_bar(light=False):
    color = "#fff" if light else INK
    icons = (f'<svg width="62" height="14" viewBox="0 0 62 14" fill="{color}">'
             f'<path d="M10 13 L1.5 4.2 A12 12 0 0 1 18.5 4.2 Z"/>'
             f'<path d="M24 12 L34 12 L34 2 Z"/>'
             f'<rect x="42" y="2" width="16" height="10" rx="2" fill="none" stroke="{color}" stroke-width="1.5"/>'
             f'<rect x="44" y="4" width="11" height="6" rx="1"/><rect x="58.5" y="5" width="2" height="4" rx="1"/></svg>')
    return f'<div class="status {"light" if light else ""}"><span>9:41</span>{icons}</div>'


def screen(body, light=False, bg=None):
    style = f' style="background:{bg}"' if bg else ""
    return (f'<div class="scr{" hero" if light and not bg else ""}"{style}>{status_bar(light)}{body}'
            f'<div class="navbar {"light" if light else ""}"></div></div>')


# ------------------------------------------------------------------ pantallas

def logo_svg(size):
    shapes, _ = icon.variant_heart()
    return icon.svg(shapes, size, "square", background=False)


def home(S):
    body = f"""
    <div style="padding: 16px 24px; display:flex; flex-direction:column; align-items:center; color:#fff">
      <div style="height:40px"></div>
      <div style="width:112px;height:112px;border-radius:50%;background:rgba(255,255,255,.18);
                  display:flex;align-items:center;justify-content:center">{logo_svg(112)}</div>
      <div style="height:24px"></div>
      <div style="font-family:Aladin;font-size:40px;line-height:46px;text-align:center">{esc(S['home_title'])}</div>
      <div style="height:8px"></div>
      <div style="font-size:17px;opacity:.9;text-align:center;line-height:24px">{esc(S['home_subtitle'])}</div>
      <div style="height:56px"></div>
      <div class="btn white">{esc(S['home_play_local'])}</div>
      <div style="font-size:13px;opacity:.85;margin:6px 0 16px">{esc(S['home_play_local_hint'])}</div>
      <div class="btn outline white">{esc(S['home_play_remote'])}</div>
      <div style="font-size:13px;opacity:.85;margin:6px 0 12px">{esc(S['home_play_remote_hint'])}</div>
      <div style="display:flex;justify-content:center">
        <div class="tbtn" style="color:#fff">{esc(S['home_how_to'])}</div>
        <div class="tbtn" style="color:#fff;font-weight:600">★ {esc(S['home_premium'])}</div>
      </div>
    </div>"""
    return screen(body, light=True)


def pack_card(pack, lang, S, locked=False):
    chip = (f'<span class="chip" style="background:rgba(212,162,76,.2);color:{INK}">🔒 {esc(S["pack_locked"])}</span>'
            if locked else "")
    n = min(len(pack["questions"]), 10)
    return f"""
    <div class="card" style="flex:1;height:150px;display:flex;flex-direction:column;gap:6px">
      <div style="display:flex;justify-content:space-between;align-items:flex-start">
        <span style="font-size:30px;line-height:36px">{pack['emoji']}</span>{chip}</div>
      <div style="font-size:16px;font-weight:700;line-height:22px">{esc(pack['name'][lang])}</div>
      <div style="font-size:12px;line-height:16px;color:{ON_SURFACE_VARIANT}">{esc(pack['subtitle'][lang])}</div>
      <div style="flex:1"></div>
      <div style="font-size:11px;font-weight:500;color:{ON_SURFACE_VARIANT}">{esc(fmt(S['pack_questions'], n))}</div>
    </div>"""


def packs_screen(S, lang, sections, locked_ids=(), overlay=""):
    rows = []
    for title, ids in sections:
        rows.append(f'<div style="font-size:16px;font-weight:700;color:{PINK};padding:12px 4px 0">{esc(title)}</div>')
        for i in range(0, len(ids), 2):
            pair = ids[i:i + 2]
            cards = "".join(pack_card(PACKS[pid], lang, S, pid in locked_ids) for pid in pair)
            if len(pair) == 1:
                cards += '<div style="flex:1"></div>'
            rows.append(f'<div style="display:flex;gap:12px">{cards}</div>')
    body = f"""
    <div class="header"><span class="back">‹ {esc(S['back'])}</span>
      <span class="title">{esc(S['packs_title'])}</span></div>
    <div style="padding:0 16px;display:flex;flex-direction:column;gap:12px">{''.join(rows)}</div>{overlay}"""
    return screen(body)


def packs_couple(S, lang):
    return packs_screen(S, lang, [(S["packs_section_couple"], [1, 2, 3, 4, 5, 6, 7, 8, 9, 10])])


def packs_friends(S, lang):
    return packs_screen(S, lang, [(S["packs_section_friends"], [20, 21]),
                                  (S["packs_section_spicy"], [30, 31, 32])], locked_ids=(31, 32))


def paywall(S, lang):
    dialog = f"""
    <div style="position:absolute;inset:0;background:rgba(0,0,0,.32)"></div>
    <div style="position:absolute;left:24px;right:24px;top:210px;background:#FBF1F3;border-radius:28px;
                padding:24px;display:flex;flex-direction:column;align-items:center">
      <div style="font-size:28px">⭐</div>
      <div style="font-size:24px;line-height:32px;text-align:center;margin:16px 0">{esc(S['paywall_title'])}</div>
      <div style="font-size:16px;line-height:24px;color:{ON_SURFACE_VARIANT};align-self:stretch">{esc(S['paywall_body'])}</div>
      <div style="height:16px"></div>
      <div class="btn primary">{esc(S['paywall_buy_noprice'])}</div>
      <div style="height:16px"></div>
      <div class="btn outline">{esc(S['paywall_watch_ad'])}</div>
      <div style="height:16px"></div>
      <div class="tbtn">{esc(S['paywall_later'])}</div>
    </div>"""
    return packs_screen(S, lang, [(S["packs_section_friends"], [20, 21]),
                                  (S["packs_section_spicy"], [30, 31, 32])], locked_ids=(31, 32), overlay=dialog)


def handoff(S, lang):
    a, b = NAMES[lang]
    body = f"""
    <div style="padding:24px;display:flex;flex-direction:column;align-items:center;color:#fff">
      <div style="align-self:flex-start;font-size:20px;padding:4px 8px">✕</div>
      <div style="height:48px"></div>
      <div style="width:120px;height:120px;border-radius:50%;background:rgba(255,255,255,.18);
                  display:flex;align-items:center;justify-content:center;font-size:60px">📱</div>
      <div style="height:32px"></div>
      <div style="font-size:28px;line-height:36px;font-weight:700;text-align:center">{esc(fmt(S['handoff_title'], b))}</div>
      <div style="height:12px"></div>
      <div style="font-size:17px;line-height:24px;opacity:.92;text-align:center">{esc(fmt(S['handoff_guess'], b, a))}</div>
      <div style="height:48px"></div>
      <div class="btn white">{esc(fmt(S['handoff_ready'], b))}</div>
    </div>"""
    return screen(body, light=True)


def question_screen(S, lang, index=3, qid=4, chosen=0, correct=0):
    a, b = NAMES[lang]
    pack = PACKS[6]
    q = next(x for x in pack["questions"] if x["id"] == qid)
    opts = []
    for i, ans in enumerate(q["a"]):
        cls = "correct" if i == correct else ("wrong" if i == chosen else "dim")
        opts.append(f'<div class="option {cls}">{esc(ans[lang])}</div>')
    progress = index / 10
    body = f"""
    <div style="display:flex;align-items:center;padding:4px 8px">
      <span style="font-size:20px;padding:10px 12px;color:{PINK}">✕</span>
      <span style="flex:1;font-size:14px;font-weight:500;color:{ON_SURFACE_VARIANT}">{esc(fmt(S['play_progress'], index + 1, 10))}</span>
      <span class="chip" style="background:{PRIMARY_CONTAINER};color:{ON_PRIMARY_CONTAINER};margin-right:12px">{pack['emoji']} {esc(pack['name'][lang])}</span>
    </div>
    <div style="margin:0 20px;height:8px;border-radius:4px;background:{PRIMARY_CONTAINER};overflow:hidden">
      <div style="width:{progress * 100}%;height:100%;background:{PINK};border-radius:4px"></div></div>
    <div style="padding:16px 20px;display:flex;flex-direction:column;gap:12px">
      <div style="font-size:14px;font-weight:600;color:{PINK}">{esc(fmt(S['play_guess_hint'], b, a))}</div>
      <div style="font-size:24px;line-height:32px;font-weight:700;margin-bottom:12px">{esc(q['q'][lang])}</div>
      {''.join(opts)}
    </div>"""
    return screen(body)


# respuestas (de quien responde) y adivinanzas para el resultado: 8 de 10 aciertos
RESULT_QIDS = [4, 1, 7, 11, 5, 12, 6, 3, 10, 2]
RESULT_ANSWERS = [0, 2, 0, 1, 1, 1, 0, 2, 3, 1]
RESULT_GUESSES = [0, 2, 0, 1, 2, 1, 0, 0, 3, 1]


def score_ring(percent):
    r = 86
    c = 2 * 3.14159 * r
    return f"""
    <div style="position:relative;width:190px;height:190px">
      <svg width="190" height="190" viewBox="0 0 190 190" style="transform:rotate(-90deg)">
        <circle cx="95" cy="95" r="{r}" fill="none" stroke="rgba(224,27,88,.15)" stroke-width="18"/>
        <circle cx="95" cy="95" r="{r}" fill="none" stroke="{PINK}" stroke-width="18" stroke-linecap="round"
                stroke-dasharray="{c * percent / 100} {c}"/></svg>
      <div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;
                  font-size:48px;font-weight:700">{percent}%</div>
    </div>"""


def review_items(S, lang, start=0, count=10):
    a, b = NAMES[lang]
    pack = PACKS[6]
    out = []
    for i in range(start, min(start + count, 10)):
        q = next(x for x in pack["questions"] if x["id"] == RESULT_QIDS[i])
        ans, gu = RESULT_ANSWERS[i], RESULT_GUESSES[i]
        match = ans == gu
        badge = (f'<div style="width:28px;height:28px;border-radius:50%;flex:none;background:{CORRECT if match else WRONG};'
                 f'color:#fff;font-weight:700;display:flex;align-items:center;justify-content:center">{"✓" if match else "✕"}</div>')
        lines = f'<div style="font-size:14px;line-height:20px;color:{ON_SURFACE_VARIANT}">{esc(fmt(S["result_answered"], a))}: {esc(q["a"][ans][lang])}</div>'
        if not match:
            lines += f'<div style="font-size:14px;line-height:20px;color:{ON_SURFACE_VARIANT}">{esc(fmt(S["result_guessed"], b))}: {esc(q["a"][gu][lang])}</div>'
        out.append(f"""
        <div class="card" style="margin:0 16px;display:flex;gap:12px;align-items:flex-start">{badge}
          <div style="display:flex;flex-direction:column;gap:4px">
            <div style="font-size:14px;line-height:20px;font-weight:600">{esc(q['q'][lang])}</div>{lines}</div></div>""")
    return "".join(out)


def result_header(S, lang):
    a, b = NAMES[lang]
    return f"""
    <div style="background:{PRIMARY_CONTAINER};padding:24px;display:flex;flex-direction:column;align-items:center;
                color:{ON_PRIMARY_CONTAINER}">
      <div style="font-size:22px;line-height:28px;font-weight:600;text-align:center">{esc(fmt(S['result_knows'], b, a))}</div>
      <div style="height:16px"></div>{score_ring(80)}<div style="height:16px"></div>
      <div style="font-size:24px;line-height:32px;font-weight:700;text-align:center">{esc(S['result_tier_2'])}</div>
      <div style="font-size:16px;line-height:24px;opacity:.8">{esc(fmt(S['result_score'], 8, 10))}</div>
    </div>"""


def result_top(S, lang):
    body = f"""{result_header(S, lang)}
    <div style="background:{PAPER};min-height:600px;padding:8px 20px;display:flex;flex-direction:column;gap:10px">
      <div class="btn primary">{esc(S['result_switch'])}</div>
      <div class="btn outline">{esc(S['result_share'])}</div>
      <div class="btn outline">{esc(S['result_other_pack'])}</div>
      <div class="tbtn">{esc(S['result_home'])}</div>
    </div>"""
    return screen(body, bg=PRIMARY_CONTAINER)


def result_review(S, lang):
    body = f"""
    <div style="font-size:16px;font-weight:700;padding:12px 24px 10px">{esc(S['result_review'])}</div>
    <div style="display:flex;flex-direction:column;gap:10px">{review_items(S, lang, 0, 7)}</div>"""
    return screen(body)


def challenge_ready(S, lang):
    a, b = NAMES[lang]
    body = f"""
    <div style="padding:24px;display:flex;flex-direction:column;align-items:center;gap:12px;color:#fff">
      <div style="height:56px"></div>
      <div style="font-size:72px;line-height:84px">💌</div>
      <div style="font-size:28px;line-height:36px;font-weight:700;text-align:center">{esc(S['challenge_ready_title'])}</div>
      <div style="font-size:17px;line-height:24px;opacity:.92;text-align:center">{esc(fmt(S['challenge_ready_body'], b))}</div>
      <div style="height:32px"></div>
      <div class="btn white">{esc(S['send_whatsapp'])}</div>
      <div class="btn outline white">{esc(S['send_other'])}</div>
      <div class="tbtn" style="color:#fff">{esc(S['copy_link'])}</div>
      <div class="tbtn" style="color:rgba(255,255,255,.85)">{esc(S['result_home'])}</div>
    </div>"""
    return screen(body, light=True)


SCREENS = {
    "result": result_top,
    "remote": challenge_ready,
    "review": result_review,
    "question": question_screen,
    "packs": packs_couple,
    "friends": packs_friends,
    "handoff": handoff,
    "premium": paywall,
    "home": home,
}
